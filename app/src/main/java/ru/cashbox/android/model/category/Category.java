package ru.cashbox.android.model.category;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.cashbox.android.model.element.Element;
import ru.cashbox.android.model.types.CategoryType;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class Category extends Element {
    private Long parentId;
    private List<Ingredient> ingredients;
    private Boolean defaultCategory;
    @SerializedName(value = "type")
    private CategoryType categoryType;
}
