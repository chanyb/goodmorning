package kr.co.kworks.goodmorning.utils;

public class OnvifDeviceMgmt {

    private final OnvifSoapClient soap;
    private final String deviceServiceUrl; // 보통 http://IP:PORT/onvif/device_service

    public OnvifDeviceMgmt(OnvifSoapClient soap, String deviceServiceUrl) {
        this.soap = soap;
        this.deviceServiceUrl = deviceServiceUrl;
    }

    public static class MediaAddrs {
        public final String media2; // ver20 media wsdl
        public final String media1; // ver10 media wsdl
        public MediaAddrs(String media2, String media1) { this.media2 = media2; this.media1 = media1; }
    }

    /** Device:GetServices → Media2/Media1 XAddr 추출 */
    public MediaAddrs getMediaServiceAddrs() throws Exception {
        String body = "<tds:GetServices><tds:IncludeCapability>true</tds:IncludeCapability></tds:GetServices>";
        String xml = soap.postSoap(deviceServiceUrl, body);

        String media2 = findServiceXAddr(xml,
            "http://www.onvif.org/ver20/media/wsdl"); // Media2
        String media1 = findServiceXAddr(xml,
            "http://www.onvif.org/ver10/media/wsdl"); // Media1

        return new MediaAddrs(media2, media1);
    }

    private String findServiceXAddr(String xml, String namespace) {
        // <tds:Service> ... <tds:Namespace>...ns...</tds:Namespace> ... <tds:XAddr>URL</tds:XAddr> ... </tds:Service>
        int from = 0;
        while (true) {
            int s = xml.indexOf("<tds:Service>", from);
            if (s < 0) break;
            int e = xml.indexOf("</tds:Service>", s);
            if (e < 0) break;
            String chunk = xml.substring(s, e);
            if (chunk.contains("<tds:Namespace>" + namespace + "</tds:Namespace>")) {
                int xi = chunk.indexOf("<tds:XAddr>");
                int xj = chunk.indexOf("</tds:XAddr>", xi);
                if (xi > 0 && xj > xi) {
                    return chunk.substring(xi + "<tds:XAddr>".length(), xj).trim();
                }
            }
            from = e + 1;
        }
        return null;
    }

}