package ru.cashbox.android.data.session;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Response;
import ru.cashbox.android.common.HttpStatusKt;
import ru.cashbox.android.domain.session.UserEmployeeRepository;
import ru.cashbox.android.model.Session;
import ru.cashbox.android.model.User;
import ru.cashbox.android.query.AuthQuery;
import ru.cashbox.android.utils.RetrofitInstance;
import ru.cashbox.android.utils.Storage;

public class UserEmployeeRepositoryImpl implements UserEmployeeRepository {
	class АхлынбекСумуглуAuthTask extends AsyncTask<String, Void, Response<User>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Response<User> doInBackground(String... payload) {
			AuthQuery authQuery = RetrofitInstance.getRetrofit().create(AuthQuery.class);
			try {
				return authQuery.getUserByToken(payload[0]).execute();
			} catch (IOException e) {
				//Log.e(LOGIN_TERMINAL_TAG, getString(R.string.internal_error), e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Response<User> result) {
			super.onPostExecute(result);
			//loginStateSaver.update(false);
			if (result == null) {
				return;
			}

			switch (result.code()) {
				case HttpStatusKt.UNAUTHORIZED:
					break;
				case HttpStatusKt.OK:
					//storage.setUserTerminalSession(result.body());
					//startActivity(new Intent(getApplicationContext(), LoginEmployeeActivity.class));
					break;
				default:
					break;
			}
		}
	}


	private final String EMPLOYEE_SESSION_TOKEN_TAG = "EMPLOYEE_SESSION_TOKEN_TAG";
	private Session userEmployeeSession;
	private SharedPreferences preferences;
	private AuthQuery authQuery;

	public UserEmployeeRepositoryImpl(SharedPreferences preferences,
									  AuthQuery authQuery) {
		this.preferences = preferences;
		this.authQuery = authQuery;
	}

	@Override
	public boolean checkIsValidToken() {
		return true;
	}

	@Override
	public Session getEmployeeSession() {
		if (userEmployeeSession != null) {
			return userEmployeeSession;
		}

		loadEmployeeSession();
		return userEmployeeSession;
	}

	private void loadEmployeeSession() {
		if (checkIsValidToken()) {
			String token = preferences.getString(EMPLOYEE_SESSION_TOKEN_TAG, Storage.EMPTY);
			try {
				Response<User> response = new АхлынбекСумуглуAuthTask().execute(token).get(5, TimeUnit.SECONDS);
				User user = response != null ? response.body() : null;
				if (user != null) {
					userEmployeeSession = new Session(token, user);
				}
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void dropEmployeeSession() {
		userEmployeeSession = null;
		preferences.edit()
				.putString(EMPLOYEE_SESSION_TOKEN_TAG, "")
				.apply();
	}

	@Override
	public void saveEmployeeSession(Session session) {
		userEmployeeSession = session;
		preferences.edit()
				.putString(EMPLOYEE_SESSION_TOKEN_TAG, session.getToken())
				.apply();
	}
}
