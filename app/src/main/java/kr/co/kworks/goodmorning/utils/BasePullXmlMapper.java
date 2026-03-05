package kr.co.kworks.goodmorning.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

public abstract class BasePullXmlMapper<T> implements XmlMapper<T> {

    @Override
    public final T parse(String xml) throws Exception {
        XmlPullParser xpp = newParser(xml);
        return parse(xpp);
    }

    protected abstract T parse(XmlPullParser xpp) throws Exception;

    protected XmlPullParser newParser(String xml) throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(xml));
        return xpp;
    }

    /** 현재 START_TAG 위치에서 그 태그의 "직접 텍스트"를 읽어서 리턴 */
    protected String readText(XmlPullParser xpp) throws Exception {
        // START_TAG 다음이 TEXT일 수도 있고, 바로 END_TAG일 수도 있음
        String result = "";
        if (xpp.next() == XmlPullParser.TEXT) {
            result = xpp.getText();
            xpp.nextTag(); // TEXT 뒤의 END_TAG로 이동
        }
        return result == null ? "" : result.trim();
    }

    /** 관심 없는 태그(하위 포함)를 통째로 스킵 */
    protected void skipTag(XmlPullParser xpp) throws Exception {
        if (xpp.getEventType() != XmlPullParser.START_TAG) return;

        int depth = 1;
        while (depth != 0) {
            int event = xpp.next();
            if (event == XmlPullParser.START_TAG) depth++;
            else if (event == XmlPullParser.END_TAG) depth--;
        }
    }

    protected long toLong(String s, long def) {
        try { return Long.parseLong(s); } catch (Exception e) { return def; }
    }

    protected int toInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    protected double toDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }

    protected boolean isStartTag(XmlPullParser xpp, String name) throws XmlPullParserException {
        return xpp.getEventType() == XmlPullParser.START_TAG && name.equals(xpp.getName());
    }

    protected boolean isEndTag(XmlPullParser xpp, String name) throws XmlPullParserException {
        return xpp.getEventType() == XmlPullParser.END_TAG && name.equals(xpp.getName());
    }
}