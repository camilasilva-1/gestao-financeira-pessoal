package gestaofinanceira.project.dto;

public class ScoreFinanceiroDTO {

    private Integer score;
    private String nivel;
    private String descricao;

    public ScoreFinanceiroDTO() {
    }

    public ScoreFinanceiroDTO(
            Integer score,
            String nivel,
            String descricao) {

        this.score = score;
        this.nivel = nivel;
        this.descricao = descricao;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}