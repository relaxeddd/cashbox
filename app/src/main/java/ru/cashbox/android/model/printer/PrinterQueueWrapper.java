package ru.cashbox.android.model.printer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class PrinterQueueWrapper {
    private PrinterAction action;
    private List<Object> params;
}
