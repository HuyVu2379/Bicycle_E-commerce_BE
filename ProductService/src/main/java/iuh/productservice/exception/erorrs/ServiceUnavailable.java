package iuh.productservice.exception.erorrs;

public class ServiceUnavailable extends RuntimeException{
    public ServiceUnavailable(String message) {
        super(message);
    }
}
