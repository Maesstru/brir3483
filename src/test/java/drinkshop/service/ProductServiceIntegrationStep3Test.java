package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.file.FileProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceIntegrationStep3Test {

    private ProductService productService;
    private FileProductRepository repository;
    private final String testFileName = "test_step3_products.txt";

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

        // Initializam toate componentele reale S, R
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

    // Test 1 pentru Step 3
    @Test
    void testAddProduct_integrationAllReal() {
        // Creare Entitate (E) REALA
        Product realProduct = new Product(10, "Cafea Espresso", 12.5, CategorieBautura.CLASSIC_COFFEE,
                TipBautura.DAIRY);

        // Apelam metoda din S
        productService.addProduct(realProduct);

        // Verificam preluarea din R (se verifica S + R + E)
        Product found = productService.findById(10);
        assertNotNull(found);
        assertEquals("Cafea Espresso", found.getNume());
        assertEquals(12.5, found.getPret());
        assertEquals(CategorieBautura.CLASSIC_COFFEE, found.getCategorie());
    }

    // Test 2 pentru Step 3
    @Test
    void testFilterByCategorie_integrationAllReal() {
        // Adaugam entitati reale
        Product p1 = new Product(11, "Ceai Verde", 10.0, CategorieBautura.TEA, TipBautura.DAIRY);
        Product p2 = new Product(12, "Limonada", 15.0, CategorieBautura.ALL, TipBautura.BASIC);
        Product p3 = new Product(13, "Ceai Negru", 11.0, CategorieBautura.TEA, TipBautura.BASIC);

        productService.addProduct(p1);
        productService.addProduct(p2);
        productService.addProduct(p3);

        // Apelam metoda din S (ce implica R si E reale)
        List<Product> ceaiuri = productService.filterByCategorie(CategorieBautura.TEA);

        // Verificam ca R contine 3 produse in total
        assertEquals(3, productService.getAllProducts().size());

        // Verificam ca S a filtrat corect
        assertEquals(2, ceaiuri.size());
        assertTrue(ceaiuri.contains(p1));
        assertTrue(ceaiuri.contains(p3));
        assertFalse(ceaiuri.contains(p2));
    }
}
