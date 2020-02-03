package ru.cashbox.android.model.bill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class BillModificatorWrapper {
    private Long id;
    private Integer count;
    private Double price;
}
