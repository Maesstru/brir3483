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
        service = spy(new StocService(stocRepo));
    }

    @Test
    void TC01_stocInsuficient_aruncaExceptie() {
        // arrange
        Reteta reteta = retetaCuIngrediente(
                ingredientReteta("Zahar", 5.0)
        );

        doReturn(false).when(service).areSuficient(reteta);

        // act + assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.consuma(reteta)
        );

        assertEquals("Stoc insuficient pentru rețeta.", ex.getMessage());
        verify(stocRepo, never()).findAll();
        verify(stocRepo, never()).update(any());
    }

    @Test
    void TC02_retetaFaraIngrediente_terminareNormala() {
        // arrange
        Reteta reteta = retetaCuIngrediente(); // lista goala
        doReturn(true).when(service).areSuficient(reteta);

        // act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // assert
        verify(stocRepo, never()).findAll();
        verify(stocRepo, never()).update(any());
    }

    @Test
    void TC03_unIngredient_unLotDeStoc_consumaCorect() {
        // arrange
        Reteta reteta = retetaCuIngrediente(
                ingredientReteta("Zahar", 5.0)
        );

        Stoc lot1 = stoc(1, "Zahar", 3, 1);

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of(lot1));

        // act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // assert
        assertEquals(5.0, lot1.getCantitate(), 0.0001);
        verify(stocRepo, times(1)).findAll();
        verify(stocRepo, times(1)).update(lot1);
    }

    @Test
    void TC04_unIngredient_douaLoturi_consumaDinAmbele() {
        // arrange
        Reteta reteta = retetaCuIngrediente(
                ingredientReteta("Zahar", 5.0)
        );

        Stoc lot1 = stoc(1, "Zahar", 3, 1);
        Stoc lot2 = stoc(2, "Zahar", 4, 1);

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of(lot1, lot2));

        // act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // assert
        assertEquals(0.0, lot1.getCantitate(), 0.0001);
        assertEquals(2.0, lot2.getCantitate(), 0.0001);

        verify(stocRepo, times(1)).findAll();
        verify(stocRepo, times(1)).update(lot1);
        verify(stocRepo, times(1)).update(lot2);
    }

    @Test
    void TC05_unIngredient_zeroLoturi_nuAruncaExceptie_dacaAreSuficientEsteStubuitTrue() {
        // ATENTIE:
        // Acest test corespunde cazului structural din CFG/tabel,
        // dar poate fi infezabil functional daca areSuficient() real
        // nu ar permite niciodata aceasta situatie.

        // arrange
        Reteta reteta = retetaCuIngrediente(
                ingredientReteta("Zahar", 5.0)
        );

        doReturn(true).when(service).areSuficient(reteta);
        when(stocRepo.findAll()).thenReturn(List.of()); // 0 loturi

        // act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // assert
        verify(stocRepo, times(1)).findAll();
        verify(stocRepo, never()).update(any());
    }

    // =========================
    // helper methods
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