package ru.cashbox.android.data.session;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Response;
import ru.cashbox.android.domain.session.UserTerminalRepository;
import ru.cashbox.android.model.Session;
import ru.cashbox.android.model.User;
import ru.cashbox.android.query.AuthQuery;
import ru.cashbox.android.utils.RetrofitInstance;
import ru.cashbox.android.utils.Storage;

public class UserTerminalRepositoryImpl implements UserTerminalRepository {
	class TerminalAuthTask extends AsyncTask<String, Void, Response<User>> {

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
				case HttpStatus.SC_UNAUTHORIZED:
					break;
				case HttpStatus.SC_OK:
					//storage.setUserTerminalSession(result.body());
					//startActivity(new Intent(getApplicationContext(), LoginEmployeeActivity.class));
					break;
				default:
					break;
			}
		}
	}


	private final String TERMINAL_SESSION_TOKEN_TAG = "TERMINAL_SESSION_TOKEN_TAG";
	private Session userTerminalSession;
	private SharedPreferences preferences;
	private AuthQuery authQuery;

	public UserTerminalRepositoryImpl(SharedPreferences preferences,
									  AuthQuery authQuery) {
		this.preferences = preferences;
		this.authQuery = authQuery;
	}

	@Override
	public boolean checkIsValidToken() {
		return true;
	}

	@Override
	public Session getTerminalSession() {
		if (userTerminalSession != null) {
			return userTerminalSession;
		}

		loadTerminalSession();
		return userTerminalSession;
	}

	private void loadTerminalSession() {
		if (checkIsValidToken()) {
			String token = preferences.getString(TERMINAL_SESSION_TOKEN_TAG, Storage.EMPTY);
			try {
				Response<User> response = new TerminalAuthTask().execute(token).get(5, TimeUnit.SECONDS);
				User user = response != null ? response.body() : null;
				if (user != null) {
					userTerminalSession = new Session(token, user);
				}
			} catch (ExecutionException | InterruptedException | TimeoutException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void dropTerminalSession() {
		userTerminalSession = null;
		preferences.edit()
				.putString(TERMINAL_SESSION_TOKEN_TAG, "")
				.apply();
	}

	@Override
	public void saveTerminalSession(Session session) {
		userTerminalSession = session;
		preferences.edit()
				.putString(TERMINAL_SESSION_TOKEN_TAG, session.getToken())
				.apply();
	}
}
