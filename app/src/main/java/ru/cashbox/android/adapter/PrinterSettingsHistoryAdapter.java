package ru.cashbox.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import ru.cashbox.android.R;
import ru.cashbox.android.model.printer.Printer;
import ru.cashbox.android.utils.Storage;

public class PrinterSettingsHistoryAdapter extends BaseAdapter {

    private Context context;
    private Storage storage;
    private LayoutInflater inflater;
    @Getter @Setter
    private List<Printer> printers;

    public PrinterSettingsHistoryAdapter(Context context, List<Printer> printers) {
        storage = Storage.getStorage();
        this.context = context;
        this.printers = printers;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return printers.size();
    }

    @Override
    public Printer getItem(int i) {
        return printers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.printer_settings_history_item, viewGroup, false);
        }

        Printer item = getItem(i);
        TextView title = view.findViewById(R.id.printer_settings_item_title);
        title.setText(item.getName());
        TextView ip = view.findViewById(R.id.printer_settings_item_ip);
        ip.setText(item.getIp());
        CustomCheckBox checkBox = view.findViewById(R.id.printer_settings_item_checkbox);
        if (item.getSelected()) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        return view;
    }
}
