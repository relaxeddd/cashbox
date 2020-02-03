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
import ru.cashbox.android.utils.Storage;

public class CheckPopupAdapter extends BaseAdapter {

    private Context context;
    private Storage storage;
    private LayoutInflater inflater;
    private List<Check> checks;

    public CheckPopupAdapter(Context context, List<Check> checks) {
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
            view = inflater.inflate(R.layout.check_popup, viewGroup, false);
        }

        Check check = getItem(i);
        TextView title = view.findViewById(R.id.check_popup_number_title);
        title.setText("№" + check.getNumber());
        ImageView bottomLine = view.findViewById(R.id.check_popup_bottom_line);
        bottomLine.setBackgroundColor(storage.getContext().getResources()
                .getColor(R.color.colorHalfTransparent));

        return view;
    }

}
