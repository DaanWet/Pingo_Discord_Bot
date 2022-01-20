package utils;

import commands.settings.Setting;
import companions.uno.UnoGame;
import data.DataHandler;

import java.text.MessageFormat;
import java.util.*;

public class Utils {

    public static boolean isInteger(String s){
        if (s == null){
            return false;
        }
        int length = s.length();
        if (length == 0){
            return false;
        }
        int i = 0;
        if (s.charAt(0) == '-'){
            if (length == 1){
                return false;
            }
            i = 1;
        }
        for (; i < length; i++){
            char c = s.charAt(i);
            if (c < '0' || c > '9'){
                return false;
            }
        }
        return true;
    }

    public static Long isLong(String s){
        Long l = null;
        try {
            l = Long.parseLong(s);
        } catch (Exception ignored){
        }
        return l;
    }

    public static int getInt(String s){
        if (s.matches("(?i)[0-9]*k?m?")){
            s = s.replaceAll("m", "000000");
            s = s.replaceAll("k", "000");
        }
        int i;
        try {
            i = Integer.parseInt(s);
        } catch (Exception e){
            i = -1;
        }
        return i;
    }

    public static boolean isBetween(UnoGame game, int turn, int between){
        int one = game.isClockwise() ? 1 : -1;
        int newturn = game.getTurn();
        int x1 = (turn + one) % game.getHands().size();
        if (x1 < 0) x1 += game.getHands().size();
        int x2 = (newturn - one) % game.getHands().size();
        if (x2 < 0) x2 += game.getHands().size();
        return between == x1 && between == x2;
    }

    public static String upperCaseFirst(String string){
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String regionalEmoji(int i){
        char x = (char) (97 + i);
        return String.format(":regional_indicator_%c:", x);
    }

    public static String regionalUnicode(int i){
        int base = 230 + i;
        String s = Integer.toHexString(base);
        return "U+1f1" + s;
    }

    public static String concat(String[] strings, int index){
        return concat(strings, index, " ");
    }

    public static String concat(String[] strings, int index, String concat){
        assert (index < strings.length);
        StringBuilder string = new StringBuilder();
        string.append(strings[index]);
        for (int i = index + 1; i < strings.length; i++){
            string.append(concat).append(strings[i]);
        }
        return string.toString();
    }

    private static Map<Locale, ResourceBundle> locales;

    public static void findAvailableLanguages(){
        HashMap<Locale, ResourceBundle> resourceBundles = new HashMap<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle("i18n", locale);
                if (bundle.getString("language").equals(locale.toString())){
                    resourceBundles.put(locale, bundle);
                }
            } catch (MissingResourceException ignored) {
            }
        }
        locales = Collections.unmodifiableMap(resourceBundles);
    }

    public static Map<Locale, ResourceBundle> getAvailableLanguages(){
        return locales;
    }

    public static MyResourceBundle getLanguage(Long guildId){
        String setting = new DataHandler().getStringSetting(guildId, Setting.LANGUAGE).get(0);
        return new MyResourceBundle("i18n", new Locale(setting));
    }

    public static String format(ResourceBundle language, String key, Object... args){
        return new MyMessageFormat(language.getString(key)).eformat(args);
    }
}
