package ru.cashbox.android.domain.session;

import ru.cashbox.android.model.auth.Session;

public interface UserEmployeeRepository {
	boolean checkIsValidToken();
	Session getEmployeeSession();
	void dropEmployeeSession();
	void saveEmployeeSession(Session session);
}
