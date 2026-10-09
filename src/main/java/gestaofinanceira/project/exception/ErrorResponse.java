package gestaofinanceira.project.exception;

public class ErrorResponse {

    private int status;
    private String mensagem;

    public ErrorResponse(int status, String mensagem) {
        this.status = status;
        this.mensagem = mensagem;
    }

    public int getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }
}
