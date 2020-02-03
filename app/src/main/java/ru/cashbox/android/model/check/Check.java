package ru.cashbox.android.model.check;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.cashbox.android.model.types.CheckStatus;
import ru.cashbox.android.model.types.PayType;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class Check {
    private Integer number;
    private UUID hash;
    private List<CheckItem> items;
    private CheckStatus status;
    private String created;
    private String closed;
    private Boolean historySelected;
    private PayType payType;
}
