package ru.cashbox.android.domain.session;

public interface ShiftRepository {
	public double getBalance();
	public void saveBalance(double balance);

	void openShift();

	void closeShift();

	boolean isShiftOpened();
}
