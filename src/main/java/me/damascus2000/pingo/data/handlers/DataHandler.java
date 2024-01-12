package me.damascus2000.pingo.data.handlers;

import java.util.Properties;

@Deprecated(forRemoval = true)
public abstract class DataHandler {


    protected static String JDBC_URL;
    protected static String USER_ID;
    protected static String PASSWD;
    protected final Properties properties;
    protected final Properties nomultiproperties;


    public DataHandler(){
        properties = getProperties();
        nomultiproperties = getMultiProperties(properties);
    }

    public static Properties getProperties(){
        Properties properties = new Properties();
        properties.setProperty("user", USER_ID);
        properties.setProperty("password", PASSWD);
        properties.setProperty("allowMultiQueries", "true");
        properties.setProperty("characterEncoding", "utf8");
        properties.setProperty("CharSet", "utf8mb4");
        properties.setProperty("useUnicode", "true");
        return properties;
    }

    public static Properties getMultiProperties(Properties properties){
        Properties nomultiproperties = new Properties(properties);
        nomultiproperties.setProperty("allowMultiQueries", "false");
        return nomultiproperties;
    }


    public static void setUserId(String userId){
        USER_ID = userId;
    }

    public static void setPASSWD(String passwd){
        PASSWD = passwd;
    }

    public static void setJdbcUrl(String jdbcUrl){
        JDBC_URL = jdbcUrl;
    }


}
