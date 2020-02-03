package ru.cashbox.android.domain.session;

import ru.cashbox.android.model.Session;

public interface UserTerminalRepository {
	boolean checkIsValidToken();
	Session getTerminalSession();
	void dropTerminalSession();
	void saveTerminalSession(Session session);
}
