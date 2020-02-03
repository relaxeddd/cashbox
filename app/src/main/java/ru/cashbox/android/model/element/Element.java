package ru.cashbox.android.model.element;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.cashbox.android.model.types.ElementType;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Element {
    private Long id;
    private String imageUrl;
    private String name;
    private ElementType elementType;
}
