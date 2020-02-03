package ru.cashbox.android.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import net.igenius.customcheckbox.CustomCheckBox;

import mehdi.sakout.fancybuttons.FancyButton;
import ru.cashbox.android.R;
import ru.cashbox.android.adapter.PrinterSettingsHistoryAdapter;
import ru.cashbox.android.model.printer.Printer;
import ru.cashbox.android.printer.PrinterSettingsActivity;
import ru.cashbox.android.utils.PrinterHelper;
import ru.cashbox.android.utils.Storage;

public class PrinterSettingsHistoryFragment extends Fragment {

    private PrinterHelper printerHelper;
    private PrinterSettingsHistoryAdapter adapter;
    private ListView printerList;
    private MaterialEditText name;
    private MaterialEditText ip;
    private MaterialEditText port;
    private FancyButton btnTestPrint;
    private FancyButton btnDelete;
    private ConstraintLayout checkBoxLayout;
    private TextView checkBoxText;
    private CustomCheckBox checkBox;
    private Printer currentPrinter;
    private FloatingActionButton fab;
    private ConstraintLayout backLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_printer_settings_history, container, false);

        PrinterSettingsActivity.CURRENT_SCREEN = R.layout.fragment_printer_settings_history;
        printerHelper = PrinterHelper.getInstance();
        printerList = view.findViewById(R.id.printer_settings_history_list);
        name = view.findViewById(R.id.printer_settings_history_name_field);
        ip = view.findViewById(R.id.printer_settings_history_ip_field);
        port = view.findViewById(R.id.printer_settings_history_port_field);
        btnTestPrint = view.findViewById(R.id.btn_printer_history_test_print);
        btnDelete = view.findViewById(R.id.btn_printer_history_delete);
        checkBoxLayout = view.findViewById(R.id.printer_settings_history_checkbox_layout);
        checkBoxText = view.findViewById(R.id.printer_settings_history_checkbox_text);
        checkBox = view.findViewById(R.id.printer_settings_history_checkbox);
        fab = view.findViewById(R.id.btn_printer_settings_history_add);
        backLayout = view.findViewById(R.id.action_bar_printer_history);
        clearData();

        adapter = new PrinterSettingsHistoryAdapter(view.getContext(), printerHelper.getPrinters());
        printerList.setAdapter(adapter);

        printerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Printer printer = (Printer) adapterView.getItemAtPosition(i);
                currentPrinter = printer;
                setData(printer);
            }
        });

        checkBoxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPrinter != null) {
                    if (currentPrinter.getSelected()) {
                        checkBox.setChecked(false);
                        printerHelper.unSelectPrinter(currentPrinter.getIp(), adapter);
                    } else {
                        checkBox.setChecked(true);
                        printerHelper.selectPrinter(currentPrinter.getIp(), adapter);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // NOTHING
            }
        });

        btnTestPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPrinter != null) {
                    printerHelper.testPrint(Storage.getStorage().getContext(),
                            currentPrinter.getIp(), currentPrinter.getPort());
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPrinter != null) {
                    printerHelper.deletePrinter(currentPrinter.getIp(), adapter);
                    adapter.notifyDataSetChanged();
                    clearData();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                PrinterSettingsAddFragment printerSettingsAddFragment = new PrinterSettingsAddFragment();
                transaction.replace(R.id.printer_settings_container, printerSettingsAddFragment);
                transaction.commit();
            }
        });

        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        return view;
    }

    private void setData(Printer printer) {
        name.setText(printer.getName());
        ip.setText(printer.getIp());
        port.setText(printer.getPort());
        btnTestPrint.setEnabled(true);
        btnDelete.setEnabled(true);
        checkBoxLayout.setEnabled(true);
        checkBoxText.setEnabled(true);
        checkBoxText.setTextColor(Storage.getStorage().getContext().getResources().getColor(android.R.color.black));
        checkBox.setEnabled(true);
        checkBox.setChecked(printer.getSelected());
    }

    private void clearData() {
        name.setText(Storage.EMPTY);
        ip.setText(Storage.EMPTY);
        port.setText(Storage.EMPTY);
        btnTestPrint.setEnabled(false);
        btnDelete.setEnabled(false);
        checkBoxLayout.setEnabled(false);
        checkBoxText.setEnabled(false);
        checkBoxText.setTextColor(Storage.getStorage().getContext().getResources().getColor(android.R.color.secondary_text_dark));
        checkBox.setEnabled(false);
        checkBox.setChecked(false);
    }

}
