package gestaofinanceira.project.dto;

import java.time.LocalDate;

public class InsightDTO {

    private String tipo;          // Ex: "Aumento de gastos", "Queda de renda"
    private String mensagem;      // Ex: "Seus gastos com transporte aumentaram 35%"
    private LocalDate dataGeracao;
    private String nivelAlerta;   // Ex: "Normal", "Atenção", "Risco", "Estourado"

    public InsightDTO() {}

    public InsightDTO(String tipo, String mensagem, LocalDate dataGeracao, String nivelAlerta) {
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.dataGeracao = dataGeracao;
        this.nivelAlerta = nivelAlerta;
    }

    // Getters e Setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public LocalDate getDataGeracao() { return dataGeracao; }
    public void setDataGeracao(LocalDate dataGeracao) { this.dataGeracao = dataGeracao; }

    public String getNivelAlerta() { return nivelAlerta; }
    public void setNivelAlerta(String nivelAlerta) { this.nivelAlerta = nivelAlerta; }
}
