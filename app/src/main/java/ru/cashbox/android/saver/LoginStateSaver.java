package ru.cashbox.android.saver;

import android.view.View;
import android.widget.ProgressBar;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginStateSaver {
    private static LoginStateSaver loginScreenSaver;
    private FancyButton btnLogin;
    private ProgressBar progressBar;
    private Boolean executed = false;

    public static LoginStateSaver getInstance() {
        return loginScreenSaver = loginScreenSaver == null ? new LoginStateSaver() : loginScreenSaver;
    }

    public void init (FancyButton btnLogin, ProgressBar progressBar) {
        this.btnLogin = btnLogin;
        this.progressBar = progressBar;
    }

    public void update(Boolean executed) {
        this.executed = executed;
        if (btnLogin != null) {
            btnLogin.setEnabled(!executed);
        }
        progressBar.setVisibility(executed ? View.VISIBLE : View.INVISIBLE);
    }

    public Boolean isExecuted() {
        return executed;
    }

}
