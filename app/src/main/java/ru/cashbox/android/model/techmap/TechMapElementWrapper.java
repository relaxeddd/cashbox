package ru.cashbox.android.model.techmap;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class TechMapElementWrapper {
    private String groupName;
    private Integer maxSelected;
    private List<TechMapElement> elements;
}
