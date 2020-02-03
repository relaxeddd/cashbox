package ru.cashbox.android.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import lombok.Setter;
import ru.cashbox.android.R;
import ru.cashbox.android.model.check.CheckItem;
import ru.cashbox.android.saver.CheckStateSaver;
import ru.cashbox.android.saver.SlideViewSaver;
import ru.cashbox.android.utils.BillHelper;
import ru.cashbox.android.utils.Storage;
import ru.rambler.libs.swipe_layout.SwipeLayout;

public class CheckAdapter extends BaseAdapter {

    private Context context;
    private Storage storage;
    private LayoutInflater inflater;
    @Setter
    private List<CheckItem> checkItems;
    private CheckStateSaver checkStateSaver;
    private SlideViewSaver slideViewSaver;
    private BillHelper billHelper;

    public CheckAdapter(Context context, List<CheckItem> checkItems) {
        storage = Storage.getStorage();
        billHelper = BillHelper.getInstance();
        this.context = context;
        this.checkItems = checkItems;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        checkStateSaver = CheckStateSaver.getInstance();
        slideViewSaver = SlideViewSaver.getInstance();
    }

    @Override
    public int getCount() {
        return checkItems.size();
    }

    @Override
    public CheckItem getItem(int i) {
        return checkItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.check_item_position, viewGroup, false);
        }

        final SwipeLayout swipeLayout = convertView.findViewById(R.id.swipe_layout);
        ImageButton actionPlus = convertView.findViewById(R.id.check_item_action_plus);
        ImageButton actionMinus = convertView.findViewById(R.id.check_item_action_minus);
        ImageButton actionDelete = convertView.findViewById(R.id.check_item_action_delete);

        TextView title = convertView.findViewById(R.id.check_list_item_title);
        TextView amount = convertView.findViewById(R.id.check_list_item_amount);
        TextView price = convertView.findViewById(R.id.check_list_item_price);
        TextView total = convertView.findViewById(R.id.check_list_item_total);

        CheckItem checkItem = getItem(position);
        title.setText(checkItem.getName());
        amount.setText(String.valueOf(checkItem.getAmount()));
        price.setText(String.valueOf(checkItem.getPrice()));
        total.setText(String.valueOf(checkItem.getFullPrice()));
        actionPlus.setImageResource(R.drawable.plus);
        actionMinus.setImageResource(R.drawable.minus);
        actionDelete.setImageResource(R.drawable.trash);

        actionPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckItem item = getItem(position);
                item.setAmount(item.getAmount() + 1);
                item.setFullPrice(item.getAmount() * item.getPrice());
                checkStateSaver.updateAdapterAndCost();
                billHelper.updateCurrentCheck();
            }
        });

        actionMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckItem item = getItem(position);
                if (item.getAmount() != 1) {
                    item.setAmount(item.getAmount() - 1);
                    item.setFullPrice(item.getAmount() * item.getPrice());
                    checkStateSaver.updateAdapterAndCost();
                    billHelper.updateCurrentCheck();
                }
            }
        });

        actionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkStateSaver.removeItemFromCurrentCheck(position);
                swipeLayout.reset();
                billHelper.updateCurrentCheck();
            }
        });

        slideViewSaver.add(swipeLayout);

        return convertView;
    }

}
