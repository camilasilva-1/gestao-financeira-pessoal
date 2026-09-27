package gestaofinanceira.project.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String tratarIllegalArgumentException(IllegalArgumentException e) {
        return e.getMessage();
    }

    @ExceptionHandler(TransacaoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String tratarTransacaoNotFound(TransacaoNotFoundException e) {
        return e.getMessage();
    }

}
