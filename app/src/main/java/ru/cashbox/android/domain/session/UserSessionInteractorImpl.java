package ru.cashbox.android.domain.session;

import ru.cashbox.android.model.Session;

public class UserSessionInteractorImpl implements UserSessionInteractor {

	private final UserTerminalRepository terminalRepository;
	private final UserEmployeeRepository employeeRepository;
	private final ShiftRepository shiftRepository;

	public UserSessionInteractorImpl(
			UserTerminalRepository terminalRepository,
			UserEmployeeRepository employeeRepository,
			ShiftRepository shiftRepository) {
		this.terminalRepository = terminalRepository;
		this.employeeRepository = employeeRepository;
		this.shiftRepository = shiftRepository;
	}

	@Override
	public boolean isValidTerminalSession() {
		return terminalRepository.checkIsValidToken();
	}

	@Override
	public void saveTerminalSession(Session terminalSession) {
		terminalRepository.saveTerminalSession(terminalSession);
	}

	@Override
	public Session getUserTerminalSession() {
		return terminalRepository.getTerminalSession();
	}

	@Override
	public void dropAllSessions() {
		terminalRepository.dropTerminalSession();
		employeeRepository.dropEmployeeSession();
	}

	@Override
	public void saveEmployeeSession(Session employeeSession) {
		employeeRepository.saveEmployeeSession(employeeSession);
	}

	@Override
	public Session getUserEmployeeSession() {
		return employeeRepository.getEmployeeSession();
	}

	@Override
	public double getBalance() {
		return shiftRepository.getBalance();
	}

	@Override
	public void setBalance(double balance) {
		shiftRepository.saveBalance(balance);
	}

	@Override
	public ShiftRepository getShiftRepository() {
		return shiftRepository;
	}
}
