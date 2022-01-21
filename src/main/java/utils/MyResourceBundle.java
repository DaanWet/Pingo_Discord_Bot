package utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class MyResourceBundle {

    private final ResourceBundle bundle;

    public MyResourceBundle(String name, Locale locale){
        bundle = ResourceBundle.getBundle(name, locale);
    }

    public String getString(String key){
        return bundle.getString(key);
    }

    public String getString(String key, Object... args){
        return new MyMessageFormat(bundle.getString(key)).eformat(args);
    }
}
