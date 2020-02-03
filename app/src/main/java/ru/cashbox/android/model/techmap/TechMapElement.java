package ru.cashbox.android.model.techmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class TechMapElement {
    private Long id;
    private Boolean selected;
    private String name;
    private String imageUrl;
    private Double price;
    private Integer count;
}
