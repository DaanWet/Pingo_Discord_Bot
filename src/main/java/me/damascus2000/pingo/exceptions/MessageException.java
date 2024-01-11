package me.damascus2000.pingo.exceptions;

public class MessageException extends RuntimeException { // Custom error

    protected final int delete;

    protected MessageException(){
        this.delete = 0;
    }

    protected MessageException(int delete){
        this.delete = delete;
    }

    public MessageException(String message, int delete){
        super(message);
        this.delete = delete;
    }

    public MessageException(String message){
        this(message, 0);
    }

    public int getDelete(){
        return delete;
    }
}
