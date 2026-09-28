package gestaofinanceira.project.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
     public ErrorResponse tratarIllegalArgumentException(IllegalArgumentException e) {
        return new ErrorResponse(400, e.getMessage());
    }

     @ExceptionHandler(TransacaoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse tratarTransacaoNotFound(TransacaoNotFoundException e) {
        return new ErrorResponse(404, e.getMessage());
    
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ErrorResponse tratarValidacao(MethodArgumentNotValidException e) {
    String mensagem = e.getBindingResult()
            .getFieldErrors()
            .get(0)
            .getDefaultMessage();

    return new ErrorResponse(400, mensagem);
}
}
