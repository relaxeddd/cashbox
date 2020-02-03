package ru.cashbox.android.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;
import ru.cashbox.android.R;
import ru.cashbox.android.model.printer.Printer;
import ru.cashbox.android.printer.PrinterSettingsActivity;
import ru.cashbox.android.utils.PrinterHelper;
import ru.cashbox.android.utils.Storage;

public class PrinterSettingsAddFragment extends Fragment implements PrinterHelper.ScanListener {

    private PrinterHelper printerHelper;
    private ProgressBar progressBar;
    private Context viewContext;
    private TextView title;
    private FancyButton btnScan;
    private MaterialEditText ip;
    private MaterialEditText port;
    private FancyButton btnTestPrint;
    private FancyButton btnSave;
    private Printer printer;
    private ConstraintLayout btnBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_printer_settings_add, container, false);

        PrinterSettingsActivity.CURRENT_SCREEN = R.layout.fragment_printer_settings_add;
        viewContext = view.getContext();
        btnSave = view.findViewById(R.id.btn_printer_save);
        progressBar = view.findViewById(R.id.progress_bar_printer_add);
        title = view.findViewById(R.id.printer_settings_printer_name);
        btnScan = view.findViewById(R.id.btn_printer_scan);
        ip = view.findViewById(R.id.printer_settings_ip);
        port = view.findViewById(R.id.printer_settings_port);
        btnTestPrint = view.findViewById(R.id.btn_printer_test_print);
        btnSave = view.findViewById(R.id.btn_printer_save);
        btnBack = view.findViewById(R.id.action_bar_printer_add);

        printerHelper = PrinterHelper.getInstance();
        if (printerHelper.isScanExecuted()) {
            setElementsState(true);
        } else {
            setElementsState(false);
        }

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable ipView = ip.getText();
                Editable portView = port.getText();

                if (ipView != null && portView != null) {
                    printer = null;
                    setElementsState(true);
                    title.setText("");
                    if (!ipView.toString().isEmpty() && !portView.toString().isEmpty()) {
                        printerHelper.startScan(viewContext,
                                PrinterSettingsAddFragment.this, ipView.toString(), portView.toString());
                    }
                    if (ipView.toString().isEmpty() || portView.toString().isEmpty()) {
                        printerHelper.startScan(viewContext, PrinterSettingsAddFragment.this);
                    }

                }

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (printer != null) {
                    if (printerHelper.getSavedPrinterByIp(printer.getIp()) == null) {
                        List<Printer> printers = printerHelper.getPrinters();
                        printers.add(printer);
                        printerHelper.savePrinters(printers);
                        printerHelper.selectPrinter(printer.getIp());

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        PrinterSettingsHistoryFragment printerSettingsHistoryFragment = new PrinterSettingsHistoryFragment();
                        transaction.replace(R.id.printer_settings_container, printerSettingsHistoryFragment);
                        transaction.commit();

                        Toast.makeText(viewContext, "Принтер сохранен и установлен по умолчанию", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(viewContext, "Данный принтер уже существует", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(viewContext, "Сначала выполните поиск принтера",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnTestPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (printer != null) {
                    printerHelper.testPrint(Storage.getStorage().getContext(), printer.getIp(), printer.getPort());
                } else {
                    Toast.makeText(viewContext, "Сначала выполните поиск принтера",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                PrinterSettingsHistoryFragment printerSettingsHistoryFragment = new PrinterSettingsHistoryFragment();
                transaction.replace(R.id.printer_settings_container, printerSettingsHistoryFragment);
                transaction.commit();
            }
        });

        return view;
    }

    private void setElementsState(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        ip.setEnabled(!loading);
        port.setEnabled(!loading);
        btnTestPrint.setEnabled(!loading);
        btnSave.setEnabled(!loading);
    }

    @Override
    public void onScanned(Printer printer) {
        setElementsState(false);
        if (printer != null) {
            this.printer = printer;
            title.setText(printer.getName());
        } else {
            title.setText("Принтер не найден");
        }
    }
}
