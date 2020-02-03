package ru.cashbox.android.model.check;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.cashbox.android.model.bill.BillModificatorWrapper;
import ru.cashbox.android.model.types.CheckItemCategoryType;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class CheckItem {
    private Long id;
    private String imageUrl;
    private String name;
    private Integer amount;
    private Double price;
    private Double fullPrice;
    private CheckItemCategoryType type;
    private List<BillModificatorWrapper> modificators;
}
