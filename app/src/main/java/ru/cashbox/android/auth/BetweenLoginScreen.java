package ru.cashbox.android.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.rengwuxian.materialedittext.MaterialEditText;

import mehdi.sakout.fancybuttons.FancyButton;
import ru.cashbox.android.MainActivity;
import ru.cashbox.android.R;
import ru.cashbox.android.saver.CheckStateSaver;
import ru.cashbox.android.utils.CashSessionHelper;
import ru.cashbox.android.utils.PrinterHelper;
import ru.cashbox.android.utils.Storage;

public class BetweenLoginScreen extends AppCompatActivity implements Logout {

    private Storage storage;
    private TextView employeeName;
    private FancyButton btnOpenShift;
    private SlideUp slideUp;
    private View dim;
    private View slideView;
    private MaterialEditText balance;
    private FancyButton save;
    private CashSessionHelper sessionHelper;
    private ImageButton btnLogout;
    private CheckStateSaver checkStateSaver;
    private PrinterHelper printerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_between_login_screen);

        storage = Storage.getStorage();
        sessionHelper = CashSessionHelper.getInstance();
        employeeName = findViewById(R.id.between_screen_employee_name);
        btnOpenShift = findViewById(R.id.btn_between_open);
        checkStateSaver = CheckStateSaver.getInstance();
        printerHelper = PrinterHelper.getInstance();
        btnLogout = findViewById(R.id.btn_between_logout);
        slideView = findViewById(R.id.shift_balance_slide_view);
        //dim = findViewById(R.id.dim_between_screen);
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
        balance = findViewById(R.id.shift_balance_field);
        save = findViewById(R.id.btn_shift_balance_save);
        /*dim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (slideUp.isVisible()) {
                    slideUp.hide();
                }
            }
        });*/

        employeeName.setText(storage.getUserEmployeeSession().getUser().getFullname());
        btnOpenShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp.show();
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

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LogoutExecutor(BetweenLoginScreen.this, storage.getBalance()).execute(true);
            }
        });

        if (Storage.getStorage().getShiftManager().isShiftOpened()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("isOpenedSession", true);
            startActivity(intent);
        }

    }


    private void saveBalance() {
        String balanceValue = balance.getText() == null ? Storage.EMPTY :
                balance.getText().toString();
        if (balanceValue.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Введите остаток кассы",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String resultValue = storage.checkBalanceValue(balanceValue);
        if (resultValue != null) {
            balance.setText(resultValue);
            storage.setBalance(resultValue);
            sessionHelper.openSession();
            finishAffinity();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else {
            Toast.makeText(getApplicationContext(), "Введите корректное значение", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFinishedLogout(Integer code, String balance) {
        checkStateSaver.setup();
        printerHelper.closeShift(storage.getContext());
        startActivity(new Intent(storage.getContext(), LoginEmployeeActivity.class));
        finishAffinity();
    }
}
