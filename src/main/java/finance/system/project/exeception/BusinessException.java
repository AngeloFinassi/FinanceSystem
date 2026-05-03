package finance.system.project.exeception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}