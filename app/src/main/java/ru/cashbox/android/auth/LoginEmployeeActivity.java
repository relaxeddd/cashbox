package ru.cashbox.android.auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.chaos.view.PinView;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Response;
import ru.cashbox.android.MainActivity;
import ru.cashbox.android.R;
import ru.cashbox.android.model.auth.Session;
import ru.cashbox.android.model.auth.User;
import ru.cashbox.android.query.AuthQuery;
import ru.cashbox.android.saver.LoginStateSaver;
import ru.cashbox.android.utils.PrinterHelper;
import ru.cashbox.android.utils.RetrofitInstance;
import ru.cashbox.android.utils.Storage;

public class LoginEmployeeActivity extends AppCompatActivity {

    private static final String LOGIN_EMPLOYEE_TAG = "LOGIN EMPLOYEE";

    private Storage storage;
    private PinView pinView;
    private ProgressBar progressBar;
    private LoginStateSaver loginStateSaver;
    private ConstraintLayout loginEmployeeLayout;
    private ImageButton btnTerminalLogout;
    private TextView terminalTitle;
    private PrinterHelper printerHelper;
    private FancyButton one;
    private FancyButton two;
    private FancyButton three;
    private FancyButton four;
    private FancyButton five;
    private FancyButton six;
    private FancyButton seven;
    private FancyButton eight;
    private FancyButton nine;
    private FancyButton zero;
    private TextView clear;
    private ImageButton delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_employee);
        storage = Storage.getStorage();
        printerHelper = PrinterHelper.getInstance();
        loginEmployeeLayout = findViewById(R.id.login_employee_layout);
        pinView = findViewById(R.id.employee_pin);
        progressBar = findViewById(R.id.login_employee_progress_bar);
        btnTerminalLogout = findViewById(R.id.btn_terminal_logout);
        terminalTitle = findViewById(R.id.employee_terminal_title);
        User terminal = storage.getUserTerminalSession().getUser();
        terminalTitle.setText(terminal.getFullName());

        one = findViewById(R.id.btn_employee_one);
        two = findViewById(R.id.btn_employee_two);
        three = findViewById(R.id.btn_employee_three);
        four = findViewById(R.id.btn_employee_four);
        five = findViewById(R.id.btn_employee_five);
        six = findViewById(R.id.btn_employee_six);
        seven = findViewById(R.id.btn_employee_seven);
        eight = findViewById(R.id.btn_employee_eight);
        nine = findViewById(R.id.btn_employee_nine);
        zero = findViewById(R.id.btn_employee_zero);
        clear = findViewById(R.id.btn_employee_clear);
        delete = findViewById(R.id.btn_employee_delete);

        pinView.setAnimationEnable(true);
        pinView.setHideLineWhenFilled(false);
        pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pinView.setItemBackgroundColor(getResources().getColor(R.color.colorPrimary));
                if (pinView.getItemCount() == s.length()) {
                    checkDataAndExecute();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loginStateSaver = LoginStateSaver.getInstance();
        loginStateSaver.init(null, progressBar);
        progressBar.setVisibility(loginStateSaver.isExecuted() ? View.VISIBLE : View.INVISIBLE);

        loginEmployeeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext()
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        btnTerminalLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Storage.getStorage().logoutFromTerminal();
                finishAffinity();
                startActivity(new Intent(getApplicationContext(), LoginTerminalActivity.class));
            }
        });

        printerHelper.closeShiftAsync(getApplicationContext()); // Закрытие предыдущей смены, если приложение было закрыто

        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(pinView.getText() + "1");
            }
        });
        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(pinView.getText() + "2");
            }
        });
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(pinView.getText() + "3");
            }
        });
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(pinView.getText() + "4");
            }
        });
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(pinView.getText() + "5");
            }
        });
        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(pinView.getText() + "6");
            }
        });
        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(pinView.getText() + "7");
            }
        });
        eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(pinView.getText() + "8");
            }
        });
        nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(pinView.getText() + "9");
            }
        });
        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(pinView.getText() + "0");
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText(null);
            }
        });
        delete.setColorFilter(getResources().getColor(android.R.color.black),
                PorterDuff.Mode.SRC_ATOP);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pinView.getText() != null && pinView.getText().toString().length() > 0) {
                    pinView.setText(pinView.getText().toString().substring(0,
                            pinView.length() - 1));

                }
            }
        });

        if (Storage.getStorage().getUserEmployeeSession() != null) {
            startActivity(new Intent(getApplicationContext(), BetweenLoginScreen.class));
        }

    }

    private void checkDataAndExecute() {
        String pin = pinView.getText() == null ? null :
                pinView.getText().toString().trim();
        if (pin != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("pin", pin);
            new EmployeeAuthTask().execute(payload);
        }
    }

    class EmployeeAuthTask extends AsyncTask<Map<String, Object>, Void, Response<Session>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginStateSaver.update(true);
        }

        @Override
        protected Response<Session> doInBackground(Map<String, Object>... payload) {
            AuthQuery authQuery = RetrofitInstance.getRetrofit().create(AuthQuery.class);
            try {
                return authQuery.loginEmployee(storage.getUserTerminalSession().getToken(),
                        payload[0]).execute();
            } catch (IOException e) {
                Log.e(LOGIN_EMPLOYEE_TAG, getString(R.string.internal_error), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Response<Session> result) {
            super.onPostExecute(result);
            loginStateSaver.update(false);
            if (result == null || result.code() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Toast.makeText(getApplicationContext(), getText(R.string.server_connect_error),
                        Toast.LENGTH_SHORT).show();
                Log.e(LOGIN_EMPLOYEE_TAG, getString(R.string.server_connect_error));
                return;
            }

            switch (result.code()) {
                case HttpStatus.SC_UNAUTHORIZED:
                    Animation animShake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                    pinView.startAnimation(animShake);
                    pinView.setItemBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    Toast.makeText(getApplicationContext(), getText(R.string.correct_data),
                            Toast.LENGTH_SHORT).show();
                    Log.i(LOGIN_EMPLOYEE_TAG, getString(R.string.correct_data));
                    break;
                case HttpStatus.SC_OK:
                    storage.setUserEmployeeSession(result.body());
                    startActivity(new Intent(getApplicationContext(), BetweenLoginScreen.class));
                    finishAffinity();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {

    }
}
