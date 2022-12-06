package utils;

import java.util.Properties;

public class MyProperties extends Properties {


    public long get(String key){
        return Long.parseLong(this.getProperty(key));
    }


}
