package kr.co.kworks.goodmorning.utils;

import android.os.Build;
import android.util.Base64;

import okhttp3.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OnvifSoapClient {
    private final OkHttpClient http;
    private final String username;
    private final String password;

    public OnvifSoapClient(String username, String password) {
        this.username = username;
        this.password = password;
        this.http = new OkHttpClient.Builder()
            .connectTimeout(3000, TimeUnit.MILLISECONDS)
            .readTimeout(3000, TimeUnit.MILLISECONDS)
            .writeTimeout(3000, TimeUnit.MILLISECONDS)
            .authenticator(this::digestAuth)
            .build();
    }

    private Request digestAuth(Route route, Response resp) {
        String hdr = resp.header("WWW-Authenticate");
        if (hdr == null || !hdr.startsWith("Digest")) return null;

        DigestParams p = DigestParams.parse(hdr);
        String cnonce = UUID.randomUUID().toString().replace("-", "");
        String nc = "00000001";

        Request prior = resp.request();
        String method = prior.method();
        String uriPath = prior.url().encodedPath();

        String ha1 = md5(username + ":" + p.realm + ":" + password);
        String ha2 = md5(method + ":" + uriPath);
        String response;
        if (p.qop != null && p.qop.contains("auth")) {
            response = md5(ha1 + ":" + p.nonce + ":" + nc + ":" + cnonce + ":auth:" + ha2);
        } else {
            response = md5(ha1 + ":" + p.nonce + ":" + ha2);
        }

        String auth = "Digest username=\"" + username + "\", realm=\"" + p.realm + "\", nonce=\"" + p.nonce + "\", uri=\"" + uriPath + "\", "
            + (p.qop != null && p.qop.contains("auth") ? ("qop=auth, nc=" + nc + ", cnonce=\"" + cnonce + "\", ") : "")
            + "response=\"" + response + "\", algorithm=MD5";

        return prior.newBuilder()
            .header("Authorization", auth)
            .build();
    }

    private static class DigestParams {
        final String realm, nonce, qop;
        DigestParams(String realm, String nonce, String qop) { this.realm = realm; this.nonce = nonce; this.qop = qop; }
        static DigestParams parse(String hdr) {
            return new DigestParams(kv(hdr,"realm"), kv(hdr,"nonce"), kv(hdr,"qop"));
        }
        private static String kv(String s, String key) {
            int i = s.indexOf(key + "=\""); if (i < 0) return null;
            int j = s.indexOf('"', i + key.length() + 2);
            return s.substring(i + key.length() + 2, j);
        }
    }

    private static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest(s.getBytes(StandardCharsets.ISO_8859_1));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static String envelope(String body) {
        return "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" "
            + "xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\" "
            + "xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\" "
            + "xmlns:tptz=\"http://www.onvif.org/ver20/ptz/wsdl\" "
            + "xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\""
            + "xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\""
            + "xmlns:tt=\"http://www.onvif.org/ver10/schema\">"
            + "<s:Body>" + body + "</s:Body></s:Envelope>";
    }

    public String makeHeader(String user, String password) {

        // 2) nonce
        byte[] nonceBytes = random16Bytes();
        String nonceB64 = Base64.encodeToString(nonceBytes, 0);
        String created = getCreated();

        // 3) created
        byte[] createdUtf8 = created.getBytes(StandardCharsets.UTF_8);
        byte[] passwordUtf8 = password.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(nonceBytes);
            baos.write(createdUtf8);
            baos.write(passwordUtf8);
        } catch (IOException e) {
            Logger.getInstance().error("makeHeader", e);
        }

        MessageDigest sha1 = null;
        String digestB64 = "";
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(baos.toByteArray());
            digestB64 = Base64.encodeToString(hash, 0);
            digestB64 = digestB64.replace("\n","");
        } catch (NoSuchAlgorithmException e) {
            Logger.getInstance().error("makeHeader", e);
        }

        return String.format(Locale.KOREA, "<s:Header>"
            + "<wsse:Security>"
                + "<wsse:UsernameToken>"
                    + "<wsse:Username>%s</wsse:Username>"
                    + "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%s</wsse:Password>"
                    + "<wsse:Nonce>%s</wsse:Nonce>"
                    + "<wsu:Created>%s</wsu:Created>"
                + "</wsse:UsernameToken>"
            + "</wsse:Security>"
        + "</s:Header>", user, digestB64, nonceB64, created);
    }

    public byte[] random16Bytes() {
        byte[] bytes = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    public String secureEnvelope(String user, String password, String body) {
        return String.format(Locale.KOREA, "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" "
            + "xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\" "
            + "xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\" "
            + "xmlns:tptz=\"http://www.onvif.org/ver20/ptz/wsdl\" "
            + "xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\""
            + "xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\""
            + "xmlns:tt=\"http://www.onvif.org/ver10/schema\">"
            + "%s"
            + "<s:Body>" + body + "</s:Body></s:Envelope>", makeHeader(user, password));
    }

    public String getCreated() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant now = Instant.now();
            return DateTimeFormatter.ISO_INSTANT.format(now);
        }

        return "";
    }


    public String postSoap2(String url, String soapBody) throws Exception {
        String rawString = secureEnvelope(username, password, soapBody);
        Request req = new Request.Builder()
            .url(url)
            .post(RequestBody.create(rawString, MediaType.parse("application/xml; charset=utf-8")))
            .build();
        try (Response response = http.newCall(req).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("HTTP " + response.code());
            return response.body() != null ? response.body().string() : "";
        }
    }


    public String postSoap(String url, String soapBody) throws Exception {
        Request req = new Request.Builder()
            .url(url)
            .post(RequestBody.create(envelope(soapBody), MediaType.parse("application/soap+xml; charset=utf-8")))
            .build();
        try (Response response = http.newCall(req).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("HTTP " + response.code());
            return response.body() != null ? response.body().string() : "";
        }
    }

    public String getPtzServiceUrl(String deviceServiceUrl) throws Exception {
        String body = "<tds:GetCapabilities xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\"><tds:Category>All</tds:Category></tds:GetCapabilities>";
        String xml = postSoap(deviceServiceUrl, body);
        String open = "<tt:PTZ><tt:XAddr>";
        String close = "</tt:XAddr>";
        int openIndex = xml.indexOf(open);
        if (openIndex < 0) throw new IllegalStateException("PTZ XAddr not found");
        int closeIndex = xml.indexOf(close, openIndex);
        return xml.substring(openIndex + open.length(), closeIndex).trim();
    }
}