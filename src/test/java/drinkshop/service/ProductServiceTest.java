package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private Repository<Integer, Product> productRepo;

    @Mock
    private Product productMock;

    @InjectMocks
    private ProductService productService;

    // Test 1: verify (se verifica interactiunea cu mock-urile)
    @Test
    void testAddProduct_withValidMockEntity_verifiesCalls() {
        // Configurarea mock-ului pentru Entitate (E) pentru a trece de Validator (V)
        when(productMock.getId()).thenReturn(1);
        when(productMock.getNume()).thenReturn("Cola");
        when(productMock.getPret()).thenReturn(5.0);

        // Apelam metoda din Service (S)
        productService.addProduct(productMock);

        // Verificam ca Validator-ul a apelat getter-ele pe mock-ul Entitatii (E)
        verify(productMock, atLeastOnce()).getId();
        verify(productMock, atLeastOnce()).getNume();
        verify(productMock, atLeastOnce()).getPret();

        // Verificam ca Repository-ul (R) a salvat mock-ul Entitatii
        verify(productRepo, times(1)).save(productMock);
    }

    // Test 2: assert (se verifica rezultatul)
    @Test
    void testFilterByCategorie_withMocks_assertResult() {
        // Configurarea comportamentului pentru Repository si Entitate
        when(productMock.getCategorie()).thenReturn(CategorieBautura.CLASSIC_COFFEE);
        when(productRepo.findAll()).thenReturn(Arrays.asList(productMock));

        // Apelam metoda din Service
        List<Product> result = productService.filterByCategorie(CategorieBautura.CLASSIC_COFFEE);

        // Evaluare cu assert pentru a ne asigura de corectitudinea filtrarii
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productMock, result.get(0));

        // Verificam interactiunea (optional, dar util pentru vizibilitate)
        verify(productRepo, times(1)).findAll();
        verify(productMock, atLeastOnce()).getCategorie();
    }

    // Test 3: verificam o exceptie din validator
    @Test
    void testAddProduct_withInvalidMockEntity_throwsException() {
        // Configurarea mock-ului pentru a esua validarea (nume gol)
        when(productMock.getId()).thenReturn(1);
        when(productMock.getNume()).thenReturn("");

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.addProduct(productMock);
        });

        assertTrue(exception.getMessage().contains("Numele nu poate fi gol"));

        // Repository-ul NU ar trebui sa fie apelat
        verify(productRepo, never()).save(any(Product.class));
    }
}
