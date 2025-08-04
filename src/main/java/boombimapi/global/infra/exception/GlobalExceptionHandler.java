package boombimapi.global.infra.exception;


import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.infra.exception.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BoombimException.class)
    public ResponseEntity<ErrorResponse> handleFlowException(BoombimException e) {
        log.error("BoombimException caught - ErrorCode: {}, Message: {}",
                e.getErrorCode(), e.getMessage());
        return ResponseEntity
                .status(e.getHttpStatusCode())
                .body(ErrorResponse.of(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        log.error("Unexpected exception caught: ", e);
        return ResponseEntity
                .status(500)
                .body(ErrorResponse.of(ErrorCode.SERVER_UNTRACKED_ERROR));
    }

    // ENUM 클래스 예외 잡을려고 넣었습니다.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseError(HttpMessageNotReadableException e) {
        Throwable rootCause = e.getRootCause();
        if (rootCause instanceof BoombimException babException) {
            return ResponseEntity
                    .status(babException.getHttpStatusCode())
                    .body(ErrorResponse.of(babException));
        }

        return ResponseEntity
                .status(400)
                .body(ErrorResponse.of(ErrorCode.PARAMETER_GRAMMAR_ERROR, rootCause != null ? rootCause.getMessage() : "잘못된 요청입니다."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }
}
