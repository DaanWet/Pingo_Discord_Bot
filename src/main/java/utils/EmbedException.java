package utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class EmbedException extends MessageException {

    private String description;

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
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(this.getMessage());
        builder.setDescription(description);
        builder.setColor(Color.RED);
        return builder;
    }

}
