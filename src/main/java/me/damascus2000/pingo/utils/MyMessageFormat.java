package me.damascus2000.pingo.utils;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Locale;

public class MyMessageFormat extends MessageFormat {
    public MyMessageFormat(@NotNull String pattern){
        super(pattern);
    }

    public MyMessageFormat(@NotNull String pattern, Locale locale){
        super(pattern, locale);
    }

    public String eformat(Object... args){
        return super.format(args);
    }


}
