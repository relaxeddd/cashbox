package ru.cashbox.android.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.cashbox.android.R;
import ru.cashbox.android.model.Check;
import ru.cashbox.android.model.CheckItem;
import ru.cashbox.android.utils.Storage;

public class CheckPopupHistoryAdapter extends BaseAdapter {

    private List<Check> checks;
    private Storage storage;
    private LayoutInflater inflater;
    private Context context;

    public CheckPopupHistoryAdapter(Context context, List<Check> checks) {
        storage = Storage.getStorage();
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.checks = checks;
    }

    @Override
    public int getCount() {
        return checks.size();
    }

    @Override
    public Check getItem(int i) {
        return checks.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.check_popup_history, viewGroup, false);
        }

        Check check = getItem(i);
        TextView title = view.findViewById(R.id.check_popup_history_number_title);
        TextView itemsArea = view.findViewById(R.id.check_popup_history_items);
        ImageView bottomLine = view.findViewById(R.id.check_popup_history_bottom_line);
        bottomLine.setBackgroundColor(storage.getContext().getResources()
                .getColor(R.color.colorHalfTransparent));

        title.setText(storage.getContext().getString(R.string.check) + "№" + check.getNumber());
        if (check.getItems() != null) {
            itemsArea.setText(buildItemsText(check.getItems()));
        }

        return view;
    }

    private String buildItemsText(List<CheckItem> checkItems) {
        StringBuilder itemsText = new StringBuilder();
        String symbol = "";
        for (CheckItem checkItem : checkItems) {
            itemsText.append(symbol);
            symbol = ", ";
            if (checkItem.getAmount() > 1) {
                itemsText.append(checkItem.getName()).append(" x").append(checkItem.getAmount());
            } else {
                itemsText.append(checkItem.getName());
            }
        }
        return itemsText.toString();
    }

}
