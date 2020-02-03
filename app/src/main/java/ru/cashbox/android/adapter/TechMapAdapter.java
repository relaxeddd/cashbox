package ru.cashbox.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ru.cashbox.android.R;
import ru.cashbox.android.model.BillModificatorWrapper;
import ru.cashbox.android.model.TechMapElement;
import ru.cashbox.android.model.TechMapElementWrapper;
import ru.cashbox.android.utils.Storage;
import ru.rambler.libs.swipe_layout.SwipeLayout;

public class TechMapAdapter extends RecyclerView.Adapter<TechMapAdapter.SimpleViewHolder> {

    private final Context mContext;
    private final List<TechMapElementWrapper> data;
    private final String techMapName;
    private final Long techmapId;
    private Storage storage;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final RoundedImageView image;
        public final SwipeLayout swipeLayout;
        public final ImageButton actionDelete;
        public final TextView count;
        public final CheckBox title;

        public SimpleViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.techmap_element_item_image);
            swipeLayout = view.findViewById(R.id.techmap_swipe_layout);
            actionDelete = view.findViewById(R.id.techmap_item_action_delete);
            count = view.findViewById(R.id.techmap_element_item_count);
            title = view.findViewById(R.id.techmap_element_item_title);
        }
    }

    public TechMapAdapter(Context context, List<TechMapElementWrapper> data, String techMapName, Long techmapId) {
        mContext = context;
        storage = Storage.getStorage();
        this.data = data;
        this.techMapName = techMapName;
        this.techmapId = techmapId;
    }

    public Long getTechmapId() {
        return techmapId;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.techmap_item_position, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        final List<TechMapElement> elements = getAllElements();
        final TechMapElement element = elements.get(position);
        final TechMapElementWrapper group = getGroupByModificatorId(element.getId());
        Picasso.get()
                .load( storage.getBaseApiAddress() + "img/" + elements.get(position).getImageUrl())
                .fit()
                .centerCrop()
                .error(R.color.colorAccent)
                .into(holder.image);

        holder.title.setText(elements.get(position).getName());

        holder.actionDelete.setImageResource(R.drawable.trash);

        if (elements.get(position).getSelected()) {
            holder.title.setChecked(true);
//            holder.image.setBorderColor(mContext.getResources().getColor(R.color.colorAccent));
//            holder.image.setBorderWidth(10f);
        } else {
            holder.title.setChecked(false);
//            holder.image.setBorderColor(mContext.getResources().getColor(android.R.color.black));
//            holder.image.setBorderWidth(10f);
        }

        if (elements.get(position).getCount() > 1) {
            holder.count.setText(String.valueOf(elements.get(position).getCount()));
        } else {
            holder.count.setText(Storage.EMPTY);
        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (group != null) {
                    if (element.getSelected()) {
                        if (group.getMaxSelected() != 1) {
                            if (checkPossibilityAdd(group)) {
                                element.setCount(element.getCount() + 1);
                            }
                        }
                    } else {
                        if (group.getMaxSelected() == 1) {
                            clearAllSelectedInGroup(group);
                            element.setSelected(true);
                        } else {
                            if (checkPossibilityAdd(group)) {
                                element.setSelected(true);
                            }
                        }
                    }
                    notifyDataSetChanged();
                }
            }
        });

//        if (group != null && group.getMaxSelected() == 1) {
//            holder.swipeLayout.setLeftSwipeEnabled(false);
//        }

        holder.swipeLayout.setOnSwipeListener(new SwipeLayout.OnSwipeListener() {
            @Override
            public void onBeginSwipe(SwipeLayout swipeLayout, boolean moveToRight) {

            }

            @Override
            public void onSwipeClampReached(SwipeLayout swipeLayout, boolean moveToRight) {
                if (group != null && getSelectedCountInGroup(group) > 1) {
                    if (element.getCount() > 1) {
                        element.setCount(element.getCount() - 1);
                    } else {
                        element.setSelected(false);
                    }
                    notifyDataSetChanged();
                }
                holder.swipeLayout.reset();
            }

            @Override
            public void onLeftStickyEdge(SwipeLayout swipeLayout, boolean moveToRight) {

            }

            @Override
            public void onRightStickyEdge(SwipeLayout swipeLayout, boolean moveToRight) {

            }
        });

    }

    public String build() {
        List<TechMapElement> allElements = getAllElements();
        List<TechMapElement> result = new ArrayList<>();
        for (int i = 0; i < allElements.size(); i++) {
            TechMapElement element = allElements.get(i);
            if (element.getSelected()) {
                result.add(element);
            }
        }
        StringBuilder title = new StringBuilder();
        String symbol = "";
        title.append(techMapName).append(" (");
        for (int i = 0; i < result.size(); i++) {
            title.append(symbol);
            symbol = ", ";
            title.append(result.get(i).getName()).append(" (")
                    .append(result.get(i).getCount()).append(")");
        }
        title.append(")");
        return title.toString();
    }

    public List<BillModificatorWrapper> getModificators() {
        List<TechMapElement> allElements = getAllElements();
        List<BillModificatorWrapper> result = new ArrayList<>();
        for (int i = 0; i < allElements.size(); i++) {
            TechMapElement element = allElements.get(i);
            if (element.getSelected()) {
                result.add(new BillModificatorWrapper(element.getId(), element.getCount(), element.getPrice()));
            }
        }

        return result;
    }

    private int getSelectedCountInGroup(TechMapElementWrapper group) {
        int count = 0;
        for (int i = 0; i < group.getElements().size(); i++) {
            if (group.getElements().get(i).getSelected()) {
                count += group.getElements().get(i).getCount();
            }
        }
        return count;
    }

    private void clearAllSelectedInGroup(TechMapElementWrapper group) {
        for (int i = 0; i < group.getElements().size(); i++) {
            TechMapElement element = group.getElements().get(i);
            element.setSelected(false);
        }
    }

    private boolean checkPossibilityAdd(TechMapElementWrapper group) {
        int selectedCount = 0;
        for (int i = 0; i < group.getElements().size(); i++) {
            TechMapElement techMapElement = group.getElements().get(i);
            if (techMapElement.getSelected()) {
                selectedCount += techMapElement.getCount();
            }
        }
        if (selectedCount < group.getMaxSelected()) {
            return true;
        }
        return false;
    }

    private TechMapElementWrapper getGroupByModificatorId(Long id) {
        for (int i = 0; i < data.size(); i++) {
            TechMapElementWrapper techMapElementWrapper = data.get(i);
            for (int j = 0; j < techMapElementWrapper.getElements().size(); j++) {
                if (techMapElementWrapper.getElements().get(j).getId() == id) {
                    return techMapElementWrapper;
                }
            }
        }
        return null;
    }

    private List<TechMapElement> getAllElements() {
        List<TechMapElement> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            result.addAll(data.get(i).getElements());
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return getAllElements().size();
    }
}
