package ru.cashbox.android.domain.session;

import ru.cashbox.android.model.auth.Session;

public interface UserSessionInteractor {
	// terminal, step1
	boolean isValidTerminalSession();
	void saveTerminalSession(Session terminalSession);
	Session getUserTerminalSession();
	// employee, step2
	void saveEmployeeSession(Session employeeSession);
	Session getUserEmployeeSession();

	void dropAllSessions();

	double getBalance();

	void setBalance(double balance);

	ShiftRepository getShiftRepository();
}
