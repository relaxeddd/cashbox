package ru.cashbox.android.model.techmap;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.cashbox.android.model.category.Category;
import ru.cashbox.android.model.element.Element;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class TechMap extends Element {
    private Double price;
    private Category category;
    private List<TechMapGroup> modificatorGroups;
}
