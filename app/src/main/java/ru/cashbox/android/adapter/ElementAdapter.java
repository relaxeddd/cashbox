package ru.cashbox.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import ru.cashbox.android.R;
import ru.cashbox.android.model.Element;
import ru.cashbox.android.utils.Storage;

public class ElementAdapter extends BaseAdapter {

    private Context context;
    private Storage storage;
    private LayoutInflater inflater;
    private List<Element> elements;

    public ElementAdapter(Context context, List<Element> categories) {
        storage = Storage.getStorage();
        this.context = context;
        this.elements = categories;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return elements.size();
    }

    @Override
    public Element getItem(int i) {
        return elements.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item, viewGroup, false);
        }

        Element item = getItem(position);
        RoundedImageView image = view.findViewById(R.id.item_image);
        TextView title = view.findViewById(R.id.item_title);
        title.setText(item.getName());

        Picasso.get()
                .load(storage.getBaseApiAddress() + "img/" + item.getImageUrl())
                .fit()
                .centerCrop()
                .error(R.color.colorAccent)
                .into(image);

        return view;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }
}
