package drinkshop.domain;

import drinkshop.repository.file.FileProductRepository;
import drinkshop.service.ProductService;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService setupService() {
        String testFileName = "test_products.txt";
        File testFile = new File(testFileName);
        try {
            if (testFile.exists()) {
                testFile.delete();
            }
            testFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FileProductRepository repo = new FileProductRepository(testFileName);
        return new ProductService(repo);
    }

    @ParameterizedTest
    @DisplayName("Teste Valide ECP & BVA - Produsul (Băutura) se salvează")
    @CsvSource({
            "10, Flat White, 15.5",   // TC01 (Valid ECP)
            "100, Apa Minerala, 0.01", // TC06 (Valid BVA - pret limita inferioara)
            "1, Espresso Scurt, 8.0"   // TC09 (Valid BVA - ID limita inferioara)
    })
    void testAddProduct_Valid_TC01_TC06_TC09(int id, String nume, double pret) {
        // 1. ARRANGE
        ProductService service = setupService();
        Product p = new Product(id, nume, pret, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        // 2. ACT
        service.addProduct(p);

        // 3. ASSERT
        System.out.println("Succes! Produsul adăugat este: " + service.findById(id).toString());

        assertEquals(1, service.getAllProducts().size());
        assertNotNull(service.findById(id));
    }

    @ParameterizedTest
    @DisplayName("Teste Invalide Preț ECP & BVA - Se așteaptă eroare")
    @CsvSource({
            "11, Americano, -5.0",    // TC02 (Invalid ECP - pret negativ)
            "101, Ceai de Fructe, 0.0", // TC07 (Invalid BVA - pret zero)
            "102, Frappe, -0.01"      // TC08 (Invalid BVA - pret sub limita)
    })
    void testAddProduct_InvalidPrice_TC02_TC07_TC08(int id, String nume, double pret) {
        // 1. ARRANGE
        ProductService service = setupService();
        Product p = new Product(id, nume, pret, CategorieBautura.ICED_COFFEE, TipBautura.WATER_BASED);

        // 2. ACT
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.addProduct(p);
        });

        // 3. ASSERT
        System.out.println("Pentru ID=" + id + " si Pret=" + pret + " s-a returnat eroare: " + exception.getMessage().trim());
        assertEquals(0, service.getAllProducts().size());
    }

    @ParameterizedTest
    @DisplayName("Teste Invalide ID ECP & BVA - Se așteaptă eroare")
    @CsvSource({
            "-5, Iced Matcha, 18.0", // TC04 (Invalid ECP - ID negativ)
            "0, Fresh Portocale, 14.0" // TC10 (Invalid BVA - ID zero)
    })
    void testAddProduct_InvalidID_TC04_TC10(int id, String nume, double pret) {
        // 1. ARRANGE
        ProductService service = setupService();
        Product p = new Product(id, nume, pret, CategorieBautura.JUICE, TipBautura.BASIC);

        // 2. ACT
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.addProduct(p);
        });

        // 3. ASSERT
        System.out.println("Pentru ID=" + id + " si Nume='" + nume + "' s-a returnat eroare: " + exception.getMessage().trim());
        assertEquals(0, service.getAllProducts().size(), "Băutura NU trebuia salvată.");
    }

    @ParameterizedTest
    @DisplayName("Test Invalid Nume ECP - Se așteaptă eroare")
    @CsvSource({
            "12, '', 12.0" // TC03 (Invalid ECP - Nume gol)
    })
    void testAddProduct_InvalidName_TC03(int id, String nume, double pret) {
        // 1. ARRANGE
        ProductService service = setupService();
        Product p = new Product(id, nume, pret, CategorieBautura.MILK_COFFEE, TipBautura.DAIRY);

        // 2. ACT
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.addProduct(p);
        });

        // 3. ASSERT
        System.out.println("Pentru ID=" + id + " si Nume= " + nume + " s-a returnat eroare: " + exception.getMessage().trim());
        assertEquals(0, service.getAllProducts().size());
    }

    @ParameterizedTest
    @DisplayName("Test Invalid Tip de Date - TC05 (Eroare de parsare)")
    @ValueSource(strings = {"gratis"})
    void testAddProduct_InvalidDataType_TC05(String pretInvalid) {
        NumberFormatException exception = assertThrows(NumberFormatException.class, () -> {
            Double.parseDouble(pretInvalid);
        });

        System.out.println("Pentru introducerea textului '" + pretInvalid + "' ca pret, sistemul a aruncat: " + exception.toString());
    }
}