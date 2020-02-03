package ru.cashbox.android.domain.session;

import ru.cashbox.android.model.auth.Session;

public interface UserTerminalRepository {
	boolean checkIsValidToken();
	Session getTerminalSession();
	void dropTerminalSession();
	void saveTerminalSession(Session session);
}
