package me.damascus2000.pingo.data.models;

import me.damascus2000.pingo.companions.Record;

public class RecordData {

    private long userId;
    private Record record;
    private double value;
    private String link;

    public RecordData(){}

    public RecordData(long userId, Record record, double value, String link){
        this.userId = userId;
        this.record = record;
        this.value = value;
        this.link = link;
    }

    public String getLink(){
        return link;
    }

    public void setLink(String link){
        this.link = link;
    }

    public double getValue(){
        return value;
    }

    public void setValue(double value){
        this.value = value;
    }

    public long getUserId(){
        return userId;
    }

    public void setUserId(long userId){
        this.userId = userId;
    }

    public Record getRecord(){
        return record;
    }

    public void setRecord(Record record){
        this.record = record;
    }
}
