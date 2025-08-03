package boombim.global.infra.exception.error;


public class BoombimException extends RuntimeException {

    private final ErrorCode errorCode;

    public BoombimException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BoombimException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getMessage() + " → " + detailMessage);
        this.errorCode = errorCode;
    }


    public BoombimException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatusCode() {
        return errorCode.getHttpCode();
    }
}
