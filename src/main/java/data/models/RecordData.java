package data.models;

public class RecordData {

    private long userId;
    private String record;
    private double value;
    private String link;

    public RecordData(){}

    public RecordData(long userId, String record, double value, String link){
        this.userId = userId;
        this.record = record;
        this.value = value;
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }
}
