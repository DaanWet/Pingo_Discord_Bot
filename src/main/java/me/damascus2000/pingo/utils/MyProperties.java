package me.damascus2000.pingo.utils;

import java.util.Properties;

public class MyProperties extends Properties {


    public long get(String key){
        return Long.parseLong(this.getProperty(key));
    }


}
