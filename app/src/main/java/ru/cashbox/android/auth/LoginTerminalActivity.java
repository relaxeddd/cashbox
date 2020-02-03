package ru.cashbox.android.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.cashbox.android.R;
import ru.cashbox.android.model.Session;
import ru.cashbox.android.query.AuthQuery;
import ru.cashbox.android.saver.LoginStateSaver;
import ru.cashbox.android.utils.PrinterHelper;
import ru.cashbox.android.utils.RetrofitInstance;
import ru.cashbox.android.utils.Storage;

public class LoginTerminalActivity extends AppCompatActivity {

    private static final String LOGIN_TERMINAL_TAG = "LOGIN TERMINAL";

    private FancyButton btnLogin;
    private MaterialEditText usernameField;
    private MaterialEditText passwordField;
    private ProgressBar progressBar;
    private Storage storage;
    private LoginStateSaver loginStateSaver;
    private ConstraintLayout loginTerminalLayout;
    private SlideUp slideUp;
    private View dim;
    private View slideView;
    private ImageButton btnChooseDomain;
    private FancyButton btnSave;
    private MaterialEditText domain;

    private ConstraintLayout usernameContainer;
    private ConstraintLayout passwordContainer;

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

    private FancyButton q;
    private FancyButton w;
    private FancyButton e;
    private FancyButton r;
    private FancyButton t;
    private FancyButton y;
    private FancyButton u;
    private FancyButton i;
    private FancyButton o;
    private FancyButton p;

    private FancyButton a;
    private FancyButton s;
    private FancyButton d;
    private FancyButton f;
    private FancyButton g;
    private FancyButton h;
    private FancyButton j;
    private FancyButton k;
    private FancyButton l;

    private FancyButton z;
    private FancyButton x;
    private FancyButton c;
    private FancyButton v;
    private FancyButton b;
    private FancyButton n;
    private FancyButton m;
    private FancyButton del;

    private enum Field {
        USERNAME,
        PASSWORD
    }

