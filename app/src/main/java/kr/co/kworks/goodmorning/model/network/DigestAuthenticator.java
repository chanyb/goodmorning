package kr.co.kworks.goodmorning.model.network;

import okhttp3.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DigestAuthenticator implements Authenticator {
    private final String username;
    private final String password;
    private final Random random = new Random();

    public DigestAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // 무한 재시도 방지
        if (response.request().header("Authorization") != null) return null;

        String authHeader = response.header("WWW-Authenticate");
        if (authHeader == null || !authHeader.startsWith("Digest")) return null;

        Map<String, String> params = parseAuthHeader(authHeader);

        String realm = params.get("realm");
        String nonce = params.get("nonce");
        String qop = params.getOrDefault("qop", "auth");
        String opaque = params.get("opaque");
        String algorithm = params.getOrDefault("algorithm", "MD5");

        if (!"MD5".equalsIgnoreCase(algorithm)) {
            // 필요 시 확장
            return null;
        }

        String method = response.request().method();
        HttpUrl url = response.request().url();
        String uri = url.encodedPath() + (url.encodedQuery() != null ? "?" + url.encodedQuery() : "");

        String cnonce = randomHex(16);
        String nc = "00000001";

        String ha1 = md5(username + ":" + realm + ":" + password);
        String ha2 = md5(method + ":" + uri);
        String resp;
        if (qop.contains("auth")) {
            resp = md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":auth:" + ha2);
        } else {
            resp = md5(ha1 + ":" + nonce + ":" + ha2);
        }

        StringBuilder auth = new StringBuilder();
        auth.append("Digest ");
        auth.append(kv("username", username)).append(", ");
        auth.append(kv("realm", realm)).append(", ");
        auth.append(kv("nonce", nonce)).append(", ");
        auth.append("uri=\"").append(uri).append("\", ");
        auth.append("response=\"").append(resp).append("\", ");
        auth.append("algorithm=MD5");
        if (opaque != null) auth.append(", ").append(kv("opaque", opaque));
        if (qop.contains("auth")) {
            auth.append(", qop=auth");
            auth.append(", nc=").append(nc);
            auth.append(", cnonce=\"").append(cnonce).append("\"");
        }

        return response.request().newBuilder()
            .header("Authorization", auth.toString())
            .build();
    }

    private static String kv(String k, String v) {
        return k + "=\"" + v + "\"";
    }

    private static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest(s.getBytes(StandardCharsets.ISO_8859_1));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> parseAuthHeader(String h) {
        Map<String, String> map = new HashMap<>();
        Pattern p = Pattern.compile("(\\w+)=\"?([^\"]*)\"?");
        Matcher m = p.matcher(h);
        while (m.find()) {
            map.put(m.group(1), m.group(2));
        }
        return map;
    }

    private String randomHex(int len) {
        byte[] buf = new byte[len/2 + 1];
        random.nextBytes(buf);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len/2; i++) sb.append(String.format("%02x", buf[i]));
        return sb.toString();
    }
}