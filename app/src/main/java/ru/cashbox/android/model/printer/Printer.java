package ru.cashbox.android.model.printer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.cashbox.android.model.types.PrinterConnectionType;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class Printer {

    private String name;
    private PrinterConnectionType connectionType;
    private String ip;
    private String port;
    private Boolean selected;

}
