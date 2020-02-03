package ru.cashbox.android.data.session;

import android.content.SharedPreferences;

import ru.cashbox.android.domain.session.ShiftRepository;

public class ShiftRepositoryImpl implements ShiftRepository {

	private static final String BALANCE_KEY = "balance_key";
	private static final String SHIFT_IS_OPENED_KEY = "is_shift_opened_key";

	private final SharedPreferences preferences;

	public ShiftRepositoryImpl(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	@Override
	public double getBalance() {
		return Double.valueOf(preferences.getString(BALANCE_KEY, "0.0"));
	}

	@Override
	public void saveBalance(double balance) {
		preferences.edit().
				putString(BALANCE_KEY, String.valueOf(getBalance()))
				.apply();
	}

	@Override
	public void openShift() {
		preferences.edit().
				putBoolean(SHIFT_IS_OPENED_KEY, true)
				.apply();
	}

	@Override
	public void closeShift() {
		preferences.edit()
				.putBoolean(SHIFT_IS_OPENED_KEY, false)
				.apply();
	}

	@Override
	public boolean isShiftOpened() {
		return preferences.getBoolean(SHIFT_IS_OPENED_KEY, false);
	}
}
