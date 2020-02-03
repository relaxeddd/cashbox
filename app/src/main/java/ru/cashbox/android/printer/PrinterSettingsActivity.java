package ru.cashbox.android.printer;

import android.app.FragmentTransaction;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ru.cashbox.android.R;
import ru.cashbox.android.fragment.PrinterSettingsHistoryFragment;
import ru.cashbox.android.utils.PrinterHelper;

public class PrinterSettingsActivity extends AppCompatActivity {

    private PrinterHelper printerHelper;
    public static Integer CURRENT_SCREEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_settings);
        printerHelper = PrinterHelper.getInstance();
        CURRENT_SCREEN = null;

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        PrinterSettingsHistoryFragment printerSettingsHistoryFragment = new PrinterSettingsHistoryFragment();
        transaction.replace(R.id.printer_settings_container, printerSettingsHistoryFragment);
        transaction.commit();

    }

    @Override
    public void onBackPressed() {
        if (CURRENT_SCREEN != null) {
            switch (CURRENT_SCREEN) {
                case R.layout.fragment_printer_settings_add:
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    PrinterSettingsHistoryFragment printerSettingsHistoryFragment = new PrinterSettingsHistoryFragment();
                    transaction.replace(R.id.printer_settings_container, printerSettingsHistoryFragment);
                    transaction.commit();
                    break;
                case R.layout.fragment_printer_settings_history:
                    super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
        printerHelper.stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        printerHelper.stopScan();
    }
}
