package kr.co.kworks.goodmorning.utils;

public interface XmlMapper<T> {
    T parse(String xml) throws Exception;
}