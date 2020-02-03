package ru.cashbox.android.model.techmap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class TechMapModificator {
    private Long id;
    private String imageUrl;
    private String name;
}
