package kr.co.kworks.goodmorning.utils;

public class OnvifMediaProfiles {

    private final OnvifSoapClient soap;

    public OnvifMediaProfiles(OnvifSoapClient soap) {
        this.soap = soap;
    }

    /** Media2(Ver20) → Media1(Ver10) 순서로 시도하여 첫 프로필 토큰 반환 */
    public String getFirstProfileToken(String media2Url, String media1Url) throws Exception {
        // 1) Media2 시도
        if (media2Url != null) {
            String body = "<tr2:GetProfiles xmlns:tr2=\"http://www.onvif.org/ver20/media/wsdl\"/>";
            String xml = soap.postSoap(media2Url, body);
            String token = extractFirstToken(xml);
            if (token != null) return token;
        }
        // 2) Media1 시도
        if (media1Url != null) {
            String body = "<trt:GetProfiles xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\"/>";
            String xml = soap.postSoap(media1Url, body);
            String token = extractFirstToken(xml);
            if (token != null) return token;
        }
        throw new IllegalStateException("profile token not found (Media2/Media1 모두 실패)");
    }

    public String getFirstProfileToken2(String media2Url, String media1Url) throws Exception {
        // 1) Media2 시도
        if (media2Url != null) {
            String body = "<trt:GetProfiles />";
            String xml = soap.postSoap2(media2Url, body);
            String token = extractFirstToken(xml);
            if (token != null) return token;
        }
        // 2) Media1 시도
        if (media1Url != null) {
            String body = "<trt:GetProfiles xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\"/>";
            String xml = soap.postSoap2(media1Url, body);
            String token = extractFirstToken(xml);
            if (token != null) return token;
        }
        throw new IllegalStateException("profile token not found (Media2/Media1 모두 실패)");
    }

    /** 응답에서 token="...” 을 관대하게 추출 (네임스페이스/태그명 차이 무시) */
    private static String extractFirstToken(String xml) {
        // 흔히 <tr2:Profiles token="..."> 또는 <trt:Profiles token="...">, <tt:Profile token="..."> 등 다양
        int i = xml.indexOf(" token=\"");
        if (i < 0) i = xml.indexOf(" token='");
        if (i < 0) return null;

        int qStart = xml.indexOf('"', i);
        char quote = '"';
        if (qStart < 0) {
            qStart = xml.indexOf('\'', i);
            quote = '\'';
        }
        if (qStart < 0) return null;
        int qEnd = xml.indexOf(quote, qStart + 1);
        if (qEnd < 0) return null;
        return xml.substring(qStart + 1, qEnd);
    }
}