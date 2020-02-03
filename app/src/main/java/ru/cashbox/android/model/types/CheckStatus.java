package ru.cashbox.android.model.types;

public enum CheckStatus {
    OPENED("Открыт"),
    CLOSED("Закрыт"),
    PAYED("Оплачен"),
    RETURNED("Возврат"),
    DELETED("Удалён");


    private final String translatedName;

    CheckStatus(String translatedName) {
        this.translatedName = translatedName;
    }

    public String getTranslatedName() {
        return translatedName;
    }

}
