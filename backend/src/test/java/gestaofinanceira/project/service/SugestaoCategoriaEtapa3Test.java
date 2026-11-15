
package gestaofinanceira.project.service;

import gestaofinanceira.project.repository.CategoriaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SugestaoCategoriaEtapa3Test {

    @Mock
    CategoriaRepository categoriaRepository;

    @InjectMocks
    CategoriaService categoriaService;

    @Test
    void deveSugerirCategoriasCorretamente() {

        // 1. PREPARAR E EXECUTAR
        String netflix =
            categoriaService.sugerirCategoria("Netflix");

        String restaurante =
            categoriaService.sugerirCategoria("McDonald's");

        String posto =
            categoriaService.sugerirCategoria("Posto Shell");

        String farmacia =
            categoriaService.sugerirCategoria("Farmácia São João");

        String desconhecido =
            categoriaService.sugerirCategoria("Loja desconhecida");

        // 2. VERIFICAR
        assertEquals("Assinaturas", netflix);
        assertEquals("Alimentação", restaurante);
        assertEquals("Transporte", posto);
        assertEquals("Saúde", farmacia);
        assertEquals("Outros", desconhecido);
    }
}
