package ru.cashbox.android.model.techmap;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class TechMapGroup {
    private Long id;
    private String name;
    private Integer modificatorsCount;
    private List<TechMapModificator> modificators;
}
