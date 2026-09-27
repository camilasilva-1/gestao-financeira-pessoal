package gestaofinanceira.project.exception;

public class TransacaoNotFoundException extends RuntimeException {
      public TransacaoNotFoundException(String mensagem) {
        super(mensagem);
    }
}
