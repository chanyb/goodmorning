package kr.co.kworks.goodmorning.model.network;

import android.util.Base64;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kr.co.kworks.goodmorning.utils.Logger;

public class StringSocketServer extends Thread {
    private final int port;
    private volatile boolean running = true;

    private Selector selector;
    private ServerSocketChannel serverChannel;

    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(512 * 512);
    private final Map<SocketChannel, ClientState> clients = new ConcurrentHashMap<>();

    public StringSocketServer(int port) { this.port = port; }

    public interface ServerListener {
        default void onClientConnected(SocketChannel ch) {}
        default void onClientDisconnected(SocketChannel ch, Throwable cause) {}
        default void onText(SocketChannel ch, String text) {}
        default void onImage(SocketChannel ch, byte[] image) {}
    }

    private volatile StringSocketServer.ServerListener listener;
    public void setListener(StringSocketServer.ServerListener l) { this.listener = l; }

    @Override public void run() {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (running) {
                selector.select(1000); // 1s 타임아웃 (깨우기용)
                for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        Logger.getInstance().info("acceptable");
                        accept();
                        continue;
                    }
                    if (key.isReadable())   {
                        Logger.getInstance().info("readable");
                        read((SocketChannel) key.channel());
                        continue;
                    }
                    if (key.isWritable())   {
                        Logger.getInstance().info("writeable");
                        write((SocketChannel) key.channel());
                        continue;
                    }
                }
                // 좀비 세션 처리
                sweepIdleClients(180_000); // 3분 이상 idle 정리
            }
        } catch (IOException e) {
            // 로그 처리
            Logger.getInstance().error("SocketSever: ", e);
        } catch (Exception e) {
            Logger.getInstance().error("SocketSever: ", e);
        } finally {
            closeAll();
        }

    }

    private void accept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        if (channel == null) return;
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        channel.register(selector, SelectionKey.OP_READ);
        clients.put(channel, new ClientState());
    }

    private void read(SocketChannel channel) {
        ClientState clientState = clients.get(channel);
        if (clientState == null) { close(channel); return; }

        readBuffer.clear();
        try {
            int n = channel.read(readBuffer);
            if (n == -1) { close(channel); return; }
            if (n == 0)  { return; }

            readBuffer.flip();
            // 간단한 '\n' 구분 프로토콜 예시
            byte[] chunk = new byte[readBuffer.remaining()];
            readBuffer.get(chunk); // 유효 구간만 복사
            clientState.in.append(new String(chunk, java.nio.charset.StandardCharsets.UTF_8));
//            clientState.in.append(new String(readBuffer.array(), readBuffer.position(), readBuffer.remaining()));
            clientState.touch();

            int idx;
            while ((idx = clientState.in.indexOf("#")) >= 0) {
                String line = clientState.in.substring(0, idx).trim();
                clientState.in.delete(0, idx + 1);
                handleMessage(channel, line);
            }
        } catch (IOException e) {
            safeClose(channel);
        }
    }

    private void handleMessage(SocketChannel channel, String line) {
        if (line.equalsIgnoreCase("PING")) {
            enqueue(channel, "PONG\n");
        } else if (line.startsWith("BROADCAST ")) {
            String msg = line.substring(10);
            broadcast("MSG " + msg + "\n");
        } else if (line.startsWith("PIC ")) {
            String base64String = line.split(" ")[1];
            byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);
            if (listener != null) listener.onImage(channel, bytes);
            enqueue(channel, "PIC SEND SUCCESS\n");
        } else {
            if (line.length() > 10) enqueue(channel, "ECHO " + line.substring(0,10));
            else enqueue(channel, "ECHO " + line);
        }
    }

    private void enqueue(SocketChannel channel, String msg) {
        Logger.getInstance().info("enqueue: " + msg);
        ClientState clientState = clients.get(channel);
        if (clientState == null) return;
        synchronized (clientState.out) { clientState.out.add(msg); }
        SelectionKey key = channel.keyFor(selector);
        if (key != null && key.isValid()) key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    private void write(SocketChannel channel) {
        ClientState st = clients.get(channel);
        if (st == null) { close(channel); return; }
        try {
            String data;
            while (true) {
                synchronized (st.out) {
                    data = st.out.peek();
                }
                if (data == null) break;

                ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
                channel.write(buf);
                if (buf.hasRemaining()) break; // 소켓 버퍼 풀 → 다음 라운드에
                synchronized (st.out) { st.out.poll(); }
            }
            st.touch();

            // 더 보낼 게 없으면 WRITE 관심 해제
            SelectionKey key = channel.keyFor(selector);
            if (key != null && key.isValid() && (st.out.isEmpty())) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            close(channel);
        }
    }

    public void broadcast(String msg) {
        for (SocketChannel channel : clients.keySet()) enqueue(channel, msg);
    }

    private void sweepIdleClients(long idleMillis) {
        long now = System.currentTimeMillis();
        for (Map.Entry<SocketChannel, ClientState> e : clients.entrySet()) {
            if (now - e.getValue().lastActive > idleMillis) close(e.getKey());
        }
    }

    private void close(SocketChannel channel) {
        clients.remove(channel);
        try { channel.close(); } catch (IOException ignored) {}
    }

    private void safeClose(SocketChannel channel) {
        try {
            SelectionKey key = channel.keyFor(selector);
            if (key != null) {
                try { key.cancel(); } catch (Exception ignored) {}
            }
            clients.remove(channel);
            try { channel.close(); } catch (Exception ignored) {}
        } finally {
            // selector가 즉시 취소된 키를 반영하게
            if (selector != null) selector.wakeup();
        }
    }

    private void closeAll() {
        Logger.getInstance().info("closeAll");
        try { if (serverChannel != null) serverChannel.close(); } catch (IOException ignored) {}
        try { if (selector != null) selector.close(); } catch (IOException ignored) {}
        for (SocketChannel channel : clients.keySet()) close(channel);
        clients.clear();
    }

    public void shutdown() {
        running = false;
        if (selector != null) selector.wakeup();
    }

    static class ClientState {
        final StringBuilder in = new StringBuilder();
        final ArrayDeque<String> out = new ArrayDeque<>();
        volatile long lastActive = System.currentTimeMillis();
        void touch() { lastActive = System.currentTimeMillis(); }
    }
}
