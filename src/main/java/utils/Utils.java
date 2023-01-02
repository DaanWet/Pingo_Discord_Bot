package utils;

import commands.settings.Setting;
import companions.uno.UnoGame;
import data.handlers.SettingsDataHandler;

import java.awt.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Utils {

    public static MyProperties config;
    private static Map<Locale, ResourceBundle> locales;
    public static PieceWiseFunction piecewise;
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

    public static void findAvailableLanguages(){
        HashMap<Locale, ResourceBundle> resourceBundles = new HashMap<>();
        for (Locale locale : Locale.getAvailableLocales()){
            try {
                ResourceBundle bundle = ResourceBundle.getBundle("i18n", locale);
                if (bundle.getString("language").equals(locale.toString())){
                    resourceBundles.put(locale, bundle);
                }
            } catch (MissingResourceException ignored){
            }
        }
        locales = Collections.unmodifiableMap(resourceBundles);
    }

    public static Map<Locale, ResourceBundle> getAvailableLanguages(){
        return locales;
    }

    public static MyResourceBundle getLanguage(Long guildId){
        String setting = new SettingsDataHandler().getStringSetting(guildId, Setting.LANGUAGE).get(0);
        return new MyResourceBundle("i18n", new Locale(setting));
    }

    public static String format(ResourceBundle language, String key, Object... args){
        return new MyMessageFormat(language.getString(key)).eformat(args);
    }

    public static void loadProperties() throws Exception{
        config = new MyProperties();
        config.load(new InputStreamReader(Utils.class.getClassLoader().getResourceAsStream("config.properties"), StandardCharsets.UTF_8));
        piecewise = new PieceWiseFunction(9, 1.06, 2, 30);
    }


    private static Point p1 = new Point(500, 3);
    private static Point p2 = new Point(5000, 4);
    private static Point p3  = new Point(10000, 5);
    private static Point p4 = new Point(100000, 12);
    private static Point p5 = new Point(1000000, 25);
    public static int getGameXP(int credits){
        int i = 0;
        if (credits >= p5.x){
            i = p5.y;
        } else if (credits >= p4.x){
            i = (int) formula(p4, p5, credits, true);
        } else if (credits >= p3.x){
            i = (int) formula(p3, p4, credits, false);
        } else if (credits >= p2.x){
            i = (int) formula(p2, p3, credits, false);
        } else if (credits >= p1.x){
            i = (int) formula(p4, p1, credits, false);
        }
        return i;
    }

    private static double formula(Point p1, Point p2, int cr, boolean round){
        double x = ((float)p2.y-p1.y)/(p2.x-p1.x)*(cr - p1.x) + p1.y;
        return round ? Math.round(x) : Math.floor(x);
    }
    public static int getXP(int level){
        return (int) Math.ceil(piecewise.integrate(level));
    }
    public static int getLevel(int xp){
        return (int)Math.floor(piecewise.solveForX(xp));
    }

}
