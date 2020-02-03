package ru.cashbox.android.application;

import android.content.Context;

import ru.cashbox.android.CashboxApplication;

public interface ContextProvider {
	// non-leakable and always available Application Context
	Context getContext();
	void init(CashboxApplication application);
}
