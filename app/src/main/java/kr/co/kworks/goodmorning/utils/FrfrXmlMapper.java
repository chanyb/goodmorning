package kr.co.kworks.goodmorning.utils;

import org.xmlpull.v1.XmlPullParser;

import kr.co.kworks.goodmorning.model.business_logic.FrfrData;
import kr.co.kworks.goodmorning.model.business_logic.FrfrRoot;

public final class FrfrXmlMapper extends BasePullXmlMapper<FrfrRoot> {

    @Override
    protected FrfrRoot parse(XmlPullParser xpp) throws Exception {
        FrfrRoot root = new FrfrRoot();

        // 문서 시작까지 이동
        int event = xpp.getEventType();
        while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT) {
            event = xpp.next();
        }

        // <ROOT> 들어가기
        while (!(xpp.getEventType() == XmlPullParser.START_TAG && "ROOT".equals(xpp.getName()))) {
            if (xpp.next() == XmlPullParser.END_DOCUMENT) return root;
        }

        // ROOT 내부 순회
        while (xpp.next() != XmlPullParser.END_DOCUMENT) {
            if (isStartTag(xpp, "DATA")) {
                root.dataList.add(parseData(xpp)); // xpp는 DATA START_TAG 위치
            } else if (isEndTag(xpp, "ROOT")) {
                break;
            }
        }

        return root;
    }

    private FrfrData parseData(XmlPullParser xpp) throws Exception {
        FrfrData d = new FrfrData();

        // DATA 내부 순회: 다음 이벤트부터 시작
        while (xpp.next() != XmlPullParser.END_DOCUMENT) {

            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                String tag = xpp.getName();

                switch (tag) {
                    case "frfrInfoId":
                        d.frfrInfoId = toLong(readText(xpp), 0L);
                        break;

                    case "frfrOccrrTpcd":
                        d.frfrOccrrTpcd = readText(xpp);
                        break;

                    case "frfrOccrrTpcdNm":
                        d.frfrOccrrTpcdNm = readText(xpp);
                        break;

                    case "frfrPrgrsStcd":
                        d.frfrPrgrsStcd = readText(xpp);
                        break;

                    case "frfrPrgrsStcdNm":
                        d.frfrPrgrsStcdNm = readText(xpp);
                        break;

                    case "frfrStepIssuCd":
                        d.frfrStepIssuCd = readText(xpp);
                        break;

                    case "frfrSttmnAddr":
                        d.frfrSttmnAddr = readText(xpp);
                        break;

                    case "frfrSttmnDt":
                        d.frfrSttmnDt = readText(xpp);
                        break;

                    case "frfrSttmnHms":
                        d.frfrSttmnHms = readText(xpp);
                        break;

                    case "frfrSttmnLctnXcrd":
                        d.frfrSttmnLctnXcrd = toDouble(readText(xpp), 0.0);
                        break;

                    case "frfrSttmnLctnYcrd":
                        d.frfrSttmnLctnYcrd = toDouble(readText(xpp), 0.0);
                        break;

                    default:
                        // 혹시 중첩 태그가 나오면 안전하게 스킵
                        skipTag(xpp);
                        break;
                }

            } else if (isEndTag(xpp, "DATA")) {
                break;
            }
        }

        return d;
    }
}