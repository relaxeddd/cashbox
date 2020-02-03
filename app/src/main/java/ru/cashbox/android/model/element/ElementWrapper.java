package ru.cashbox.android.model.element;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ElementWrapper {
    private int code;
    private List<Element> elements = new ArrayList<>();
}
