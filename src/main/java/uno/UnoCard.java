package uno;

public class UnoCard {

    public enum Color {
        RED("Red", "r"),
        BLUE("Blue", "b"),
        GREEN("Green", "g"),
        YELLOW("Yellow", "y");

        public String getName() {
            return name;
        }

        public String getToken() {
            return token;
        }

        private String name;
        private String token;

        Color(String name, String token){
            this.name = name;
            this.token = token;
        }
    }

    public enum Value{
        ZERO("zero", "0", 0),
        ONE("one", "1", 1),
        TWO("two", "2", 2),
        THREE("tree", "3", 3),
        FOUR("four", "4", 4),
        FIVE("five", "5", 5),
        SIX("six", "6", 6),
        SEVEN("seven", "7", 7),
        EIGHT("eight", "8", 8),
        NINE("nine", "9", 9),
        //TEN("ten", "10", 10),
        REVERSE("change", "n", 20),
        PLUSTWO("Plus Two", "t", 20),
        SKIP("Skip", "s", 20),
        PLUSFOUR("Plus Four", "f", 50),
        COLOR("Change color", "c", 50);

        private String name;
        private String token;
        private int value;

        public String getName() {
            return name;
        }

        public String getToken() {
            return token;
        }

        public int getValue() {
            return value;
        }

        Value(String name, String token, int value){
            this.name = name;
        }



    }

    private Color color;
    private final Value value;

    public UnoCard(Color color, Value value){
        this.color = color;
        this.value = value;
    }

    public static UnoCard fromString(String card){
        Color color = null;
        card = card.toLowerCase();
        for (Color c : Color.values()){
            if (card.contains(c.getToken()) || card.contains(c.getName().toLowerCase())){
                color = c;
            }
        }
        if (color == null) return null;
        Value value = null;
        for (Value v : Value.values()){
            if (card.contains(v.getToken()) || card.contains(v.getName().toLowerCase())){
                value = v;
            }
        }
        if (value == null) return null;
        return new UnoCard(color, value);


    }


    public boolean canBePlayed(UnoCard card){
        return card.value == Value.PLUSFOUR || card.value == Value.COLOR || this.color == card.color || this.value == card.value;
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Value getValue() {
        return value;
    }

    public boolean equals(UnoCard othercard){
        return (this.value == othercard.value && this.color == othercard.color) || (this.value == Value.COLOR && othercard.value == Value.COLOR) || (this.value == Value.PLUSFOUR && othercard.value == Value.PLUSFOUR);
    }


}
