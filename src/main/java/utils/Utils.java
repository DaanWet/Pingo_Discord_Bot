package utils;

import casino.uno.UnoGame;

public class Utils {

    public static boolean isInteger(String s) {
        if (s == null) {
            return false;
        }
        int length = s.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (s.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static Long isLong(String s) {
        Long l = null;
        try {
            l = Long.parseLong(s);
        } catch (Exception ignored) {
        }
        return l;
    }

    public static int getInt(String s) {
        if (s.matches("(?i)[0-9]*k?m?")) {
            s = s.replaceAll("m", "000000");
            s = s.replaceAll("k", "000");
        }
        int i;
        try {
            i = Integer.parseInt(s);
        } catch (Exception e) {
            i = -1;
        }
        return i;
    }

    public static boolean isBetween(UnoGame game, int turn, int between) {
        int one = game.isClockwise() ? 1 : -1;
        int newturn = game.getTurn();
        int x1 = (turn + one) % game.getHands().size();
        if (x1 < 0) x1 += game.getHands().size();
        int x2 = (newturn - one) % game.getHands().size();
        if (x2 < 0) x2 += game.getHands().size();
        return between == x1 && between == x2;
    }

    public static String upperCaseFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String regionalEmoji(int i) {
        char x = (char) (97 + i);
        return String.format(":regional_indicator_%c:", x);
    }

    public static String regionalUnicode(int i){
        int base = 230 + i;
        String s = Integer.toHexString(base);
        return "U+1f1" + s;
    }

}
