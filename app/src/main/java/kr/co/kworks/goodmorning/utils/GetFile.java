package kr.co.kworks.goodmorning.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GetFile {
    public static final int HTTP_CONNECTION_TIMEOUT = 3;

    public GetFile() {
    }

    // http 프로토콜 화일 다운(text)
    public boolean textDownload(String Url, String FileName, String sSavePath) {
        // 다운 받을 화일이 위치한 서버 경로
        URL Downloadurl;
        boolean isComplete = false;
        try {
            Downloadurl = new URL(Url + FileName);

            // http 프로토콜 연결
            HttpURLConnection conn = (HttpURLConnection) Downloadurl.openConnection();
            conn.setConnectTimeout(HTTP_CONNECTION_TIMEOUT * 1000);
            conn.setReadTimeout(HTTP_CONNECTION_TIMEOUT * 1000);
            BufferedReader in = new BufferedReader(new InputStreamReader((InputStream) conn.getInputStream(), "UTF-8"));
            // 임시 폴더에 다운받음
            FileOutputStream fos = new FileOutputStream(sSavePath + "/" + FileName);

            byte[] contentInBytes;
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String data = inputLine + "\r\n";
                contentInBytes = data.getBytes();
                fos.write(contentInBytes);
            }
            fos.flush();
            fos.close();
            isComplete = true;
            Logger.getInstance().info("textDownload complete");
        } catch (Exception ex) {
            Logger.getInstance().error("GetFile-TextDownload", ex);
            isComplete = false;
        }
        return isComplete;
    }

    // http 프로토콜 화일 다운(content)
    public boolean contentDownload(String Url, String FileName, String sSavePath, Consumer<Long> callback) {
        // 다운 받을 화일이 위치한 서버 경로
        URL Downloadurl;
        boolean isComplete = false;
        try {
            Downloadurl = new URL(Url + FileName);

            // http 프로토콜 연결
            HttpURLConnection conn = (HttpURLConnection) Downloadurl.openConnection();

            byte[] buffer = new byte[1024];
            int length = 0;
            long fileSize = conn.getContentLength();

            InputStream is = conn.getInputStream();
            // 임시 폴더에 다운받음
            FileOutputStream fos = new FileOutputStream(sSavePath + "/" + FileName, false);

            long downloadedFileSize = 0;
            while ((length = is.read(buffer)) >= 0) {
                downloadedFileSize += length;
                long progress = (downloadedFileSize * 100L) / fileSize;
                fos.write(buffer, 0, length);
                callback.accept(progress);
            }

            fos.flush();
            fos.close();
            is.close();
            isComplete = true;
        } catch (Exception ex) {
            Logger.getInstance().error("GetFile-ContentDownload", ex);
            isComplete = false;
        }
        return isComplete;
    }

    public boolean contentDownload(String Url, String FileName, String sSavePath) {
        // 다운 받을 화일이 위치한 서버 경로
        URL Downloadurl;
        boolean isComplete = false;
        try {
            Downloadurl = new URL(Url + FileName);

            // http 프로토콜 연결
            HttpURLConnection conn = (HttpURLConnection) Downloadurl.openConnection();

            byte[] buffer = new byte[1024];
            int length = 0;
            long fileSize = conn.getContentLength();

            InputStream is = conn.getInputStream();
            // 임시 폴더에 다운받음
            FileOutputStream fos = new FileOutputStream(sSavePath + "/" + FileName, false);

            long downloadedFileSize = 0;
            while ((length = is.read(buffer)) >= 0) {
                downloadedFileSize += length;
                long progress = (downloadedFileSize * 100L) / fileSize;
                fos.write(buffer, 0, length);
            }

            fos.flush();
            fos.close();
            is.close();
            isComplete = true;
        } catch (Exception ex) {
            Logger.getInstance().error("GetFile-ContentDownload", ex);
            isComplete = false;
        }
        return isComplete;
    }

    public List<String> readFileToLineList(String filePath) {
        ArrayList<String> lines = new ArrayList<>();
        Reader reader = null;
        BufferedReader bufferedReader = null;
        File file = new File(filePath);
        if (!file.exists()) {
            return lines;
        }

        try {
            reader = new InputStreamReader(new FileInputStream(filePath), "UTF-8");
            bufferedReader = new BufferedReader(reader, 8*1024); // 8KB
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            Logger.getInstance().error("readFile - error", e);
        }

        try {
            if (bufferedReader != null) bufferedReader.close();
            if (reader != null) reader.close();
        } catch (IOException e) {
            Logger.getInstance().error("readFile - close error", e);
        }


        return lines;
    }
}
