package utils;

public class MessageException extends RuntimeException { // Custom error

    public MessageException(String message) {
      super(message);
    }
}
