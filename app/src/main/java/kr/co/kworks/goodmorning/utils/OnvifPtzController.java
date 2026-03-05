package kr.co.kworks.goodmorning.utils;

import java.util.Locale;

public class OnvifPtzController {
    private final double degreeUnit = 0.0055555555555556;

    private final OnvifSoapClient soap;
    private final String ptzServiceUrl;
    private final String profileToken;

    public OnvifPtzController(OnvifSoapClient soap, String deviceUrl, String profileToken) throws Exception {
        this.soap = soap;
        this.ptzServiceUrl = soap.getPtzServiceUrl(deviceUrl);
        this.profileToken = profileToken;
    }

    // ContinuousMove: panSpeed, tiltSpeed, zoomSpeed ∈ [-1.0, 1.0], timeoutSeconds 옵션
    public boolean continuousMove(Double panSpeed, Double tiltSpeed, Double zoomSpeed, Integer timeoutSeconds) throws Exception {
        StringBuilder v = new StringBuilder();
        if (panSpeed != null || tiltSpeed != null) {
            v.append("<tptz:PanTilt x=\"").append(panSpeed != null ? panSpeed : 0.0)
                .append("\" y=\"").append(tiltSpeed != null ? tiltSpeed : 0.0).append("\"/>");
        }
        if (zoomSpeed != null) {
            v.append("<tptz:Zoom x=\"").append(zoomSpeed).append("\"/>");
        }

        String timeout = (timeoutSeconds != null && timeoutSeconds > 0)
            ? "<tptz:Timeout>PT" + timeoutSeconds + "S</tptz:Timeout>" : "";

        String body = "<tptz:ContinuousMove>"
            + "  <tptz:ProfileToken>" + profileToken + "</tptz:ProfileToken>"
            + "  <tptz:Velocity>" + v + "</tptz:Velocity>"
            +      timeout
            + "</tptz:ContinuousMove>";

        String resp = soap.postSoap(ptzServiceUrl, body);
        return resp.contains("<tptz:ContinuousMoveResponse");
    }

    public boolean continuousMove2(Double panSpeed, Double tiltSpeed, Double zoomSpeed, Integer timeoutSeconds) throws Exception {
        StringBuilder v = new StringBuilder();
        if (panSpeed != null || tiltSpeed != null) {
            v.append("<tt:PanTilt x=\"").append(panSpeed != null ? panSpeed : 0.0)
                .append("\" y=\"").append(tiltSpeed != null ? tiltSpeed : 0.0).append("\"/>");
        }
        if (zoomSpeed != null) {
            v.append("<tptz:Zoom x=\"").append(zoomSpeed).append("\"/>");
        }

        String timeout = (timeoutSeconds != null && timeoutSeconds > 0)
            ? "<tptz:Timeout>PT" + timeoutSeconds + "S</tptz:Timeout>" : "";

        String body = "<tptz:ContinuousMove>"
            + "  <tptz:ProfileToken>" + profileToken + "</tptz:ProfileToken>"
            + "  <tptz:Velocity>" + v + "</tptz:Velocity>"
            +      timeout
            + "</tptz:ContinuousMove>";

        String resp = soap.postSoap2(ptzServiceUrl, body);
        return resp.contains("<tptz:ContinuousMoveResponse");
    }


    // AbsoluteMove: pan/tilt ∈ [-1, 1], zoom ∈ [0, 1] (모델마다 범위 상이)
    public boolean absoluteMove(Double pan, Double tilt, Double zoom,
                                Double panSpeed, Double tiltSpeed, Double zoomSpeed) throws Exception {
        StringBuilder pos = new StringBuilder();
        if (pan != null || tilt != null) {
            pos.append("<tptz:PanTilt x=\"").append(pan != null ? pan : 0.0)
                .append("\" y=\"").append(tilt != null ? tilt : 0.0).append("\"/>");
        }
        if (zoom != null) pos.append("<tptz:Zoom x=\"").append(zoom).append("\"/>");

        StringBuilder spd = new StringBuilder();
        if (panSpeed != null || tiltSpeed != null) {
            spd.append("<tptz:PanTilt x=\"").append(panSpeed != null ? panSpeed : 0.0)
                .append("\" y=\"").append(tiltSpeed != null ? tiltSpeed : 0.0).append("\"/>");
        }
        if (zoomSpeed != null) spd.append("<tptz:Zoom x=\"").append(zoomSpeed).append("\"/>");

        String body = "<tptz:AbsoluteMove>"
            + "  <tptz:ProfileToken>" + profileToken + "</tptz:ProfileToken>"
            + "  <tptz:Position>" + pos + "</tptz:Position>"
            + "  <tptz:Speed>" + spd + "</tptz:Speed>"
            + "</tptz:AbsoluteMove>";
        String resp = soap.postSoap(ptzServiceUrl, body);
        return resp.contains("<tptz:AbsoluteMoveResponse");
    }

    /**
     * 절대 이동
     * @param pan -1.0 ~ 1.0
     * @param tilt -1.0 ~ 1.0
     * @param zoom 0.0 ~ 1.0
     * @return 성공 여부 (boolean)
     * @throws Exception
     */
    public boolean absoluteMove2(double pan, double tilt, double zoom) throws Exception {

        String pos = String.format(Locale.KOREA, "<tt:PanTilt x=\"%f\" y=\"%f\" />" +
            "<tt:Zoom x=\"%f\" />", pan, tilt, zoom);

        String body = "<tptz:AbsoluteMove>"
            + "<tptz:ProfileToken>" + profileToken + "</tptz:ProfileToken>"
            + "<tptz:Position>" + pos + "</tptz:Position>"
            + "</tptz:ContinuousMove>";

        String resp = soap.postSoap2(ptzServiceUrl, body);
        return resp.contains("<tptz:ContinuousMoveResponse");
    }

    public double parsePanValue(int degree) {
        double value = degree * degreeUnit;
        value -= 1;
        return value;
    }
}