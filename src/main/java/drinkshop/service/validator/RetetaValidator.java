package drinkshop.service.validator;

import drinkshop.domain.IngredientReteta;
import drinkshop.domain.Reteta;

import java.util.List;

public class RetetaValidator implements Validator<Reteta> {

    @Override
    public void validate(Reteta reteta) {

        StringBuilder errors = new StringBuilder();

        if (reteta.getId() <= 0)
            errors.append("Product ID invalid!\n");

        List<IngredientReteta> ingrediente = reteta.getIngrediente();
        if (ingrediente == null || ingrediente.isEmpty())
            errors.append("Ingrediente empty!\n");
        else {
            ingrediente.stream()
                    .filter(entry -> entry.getCantitate() <= 0)
                    .forEach(entry -> errors.append("[")
                            .append(entry.getDenumire())
                            .append("] cantitate negativa sau zero\n"));
        }

        if (!errors.isEmpty())
            throw new ValidationException(errors.toString());
    }
}
