package ru.cashbox.android.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.rengwuxian.materialedittext.MaterialEditText;

import mehdi.sakout.fancybuttons.FancyButton;
import ru.cashbox.android.MainActivity;
import ru.cashbox.android.R;
import ru.cashbox.android.auth.BetweenLoginScreen;
import ru.cashbox.android.auth.LoginEmployeeActivity;
import ru.cashbox.android.auth.LoginTerminalActivity;
import ru.cashbox.android.auth.Logout;
import ru.cashbox.android.auth.LogoutExecutor;
import ru.cashbox.android.model.Session;
import ru.cashbox.android.printer.PrinterSettingsActivity;
import ru.cashbox.android.saver.CheckStateSaver;
import ru.cashbox.android.utils.CashSessionHelper;
import ru.cashbox.android.utils.PrinterHelper;
import ru.cashbox.android.utils.Storage;

public class SettingsFragment extends Fragment implements Logout, View.OnClickListener {

    private Storage storage;
    private TextView name;
    private FancyButton btnLogout;
    private CheckStateSaver checkStateSaver;
    private ConstraintLayout printerSettingsItem;
    private PrinterHelper printerHelper;
    private SlideUp slideUp;
    private View dim;
    private View slideView;
    private MaterialEditText balance;
    private FancyButton save;
    private CashSessionHelper sessionHelper;
    private FancyButton btnLogoutBetween;
    private boolean fullLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        checkStateSaver = CheckStateSaver.getInstance();
        storage = Storage.getStorage();
        printerHelper = PrinterHelper.getInstance();
        sessionHelper = CashSessionHelper.getInstance();
        name = view.findViewById(R.id.settings_employee_name);
        btnLogout = view.findViewById(R.id.btn_logout_employee);
        printerSettingsItem = view.findViewById(R.id.setting_connect_printer_item);

        Session userEmployeeSession = storage.getUserEmployeeSession();
        name.setText(userEmployeeSession.getUser().getFullname());

        btnLogout.setOnClickListener(this);
        printerSettingsItem.setOnClickListener(this);
        btnLogoutBetween = view.findViewById(R.id.btn_logout_settings_between);

        slideView = view.findViewById(R.id.shift_balance_slide_view);
        dim = view.findViewById(R.id.dim_settings_screen);
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

                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .withStartState(SlideUp.State.HIDDEN)
                .build();
        balance = view.findViewById(R.id.shift_balance_field);
        save = view.findViewById(R.id.btn_shift_balance_save);
        dim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (slideUp.isVisible()) {
                    slideUp.hide();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBalance();
            }
        });

        balance.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    saveBalance();
                    return true;
                }
                return false;
            }
        });

        btnLogoutBetween.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp.show();
            }
        });


        return view;
    }

    private void saveBalance() {
        String balanceValue = balance.getText() == null ? Storage.EMPTY :
                balance.getText().toString();
        if (balanceValue.isEmpty()) {
            Toast.makeText(storage.getContext(), "Введите остаток кассы",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String resultValue = storage.checkBalanceValue(balanceValue);
        if (resultValue != null) {
            if (fullLogout) {
                new LogoutExecutor(SettingsFragment.this, resultValue).execute(false);
            } else {
                printerHelper.closeShift(storage.getContext());
                sessionHelper.closeSession();
                getActivity().finishAffinity();
                startActivity(new Intent(storage.getContext(), BetweenLoginScreen.class));
            }
        } else {
            Toast.makeText(storage.getContext(), "Введите корректное значение", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFinishedLogout(Integer code, String value) {
        checkStateSaver.setup();
        printerHelper.closeShift(storage.getContext());
        balance.setText(value);
        storage.setBalance(value);
        sessionHelper.closeSession();
        getActivity().finishAffinity();
        startActivity(new Intent(storage.getContext(), LoginEmployeeActivity.class));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnLogout.getId()) {
            slideUp.show();
            fullLogout = true;
        }
        if (v.getId() == printerSettingsItem.getId()) {
            startActivity(new Intent(storage.getContext(), PrinterSettingsActivity.class));
        }
    }
}
