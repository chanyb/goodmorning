package kr.co.kworks.goodmorning.model.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kr.co.kworks.goodmorning.model.business_logic.Recognition;
import kr.co.kworks.goodmorning.utils.Logger;

public class ByteSocketServer extends Thread {

    private static final String CMD_RECOGNITION = "Recognition";
    private static final String CMD_IMAGE = "Image";

    private final int port;
    private volatile boolean running = true;

    private String previousCommand;
    private final Gson gson;

    private Selector selector;
    private ServerSocketChannel serverChannel;

    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(64 * 1024);
    private final Map<SocketChannel, ClientState> clients = new ConcurrentHashMap<>();

    public ByteSocketServer(int port) {
        this.port = port;
        gson = new Gson();
    }

    public interface ServerListener {
        default void onClientConnected(SocketChannel ch) {}
        default void onClientDisconnected(SocketChannel ch, Throwable cause) {}
        default void onText(SocketChannel ch, String text) {}
        default void onImage(SocketChannel ch, byte[] image) {}
    }

    private volatile ServerListener listener;
    public void setListener(ServerListener l) { this.listener = l; }

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
            clientState.appendAndParse(readBuffer, (payload) -> {
                clientState.touch();
                handleFrame2(channel, payload);
            });
            clientState.touch();
        } catch (Exception e) {
            Logger.getInstance().error("read error: ", e);
            safeClose(channel);
        }
    }

    /** payload가 텍스트면 명령 처리, 아니면 이미지 처리 */
    private void handleFrame(SocketChannel channel, byte[] payload) {
        if (isLikelyText(payload)) {
            String line = new String(payload, StandardCharsets.UTF_8).trim();
            if (line.equalsIgnoreCase("PING")) {
                enqueueString(channel, "PONG");
            } else if (line.startsWith("BROADCAST ")) {
                String msg = line.substring(10);
                broadcastString("MSG " + msg);
            } else {
                enqueueString(channel, "ECHO " + line);
            }

            if (listener != null) listener.onText(channel, line);
        } else {
            Logger.getInstance().info("handleImage: " + payload.length + " bytes");
            onImage(channel, payload);
            // 필요 시 응답
            enqueueString(channel, "IMAGE_OK " + payload.length);
        }
    }

    /** payload가 어떤 명령어인지 판단 */
    private void handleFrame2(SocketChannel channel, byte[] payload) {
        if (isAsciiText(payload)) {
            String command = new String(payload, StandardCharsets.UTF_8).trim();

            switch (command) {
                case CMD_RECOGNITION -> {
                    previousCommand = CMD_RECOGNITION;
                }
                case CMD_IMAGE -> {
                    previousCommand = CMD_IMAGE;
                }
                default -> {
                    Logger.getInstance().info("default");
                }
            }

            if (listener != null) listener.onText(channel, command);
        } else {
            Logger.getInstance().info("handleImage: " + payload.length + " bytes");

            if (previousCommand == null) {
                return;
            }

            switch (previousCommand) {
                case CMD_RECOGNITION -> {
                    String json = new String(payload, StandardCharsets.UTF_8);
                    Recognition recognition = gson.fromJson(json, Recognition.class);
                    Logger.getInstance().info("recognition: " + recognition.toString());
                }
                case CMD_IMAGE -> {
                    onImage(channel, payload);
                }
                default -> {
                    Logger.getInstance().info("default");
                }
            }
            previousCommand = null;
        }
    }

    public void broadcastString(String msg) {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        for (SocketChannel ch : clients.keySet()) {
            enqueueBinary(ch, bytes);
        }
    }

    public static boolean isAsciiText(byte[] data) {
        if (data == null || data.length == 0) return false;

        int printable = 0;
        for (int i = 0; i < data.length && i < 20; i++) {
            int b = data[i] & 0xFF;
            if (b == 0) return false; // NUL 있으면 바이너리 가능성 높음
            if (b >= 0x20 && b <= 0x7E) { // ASCII printable
                printable++;
            }
        }
        double ratio = (double) printable / data.length;
        return ratio > 0.99; // 99% 이상이 프린터블이면 텍스트
    }


    /** payload가 텍스트처럼 보이는지 판정 */
    private static boolean isLikelyText(byte[] data) {
        if (data.length == 0) return false;
        int printable = 0, checked = 0;
        for (int i = 0; i < data.length && i < 20; i++) { // 처음 512바이트만 검사
            int b = data[i] & 0xff;
            if (b == 0) return false; // NUL 있으면 바이너리 가능성 높음
            if (b >= 0x09 && b <= 0x0d) { checked++; printable++; continue; } // \t..\r
            if (b >= 0x20 && b <= 0x7e) { checked++; printable++; continue; } // ASCII printable
            checked++;
        }
        return checked > 0 && (printable * 1.0 / checked) > 0.85;
    }

    /** 바이너리(이미지) 수신 처리 훅 */
    private void onImage(SocketChannel channel, byte[] imageBytes) {
        // TODO: 파일로 저장하거나, 디코딩/처리 로직 배치
        Logger.getInstance().info("onImage: image received.");
        if (listener != null) listener.onImage(channel, imageBytes);
    }

    public Bitmap getBitmapFromByteArray(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /** 문자열/바이너리 모두 length 프레임으로 큐잉 */
    private void enqueueString(SocketChannel ch, String msg) {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        enqueueInt(ch, bytes.length);
        enqueueBinary(ch, bytes);
    }

    private void enqueueInt(SocketChannel ch, int integer) {
        Logger.getInstance().info("enqueueInt");
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (integer >> 24); // 최상위 바이트
        bytes[1] = (byte) (integer >> 16);
        bytes[2] = (byte) (integer >> 8);
        bytes[3] = (byte) (integer);       // 최하위 바이트
        enqueueBinary(ch, bytes);
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

    public void enqueueBinary(SocketChannel ch, byte[] payload) {
        ClientState st = clients.get(ch);
        if (st == null) return;
        // 프레임: 4B length + payload
        ByteBuffer frame = ByteBuffer.allocate(4 + payload.length);
        frame.putInt(payload.length);
        frame.put(payload);
        frame.flip();
        synchronized (st.outQueue) {
            st.outQueue.add(frame);
        }
        SelectionKey key = ch.keyFor(selector);
        if (key != null && key.isValid()) key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        if (selector != null) selector.wakeup();
    }

    private void write(SocketChannel channel) {
        ClientState st = clients.get(channel);
        if (st == null) { close(channel); return; }

        try {
            // pendingWrite가 있으면 먼저 처리
            if (st.pendingWrite != null) {
                channel.write(st.pendingWrite);
                if (st.pendingWrite.hasRemaining()) {
                    // 아직 못 보냄 → 다음 라운드
                    return;
                } else {
                    st.pendingWrite = null;
                }
            }

            // 큐에서 계속 꺼내서 보냄
            while (true) {
                ByteBuffer buf;
                synchronized (st.outQueue) {
                    buf = st.outQueue.poll();
                }
                if (buf == null) break;

                channel.write(buf);
                if (buf.hasRemaining()) {
                    // 다음 라운드에 마저 보냄
                    st.pendingWrite = buf;
                    break;
                }
            }
            st.touch();

            // 더 보낼 게 없으면 WRITE 관심 해제
            SelectionKey key = channel.keyFor(selector);
            if (key != null && key.isValid()
                && st.pendingWrite == null
                && st.outQueue.isEmpty()) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            Logger.getInstance().error("write error: ", e);
            close(channel);
        }
    }

    public void broadcast(String msg) {
        for (SocketChannel channel : clients.keySet()) enqueue(channel, msg);
    }

    private void sweepIdleClients(long idleMillis) {
        long now = System.currentTimeMillis();
        for (Map.Entry<SocketChannel, ClientState> e : clients.entrySet()) {
            if (now - e.getValue().lastActive > idleMillis) {
                Logger.getInstance().info("idle close: " + e.getKey());
                close(e.getKey());
            }
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
        private ByteBuffer byteBuffer = ByteBuffer.allocate(64 * 1024);
        private int expected = -1;

        final StringBuilder in = new StringBuilder();
        final ArrayDeque<String> out = new ArrayDeque<>();

        // 쓰기 큐(프레임 단위), 부분 쓰기 버퍼
        final ArrayDeque<ByteBuffer> outQueue = new ArrayDeque<>();
        ByteBuffer pendingWrite = null;

        volatile long lastActive = System.currentTimeMillis();
        void touch() { lastActive = System.currentTimeMillis(); }

        interface FrameHandler { void onFrame(byte[] payload); }

        void appendAndParse(ByteBuffer src, FrameHandler handler) {
            ensureCapacity(byteBuffer, src.remaining());
            byteBuffer.put(src);
            byteBuffer.flip();
            // length(4B) → payload(expected) 반복 파싱
            while (true) {
                if (expected < 0) {
                    if (byteBuffer.remaining() < 4) break;
                    expected = ((byteBuffer.get() & 0xff) << 24) | ((byteBuffer.get() & 0xff) << 16) | ((byteBuffer.get() & 0xff) << 8) | (byteBuffer.get() & 0xff);
                    if (expected < 0 || expected > (100 * 1024 * 1024)) {
                        throw new IllegalStateException("Bad length: " + expected);
                    }
                }
                if (byteBuffer.remaining() < expected) break;

                byte[] payload = new byte[expected];
                byteBuffer.get(payload);
                expected = -1; // 다음 프레임 준비

                // 프레임 콜백
                handler.onFrame(payload);
            }
            byteBuffer.compact();
        }

        private void ensureCapacity(ByteBuffer buf, int need) {
            if (buf.remaining() >= need) return;
            buf.flip();
            int newCap = Math.max(buf.capacity() * 2, buf.limit() + need);
            ByteBuffer bigger = ByteBuffer.allocate(newCap);
            bigger.put(buf);
            byteBuffer = bigger;
        }
    }
}
