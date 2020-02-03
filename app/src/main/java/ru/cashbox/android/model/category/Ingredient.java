package ru.cashbox.android.model.category;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.cashbox.android.model.element.Element;
import ru.cashbox.android.model.types.ItemType;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class Ingredient extends Element {
    private Double price;
    @SerializedName(value = "type")
    private ItemType itemType;
    private UnitMeasurement unit;
}
