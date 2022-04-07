package utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class EmbedException extends MessageException {

    private String description;
    private EmbedBuilder eb;

    public EmbedException(EmbedBuilder eb){
        super();
        this.eb = eb;
    }

    public EmbedException(EmbedBuilder eb, int delete){
        super(delete);
        this.eb = eb;
    }

    public EmbedException(String message, String description, int delete){
        super(message, delete);
        this.description = description;
    }

    public EmbedException(String message, int delete){
        super(message, delete);
    }

    public EmbedException(String message, String description){
        super(message);
        this.description = description;
    }

    public EmbedException(String message){
        super(message);
    }


    public EmbedBuilder getEmbed(){
        if (eb == null){
            createEmbed();
        }
        return eb;
    }

    private void createEmbed(){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(this.getMessage());
        builder.setDescription(description);
        builder.setColor(Color.RED);
        this.eb = builder;
    }
}
