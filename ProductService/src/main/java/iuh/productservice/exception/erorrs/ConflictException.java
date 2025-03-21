package iuh.productservice.exception.erorrs;

public class ConflictException extends RuntimeException{
    public ConflictException(String message) {
        super(message);
    }
}
