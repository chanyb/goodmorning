package kr.co.kworks.goodmorning.utils;

import org.w3c.dom.*;
import javax.xml.xpath.*;
import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;
import java.util.*;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

public class SoapXmlParser {

    private final Document document;
    private final XPath xpath;

    /* ===============================
     * 생성자 (String XML)
     * =============================== */
    public SoapXmlParser(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // ⭐ 필수

        DocumentBuilder builder = factory.newDocumentBuilder();
        this.document = builder.parse(new InputSource(new StringReader(xml)));

        XPathFactory xPathFactory = XPathFactory.newInstance();
        this.xpath = xPathFactory.newXPath();
        this.xpath.setNamespaceContext(new OnvifNamespaceContext());
    }

    /* ===============================
     * SOAP Body Element 반환
     * =============================== */
    public Element getSoapBody() throws Exception {
        return (Element) xpath.evaluate(
            "/*[local-name()='Envelope']/*[local-name()='Body']",
            document,
            XPathConstants.NODE
        );
    }

    /* ===============================
     * local-name 기반 단일 값
     * =============================== */
    public String getTextByLocalName(String tagName) throws Exception {
        return xpath.evaluate(
            "//*[local-name()='" + tagName + "']",
            document
        );
    }

    /* ===============================
     * namespace + tagName 기반
     * =============================== */
    public String getTextByNamespace(String namespaceUri, String tagName) {
        NodeList list = document.getElementsByTagNameNS(namespaceUri, tagName);
        if (list.getLength() == 0) return null;
        return list.item(0).getTextContent();
    }

    /* ===============================
     * 동일 tag 다건
     * =============================== */
    public List<String> getAllByLocalName(String tagName) throws Exception {
        NodeList list = (NodeList) xpath.evaluate(
            "//*[local-name()='" + tagName + "']",
            document,
            XPathConstants.NODESET
        );

        List<String> result = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            result.add(list.item(i).getTextContent());
        }
        return result;
    }

    /* ===============================
     * 특정 Element 하위 값 추출
     * =============================== */
    public String getChildText(Element parent, String childLocalName) throws Exception {
        return xpath.evaluate(
            ".//*[local-name()='" + childLocalName + "']",
            parent
        );
    }

    /* ===============================
     * ONVIF NamespaceContext
     * =============================== */
    private static class OnvifNamespaceContext implements NamespaceContext {

        private static final Map<String, String> MAP = new HashMap<>();

        static {
            MAP.put("soap", "http://www.w3.org/2003/05/soap-envelope");
            MAP.put("tds",  "http://www.onvif.org/ver10/device/wsdl");
            MAP.put("trt",  "http://www.onvif.org/ver10/media/wsdl");
            MAP.put("tptz", "http://www.onvif.org/ver20/ptz/wsdl");
            MAP.put("tt",   "http://www.onvif.org/ver10/schema");
            MAP.put("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
            MAP.put("wsu",  "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return MAP.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
        }

        @Override public String getPrefix(String namespaceURI) { return null; }
        @Override public Iterator<String> getPrefixes(String namespaceURI) { return null; }
    }

    /* ===============================
     * 특정 tag의 attribute 값 추출
     * =============================== */
    public String getAttributeByLocalName(String tagName, String attrName) throws Exception {
        Node node = (Node) xpath.evaluate(
            "//*[local-name()='" + tagName + "']",
            document,
            XPathConstants.NODE
        );

        if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        Element el = (Element) node;
        return el.getAttribute(attrName);
    }
}