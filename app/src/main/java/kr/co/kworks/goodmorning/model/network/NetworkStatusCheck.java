package kr.co.kworks.goodmorning.model.network;

import android.util.Base64;

import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import kr.co.kworks.goodmorning.utils.ApiConstants;
import kr.co.kworks.goodmorning.utils.Logger;


@Singleton
public class NetworkStatusCheck {
    public static final int PING_TEST=0, PING_FAIL=1, PING_SUCCESS=2;
    public Executor executor, integrateServerExecutor;
    public MutableLiveData<Integer> status_camera, status_integrate_server, status_weather_sensor, status_vehicle_board, status_software_update_server, status_edge_ai_device;
    private String cameraIp, integrateServerDomain, weatherSensorIp, edgeAiDeviceIp, softwareUpdateServerDomain, vehicleBoardIp;


    @Inject
    public NetworkStatusCheck() {
        executor = Executors.newScheduledThreadPool(6);
        status_camera = new MutableLiveData<>(PING_TEST);
        status_integrate_server = new MutableLiveData<>(PING_TEST);
        status_weather_sensor = new MutableLiveData<>(PING_TEST);
        status_vehicle_board = new MutableLiveData<>(PING_TEST);
        status_software_update_server = new MutableLiveData<>(PING_TEST);
        status_edge_ai_device = new MutableLiveData<>(PING_TEST);
        this.cameraIp = ApiConstants.WONWOO_CAMERA_IP;
        this.integrateServerDomain = ApiConstants.INTEGRATE_SERVER_DOMAIN;
        this.weatherSensorIp = ApiConstants.SENSOR_IP;
        this.edgeAiDeviceIp = ApiConstants.EMPTY_IP;
        this.softwareUpdateServerDomain = ApiConstants.SOFTWARE_UPDATE_SERVER_DOMAIN;
        this.vehicleBoardIp = ApiConstants.EMPTY_IP;
    }

    public void cameraPingTest() {
        executor.execute(() -> {
            status_camera.postValue(PING_TEST);
            boolean success = 0 == runPing(cameraIp);
            if(success) status_camera.postValue(PING_SUCCESS);
            else status_camera.postValue(PING_FAIL);
        });
    }

    public void integrateServerPingTest() {
        executor.execute(() -> {
            status_integrate_server.postValue(PING_TEST);
            boolean success = 0 == runPing(integrateServerDomain);
            if(success) status_integrate_server.postValue(PING_SUCCESS);
            else status_integrate_server.postValue(PING_FAIL);
        });
    }

    public void weatherSensorPingTest() {
        executor.execute(() -> {
            status_weather_sensor.postValue(PING_TEST);
            boolean success = 200 == runHttp(weatherSensorIp, "kworks", "kworks0001819!!");
            if(success) status_weather_sensor.postValue(PING_SUCCESS);
            else status_weather_sensor.postValue(PING_FAIL);
        });
    }

    public void edgeAiDevicePingTest() {
        executor.execute(() -> {
            status_edge_ai_device.postValue(PING_TEST);
            boolean success = 0 == runPing(edgeAiDeviceIp);
            if(success) status_edge_ai_device.postValue(PING_SUCCESS);
            else status_edge_ai_device.postValue(PING_FAIL);
        });
    }

    public void vehicleBoardPingTest() {
        executor.execute(() -> {
            status_vehicle_board.postValue(PING_TEST);
            boolean success = 0 == runPing(vehicleBoardIp);
            if(success) status_vehicle_board.postValue(PING_SUCCESS);
            else status_vehicle_board.postValue(PING_FAIL);
        });
    }

    public void softwareUpdateServerPingTest() {
        executor.execute(() -> {
            status_software_update_server.postValue(PING_TEST);
            boolean success = 200 == runHttp(softwareUpdateServerDomain, "","");
            if(success) status_software_update_server.postValue(PING_SUCCESS);
            else status_software_update_server.postValue(PING_FAIL);
        });
    }

    private int runPing(String ip) {
        StringBuilder output = new StringBuilder();
        try {
            // -c: count, -W: timeout(sec)
            Process process = Runtime.getRuntime().exec("ping -c 4 -W 3 " + ip);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

//            Logger.getInstance().info(output.toString());
            return process.waitFor(); // 0 이면 성공
        } catch (Exception e) {
            output.append("Error: ").append(e.getMessage());
//            Logger.getInstance().error(output.toString(), null);
        }
        return -1;
    }

    private int runHttp(String url, String username, String password) {
        HttpURLConnection conn = null;
        try {
            URL target = new URL(url);
            conn = (HttpURLConnection) target.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (username != null && !username.isEmpty()) {
                String auth = username + ":" + password;
                String encoded = null;

                encoded = Base64.encodeToString(auth.getBytes(StandardCharsets.UTF_8), 0);
                conn.setRequestProperty("Authorization", "Basic " + encoded);
            }

            int responseCode = conn.getResponseCode();
            Logger.getInstance().info("HTTP Status Code: " + responseCode);
            return responseCode;

        } catch (Exception e) {
            Logger.getInstance().error("runHttp error", e);
            return -1;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
