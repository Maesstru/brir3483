package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.file.FileProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceIntegrationStep2Test {

    private ProductService productService;
    private FileProductRepository repository;
    private final String testFileName = "test_step2_products.txt";

    @BeforeEach
    void setUp() {
        // Asiguram ca fisierul este gol
        File file = new File(testFileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (Exception e) {
        }

        // Initializam componentele reale S si R
        repository = new FileProductRepository(testFileName);
        productService = new ProductService(repository);
    }

    @AfterEach
    void tearDown() {
        File file = new File(testFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    // Test 1 pentru Step 2
    @Test
    void testAddProduct_integrationR_withMockE() {
        // Creare mock pentru Entitate (E)
        Product mockProduct = Mockito.mock(Product.class);

        // Configuram mock-ul pentru a trece de V si pentru a fi procesat de R
        when(mockProduct.getId()).thenReturn(100);
        when(mockProduct.getNume()).thenReturn("Apa Minerala");
        when(mockProduct.getPret()).thenReturn(5.5);
        when(mockProduct.getCategorie()).thenReturn(CategorieBautura.ALL);
        when(mockProduct.getTip()).thenReturn(TipBautura.WATER_BASED);

        // Apelam metoda din S
        productService.addProduct(mockProduct);

        // Verificam ca S a integrat R cu succes, testand efectul real in R
        Product found = repository.findOne(100);
        assertNotNull(found);
        assertEquals(mockProduct, found);

        // Verificam interactiunea mock-ului E
        verify(mockProduct, atLeastOnce()).getId();
        verify(mockProduct, atLeastOnce()).getNume();
        verify(mockProduct, atLeastOnce()).getPret();
    }

    // Test 2 pentru Step 2
    @Test
    void testDeleteProduct_integrationR_withMockE() {
        Product mockProduct = Mockito.mock(Product.class);
        when(mockProduct.getId()).thenReturn(101);
        when(mockProduct.getNume()).thenReturn("Suc");
        when(mockProduct.getPret()).thenReturn(7.0);
        when(mockProduct.getCategorie()).thenReturn(CategorieBautura.JUICE);
        when(mockProduct.getTip()).thenReturn(TipBautura.BASIC);

        productService.addProduct(mockProduct);
        assertEquals(1, productService.getAllProducts().size());

        // Apelam delete din S
        productService.deleteProduct(101);

        // Verificam in R ca stergerea a avut loc
        assertEquals(0, productService.getAllProducts().size());
        assertNull(repository.findOne(101));
    }
}
