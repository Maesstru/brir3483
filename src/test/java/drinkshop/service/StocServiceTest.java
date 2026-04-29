package drinkshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import drinkshop.domain.IngredientReteta;
import drinkshop.domain.Reteta;
import drinkshop.domain.Stoc;
import drinkshop.repository.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StocServiceTest {

    @Mock
    private Repository<Integer, Stoc> stocRepo;

    private StocService service;

    @BeforeEach
    void setUp() {
        // Folosim spy pentru a putea "fenta" (stub) metoda interna areSuficient()
        service = spy(new StocService(stocRepo));
    }

    @Test
    void F02_TC01_stocInsuficient_aruncaExceptie() {
        // Arrange
        Reteta reteta = retetaCuIngrediente(ingredientReteta("zahar", 2.0));
        doReturn(false).when(service).areSuficient(reteta);

        // Act + Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.consuma(reteta)
        );

        assertEquals("Stoc insuficient pentru rețeta.", ex.getMessage());
        verify(stocRepo, never()).update(any());
    }

    @Test
    void F02_TC02_retetaFaraIngrediente_terminareNormala() {
        // Arrange
        Reteta reteta = retetaCuIngrediente(); // Lista goala
        doReturn(true).when(service).areSuficient(reteta);

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        verify(stocRepo, never()).update(any());
    }

    @Test
    void F02_TC03_zaharSiCacao_UpdateUnLot() {
        // Arrange
        Reteta reteta = retetaCuIngrediente(
                ingredientReteta("zahar", 2.0),
                ingredientReteta("cacao", 0.0)
        );
        Stoc lotZahar = stoc(1, "zahar", 5, 1);
        Stoc lotLapte = stoc(2, "lapte", 4, 1);

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of(lotZahar, lotLapte));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(3.0, lotZahar.getCantitate(), 0.0001);
        assertEquals(4.0, lotLapte.getCantitate(), 0.0001); // Ramane neschimbat
        verify(stocRepo, times(1)).update(lotZahar);
        verify(stocRepo, never()).update(lotLapte);
    }

    @Test
    void F02_TC04_fainaConsumDinDouaLoturi() {
        // Arrange
        Reteta reteta = retetaCuIngrediente(ingredientReteta("faina", 5.0));
        Stoc lot1 = stoc(1, "faina", 2, 1);
        Stoc lot2 = stoc(2, "faina", 3, 1);

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of(lot1, lot2));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(0.0, lot1.getCantitate(), 0.0001);
        assertEquals(0.0, lot2.getCantitate(), 0.0001);
        verify(stocRepo, times(1)).update(lot1);
        verify(stocRepo, times(1)).update(lot2);
    }

    @Test
    void F02_TC05_sareTreiLoturi_ConsumaDoua_ExecutaBreak() {
        // Arrange
        Reteta reteta = retetaCuIngrediente(ingredientReteta("sare", 5.0));
        Stoc lot1 = stoc(1, "sare", 2, 1);
        Stoc lot2 = stoc(2, "sare", 3, 1);
        Stoc lot3 = stoc(3, "sare", 10, 1);

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of(lot1, lot2, lot3));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(0.0, lot1.getCantitate(), 0.0001);
        assertEquals(0.0, lot2.getCantitate(), 0.0001);
        assertEquals(10.0, lot3.getCantitate(), 0.0001); // Al treilea ramane neschimbat
        verify(stocRepo, times(1)).update(lot1);
        verify(stocRepo, times(1)).update(lot2);
        verify(stocRepo, never()).update(lot3);
    }

    @Test
    void F02_TC06_necesarZero_BreakImediat() {
        // Arrange
        Reteta reteta = retetaCuIngrediente(ingredientReteta("zahar", 0.0));
        Stoc lot1 = stoc(1, "zahar", 5, 1);

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of(lot1));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(5.0, lot1.getCantitate(), 0.0001);
        verify(stocRepo, never()).update(any());
    }

    @Test
    void F02_TC07_ingredientInexistentInStoc_NecesarZero() {
        // Arrange
        Reteta reteta = retetaCuIngrediente(ingredientReteta("cacao", 0.0));
        Stoc lotZahar = stoc(1, "zahar", 5, 1);
        Stoc lotLapte = stoc(2, "lapte", 2, 1);

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of(lotZahar, lotLapte));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(5.0, lotZahar.getCantitate(), 0.0001);
        assertEquals(2.0, lotLapte.getCantitate(), 0.0001);
        verify(stocRepo, never()).update(any());
    }

    @Test
    void F02_TC08_caseInsensitiveCheck() {
        // Arrange
        Reteta reteta = retetaCuIngrediente(ingredientReteta("zahar", 3.0));
        Stoc lotZaharMajuscule = stoc(1, "ZAHAR", 4, 1);

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of(lotZaharMajuscule));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(1.0, lotZaharMajuscule.getCantitate(), 0.0001);
        verify(stocRepo, times(1)).update(lotZaharMajuscule);
    }

    @Test
    void F02_TC09_consumCompletUnLot() {
        // Arrange
        Reteta reteta = retetaCuIngrediente(ingredientReteta("faina", 10.0));
        Stoc lotFaina = stoc(1, "faina", 10, 1);

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of(lotFaina));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(0.0, lotFaina.getCantitate(), 0.0001);
        verify(stocRepo, times(1)).update(lotFaina);
    }

    @Test
    void F02_TC10_multipleIngrediente_MultipleLoturi() {
        // Arrange
        Reteta reteta = retetaCuIngrediente(
                ingredientReteta("oua", 3.0),
                ingredientReteta("lapte", 4.0),
                ingredientReteta("unt", 1.0)
        );

        Stoc lotOua1 = stoc(1, "oua", 2, 1);
        Stoc lotOua2 = stoc(2, "oua", 2, 1);
        Stoc lotLapte = stoc(3, "lapte", 5, 1);
        Stoc lotUnt = stoc(4, "unt", 1, 1);

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of(lotOua1, lotOua2, lotLapte, lotUnt));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(0.0, lotOua1.getCantitate(), 0.0001);
        assertEquals(1.0, lotOua2.getCantitate(), 0.0001);
        assertEquals(1.0, lotLapte.getCantitate(), 0.0001);
        assertEquals(0.0, lotUnt.getCantitate(), 0.0001);

        verify(stocRepo, times(1)).update(lotOua1);
        verify(stocRepo, times(1)).update(lotOua2);
        verify(stocRepo, times(1)).update(lotLapte);
        verify(stocRepo, times(1)).update(lotUnt);
    }

    // =========================
    // Helper Methods
    // =========================

    private Reteta retetaCuIngrediente(IngredientReteta... ingrediente) {
        return new Reteta(1, List.of(ingrediente));
    }

    private IngredientReteta ingredientReteta(String denumire, double cantitate) {
        return new IngredientReteta(denumire, cantitate);
    }

    private Stoc stoc(int id, String ingredient, int cantitate, int stocMinim) {
        return new Stoc(id, ingredient, cantitate, stocMinim);
    }
}