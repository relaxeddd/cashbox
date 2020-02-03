package ru.cashbox.android.application;

import android.content.Context;

import ru.cashbox.android.CashboxApplication;

public class ContextProviderImpl implements ContextProvider {
	private Context applicationContext;

	@Override
	public Context getContext() {
		return applicationContext;
	}

	@Override
	public void init(CashboxApplication application) {
		applicationContext = application.getApplicationContext();
	}
}
