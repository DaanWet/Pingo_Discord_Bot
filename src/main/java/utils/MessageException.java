package utils;

public class MessageException extends RuntimeException { // Custom error

    protected final int delete;

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
