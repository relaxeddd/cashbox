package ru.cashbox.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ru.cashbox.android.R;
import ru.cashbox.android.model.CheckItem;
import ru.cashbox.android.utils.Storage;

public class CheckHistoryProductAdapter extends BaseAdapter {

    private List<CheckItem> items;
    private Storage storage;
    private LayoutInflater inflater;

    public CheckHistoryProductAdapter(List<CheckItem> items) {
        this.items = items;
        storage = Storage.getStorage();
        inflater = (LayoutInflater) storage.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public CheckItem getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.check_history_product_item, viewGroup, false);
        }

        CheckItem item = getItem(i);
        TextView title = view.findViewById(R.id.check_history_product_item_title);
        TextView amount = view.findViewById(R.id.check_history_product_item_amount);
        TextView price = view.findViewById(R.id.check_history_product_item_price);
        TextView total = view.findViewById(R.id.check_history_product_item_total);

        title.setText(item.getName());
        amount.setText(String.valueOf(item.getAmount()));
        price.setText(String.valueOf(item.getPrice()));
        total.setText(String.valueOf(item.getFullPrice()));

        return view;
    }

    public void setItems(List<CheckItem> items) {
        this.items = items;
    }
}
