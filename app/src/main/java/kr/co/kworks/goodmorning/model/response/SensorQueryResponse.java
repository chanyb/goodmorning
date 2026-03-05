package kr.co.kworks.goodmorning.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SensorQueryResponse {

    @SerializedName("head")
    private Head head;

    @SerializedName("data")
    private List<DataItem> data;

    public Head getHead() {
        return head;
    }

    public List<DataItem> getData() {
        return data;
    }

    // --- 내부 클래스 정의 ---

    public static class Head {
        @SerializedName("transaction")
        private int transaction;

        @SerializedName("signature")
        private int signature;

        @SerializedName("environment")
        private Environment environment;

        @SerializedName("fields")
        private List<Field> fields;

        public int getTransaction() {
            return transaction;
        }

        public int getSignature() {
            return signature;
        }

        public Environment getEnvironment() {
            return environment;
        }

        public List<Field> getFields() {
            return fields;
        }
    }

    public static class Environment {
        @SerializedName("station_name")
        private String stationName;

        @SerializedName("table_name")
        private String tableName;

        @SerializedName("model")
        private String model;

        @SerializedName("serial_no")
        private String serialNo;

        @SerializedName("os_version")
        private String osVersion;

        @SerializedName("prog_name")
        private String progName;

        public String getStationName() {
            return stationName;
        }

        public String getTableName() {
            return tableName;
        }

        public String getModel() {
            return model;
        }

        public String getSerialNo() {
            return serialNo;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public String getProgName() {
            return progName;
        }
    }

    public static class Field {
        @SerializedName("name")
        private String name;

        @SerializedName("type")
        private String type;

        @SerializedName("units")
        private String units; // 있을 수도, 없을 수도 있음

        @SerializedName("process")
        private String process;

        @SerializedName("settable")
        private boolean settable;

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getUnits() {
            return units;
        }

        public String getProcess() {
            return process;
        }

        public boolean isSettable() {
            return settable;
        }
    }

    public static class DataItem {
        @SerializedName("time")
        private String time;

        @SerializedName("no")
        private int no;

        @SerializedName("vals")
        private List<Float> vals;

        public String getTime() {
            return time;
        }

        public int getNo() {
            return no;
        }

        public List<Float> getVals() {
            return vals;
        }
    }
}