    private Field field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_terminal);
        storage = Storage.getStorage();

        loginTerminalLayout = findViewById(R.id.login_terminal_layout);
        progressBar = findViewById(R.id.login_terminal_progress_bar);
        usernameField = findViewById(R.id.login_terminal_username_input);
        passwordField = findViewById(R.id.login_terminal_password_input);
        btnLogin = findViewById(R.id.btn_login_terminal_enter);
        btnChooseDomain = findViewById(R.id.btn_choose_domain);
        btnSave = findViewById(R.id.domain_save);
        domain = findViewById(R.id.domain_field);
        usernameContainer = findViewById(R.id.terminal_username_container);
        passwordContainer = findViewById(R.id.terminal_password_container);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameField.getText() == null ? null :
                        usernameField.getText().toString().trim();
                String password = passwordField.getText() == null ? null :
                        passwordField.getText().toString().trim();

                if (username != null && password != null) {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("username", username);
                    payload.put("password", password);
                    payload.put("rememberMe", true);

                    new TerminalAuthTask().execute(payload);
                }
            }
        });

        loginStateSaver = LoginStateSaver.getInstance();
        loginStateSaver.init(btnLogin, progressBar);
        progressBar.setVisibility(loginStateSaver.isExecuted() ? View.VISIBLE : View.INVISIBLE);
        btnLogin.setEnabled(!loginStateSaver.isExecuted());

        loginTerminalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext()
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        slideView = findViewById(R.id.domain_slide_view);
        dim = findViewById(R.id.dim_domain);
        slideUp = new SlideUpBuilder(slideView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
                        float value = 1 - (percent / 100);
                        if (value > 0.0) {
                            dim.setClickable(true);
                        }
                        if (value == 0.0) {
                            dim.setClickable(false);
                        }
                        dim.setAlpha(value);
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if (visibility == 0) {
                            usernameField.setEnabled(false);
                            passwordField.setEnabled(false);
                        } else {
                            usernameField.setEnabled(true);
                            passwordField.setEnabled(true);
                        }
                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .withStartState(storage.getDomain() == null ? SlideUp.State.SHOWED :
                        SlideUp.State.HIDDEN)
                .build();
        dim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (slideUp.isVisible()) {
                    slideUp.hide();
                }
            }
        });

        btnChooseDomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDomain();
            }
        });

        domain.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    saveDomain();
                    return true;
                }
                return false;
            }
        });

        one = findViewById(R.id.btn_terminal_one);
        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("1");
            }
        });

        two = findViewById(R.id.btn_terminal_two);
        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("2");
            }
        });

        three = findViewById(R.id.btn_terminal_three);
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("3");
            }
        });

        four = findViewById(R.id.btn_terminal_four);
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("4");
            }
        });

        five = findViewById(R.id.btn_terminal_five);
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("5");
            }
        });

        six = findViewById(R.id.btn_terminal_six);
        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("6");
            }
        });

        seven = findViewById(R.id.btn_terminal_seven);
        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("7");
            }
        });

        eight = findViewById(R.id.btn_terminal_eight);
        eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("8");
            }
        });

        nine = findViewById(R.id.btn_terminal_nine);
        nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("9");
            }
        });

        zero = findViewById(R.id.btn_terminal_zero);
        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("0");
            }
        });

        q = findViewById(R.id.btn_terminal_q);
        q.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("q");
            }
        });

        w = findViewById(R.id.btn_terminal_w);
        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("w");
            }
        });

        e = findViewById(R.id.btn_terminal_e);
        e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("e");
            }
        });

        r = findViewById(R.id.btn_terminal_r);
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("r");
            }
        });

        t = findViewById(R.id.btn_terminal_t);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("t");
            }
        });

        y = findViewById(R.id.btn_terminal_y);
        y.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("y");
            }
        });

        u = findViewById(R.id.btn_terminal_u);
        u.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("u");
            }
        });

        i = findViewById(R.id.btn_terminal_i);
        i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("i");
            }
        });

        o = findViewById(R.id.btn_terminal_o);
        o.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("o");
            }
        });

        p = findViewById(R.id.btn_terminal_p);
        p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("p");
            }
        });

        a = findViewById(R.id.btn_terminal_a);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("a");
            }
        });

        s = findViewById(R.id.btn_terminal_s);
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("s");
            }
        });

        d = findViewById(R.id.btn_terminal_d);
        d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("d");
            }
        });

        f = findViewById(R.id.btn_terminal_f);
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("f");
            }
        });

        g = findViewById(R.id.btn_terminal_g);
        g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("g");
            }
        });

        h = findViewById(R.id.btn_terminal_h);
        h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("h");
            }
        });

        j = findViewById(R.id.btn_terminal_j);
        j.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("j");
            }
        });

        k = findViewById(R.id.btn_terminal_k);
        k.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("k");
            }
        });

        l = findViewById(R.id.btn_terminal_l);
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("l");
            }
        });

        z = findViewById(R.id.btn_terminal_z);
        z.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("z");
            }
        });

        x = findViewById(R.id.btn_terminal_x);
        x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("x");
            }
        });

        c = findViewById(R.id.btn_terminal_c);
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("c");
            }
        });

        v = findViewById(R.id.btn_terminal_v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("v");
            }
        });

        b = findViewById(R.id.btn_terminal_b);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("b");
            }
        });

        n = findViewById(R.id.btn_terminal_n);
        n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("n");
            }
        });

        m = findViewById(R.id.btn_terminal_m);
        m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSymbol("m");
            }
        });

        del = findViewById(R.id.btn_terminal_del);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (field != null) {
                    if (field.equals(Field.USERNAME)) {
                        if (usernameField.getText() != null &&
                                usernameField.getText().toString().length() > 0) {
                            usernameField.setText(usernameField.getText().toString().substring(0,
                                    usernameField.length() - 1));
                        }
                    }
                    if (field.equals(Field.PASSWORD)) {
                        if (passwordField.getText() != null &&
                                passwordField.getText().toString().length() > 0) {
                            passwordField.setText(passwordField.getText().toString().substring(0,
                                    passwordField.length() - 1));
                        }
                    }
                }
            }
        });

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            usernameField.setFocusable(false);
            passwordField.setFocusable(false);
        }

        usernameContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernameContainer.setBackgroundResource(R.drawable.edit_text_border_selected);
                passwordContainer.setBackgroundResource(R.drawable.edit_text_border);
                field = Field.USERNAME;
            }
        });

        passwordContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernameContainer.setBackgroundResource(R.drawable.edit_text_border);
                passwordContainer.setBackgroundResource(R.drawable.edit_text_border_selected);
                field = Field.PASSWORD;
            }
        });

        usernameField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernameContainer.setBackgroundResource(R.drawable.edit_text_border_selected);
                passwordContainer.setBackgroundResource(R.drawable.edit_text_border);
                field = Field.USERNAME;
            }
        });
        passwordField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernameContainer.setBackgroundResource(R.drawable.edit_text_border);
                passwordContainer.setBackgroundResource(R.drawable.edit_text_border_selected);
                field = Field.PASSWORD;
            }
        });

        if (Storage.getStorage().getUserTerminalSession() != null) {
            startActivity(new Intent(getApplicationContext(), LoginEmployeeActivity.class));
        }

    }

    @SuppressLint("SetTextI18n")
    private void addSymbol(String symbol) {
        if (field != null) {
            if (field.equals(Field.USERNAME)) {
                Editable usernameEditable = usernameField.getText();
                if (usernameEditable != null) {
                    usernameField.setText(usernameEditable.toString() + symbol);
                }
            }
            if (field.equals(Field.PASSWORD)) {
                Editable passwordEditable = passwordField.getText();
                if (passwordEditable != null) {
                    passwordField.setText(passwordEditable.toString() + symbol);
                }
            }
        }
    }

    private void saveDomain() {
        String domainValue = domain.getText() == null ?
                Storage.EMPTY : domain.getText().toString();
        storage.saveDomain(domainValue);
        slideUp.hide();
        Toast.makeText(getApplicationContext(), "Поддомен сохранён",
                Toast.LENGTH_SHORT).show();
        InputMethodManager imm = (InputMethodManager) getApplicationContext()
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    class TerminalAuthTask extends AsyncTask<Map<String, Object>, Void, Response<Session>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginStateSaver.update(true);
        }

        @Override
        protected Response<Session> doInBackground(Map<String, Object>... payload) {
            AuthQuery authQuery = RetrofitInstance.getRetrofit().create(AuthQuery.class);
            try {
                return authQuery.loginTerminal(payload[0]).execute();
            } catch (IOException e) {
                Log.e(LOGIN_TERMINAL_TAG, getString(R.string.internal_error), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Response<Session> result) {
            super.onPostExecute(result);
            loginStateSaver.update(false);
            if (result == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.server_connect_error),
                        Toast.LENGTH_SHORT).show();
                Log.e(LOGIN_TERMINAL_TAG, getString(R.string.server_connect_error));
                return;
            }

            switch (result.code()) {
                case HttpStatus.SC_UNAUTHORIZED:
                    Toast.makeText(getApplicationContext(), getString(R.string.correct_data),
                            Toast.LENGTH_SHORT).show();
                    Log.i(LOGIN_TERMINAL_TAG, getString(R.string.correct_data));
                    break;
                case HttpStatus.SC_OK:
                    storage.setUserTerminalSession(result.body());
                    startActivity(new Intent(getApplicationContext(), LoginEmployeeActivity.class));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                .setTopColorRes(R.color.colorAccent)
                .setTitle(R.string.exit_title)
                .setMessage(R.string.exit_msg)
                .setPositiveButton(R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishAffinity();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
