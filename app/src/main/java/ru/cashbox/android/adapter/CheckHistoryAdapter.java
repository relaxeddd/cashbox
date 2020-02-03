package ru.cashbox.android.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import ru.cashbox.android.R;
import ru.cashbox.android.model.Check;
import ru.cashbox.android.model.CheckItem;
import ru.cashbox.android.saver.CheckStateSaver;
import ru.cashbox.android.utils.Storage;

public class CheckHistoryAdapter extends BaseAdapter {

    private List<Check> checks;
    private Storage storage;
    private LayoutInflater inflater;
    private CheckStateSaver checkStateSaver;

    public CheckHistoryAdapter(List<Check> checks) {
        this.checks = checks;
        storage = Storage.getStorage();
        checkStateSaver = CheckStateSaver.getInstance();
        inflater = (LayoutInflater) storage.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            view = inflater.inflate(R.layout.check_history_item, viewGroup, false);
        }

        Check check = getItem(i);
        ConstraintLayout layout = view.findViewById(R.id.check_history_layout_bg);
        TextView title = view.findViewById(R.id.check_history_item_title);
        TextView cost = view.findViewById(R.id.check_history_item_cost);
        TextView products = view.findViewById(R.id.check_history_item_products);
        RoundedImageView dot = view.findViewById(R.id.check_history_item_dot);
        title.setText("№" + check.getNumber());
        cost.setText(checkStateSaver.countFullPrice(check) + " руб.");
        products.setText(buildProductLine(check.getItems()));
        switch (check.getStatus()) {
            case PAYED:
                dot.setImageResource(R.color.colorCheckPaid);
                break;
            case CLOSED:
                dot.setImageResource(R.color.colorCheckClosed);
                break;
            case RETURNED:
                dot.setImageResource(R.color.colorCheckRevert);
                break;
            default:
                break;
        }

        if (check.getHistorySelected()) {
            layout.setBackgroundColor(storage.getContext().getResources().getColor(R.color.colorSelected));
            products.setTextColor(storage.getContext().getResources().getColor(android.R.color.white));
        }
        if (!check.getHistorySelected()) {
            layout.setBackgroundColor(storage.getContext().getResources().getColor(android.R.color.white));
            products.setTextColor(storage.getContext().getResources().getColor(android.R.color.darker_gray));
        }

        return view;
    }

    public List<Check> getChecks() {
        return checks;
    }

    private String buildProductLine(List<CheckItem> items) {
        StringBuilder result = new StringBuilder();
        String symbol = Storage.EMPTY;
        for (int i = 0; i < items.size(); i++) {
            CheckItem item = items.get(i);
            result.append(symbol);
            symbol = ", ";
            result.append(item.getName());
        }
        return result.toString();
    }

}
