package ru.cashbox.android.saver;

import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mancj.slideup.SlideUp;

import ru.cashbox.android.adapter.TechMapAdapter;

public class TechMapSlideSaver {
    private static TechMapSlideSaver slideSaver;

    private SlideUp slideUp;
    private RecyclerView techMapGridView;
    private TextView totalText;
    private TechMapAdapter adapter;

    public static TechMapSlideSaver getInstance() {
        return slideSaver = slideSaver == null ? new TechMapSlideSaver() : slideSaver;
    }

    public void init(SlideUp slideUp, RecyclerView techMapGridView, TextView totalText) {
        this.slideUp = slideUp;
        this.techMapGridView = techMapGridView;
        this.totalText = totalText;
    }

    public TechMapAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(TechMapAdapter adapter) {
        this.adapter = adapter;
    }

    public void show() {
        slideUp.show();
    }

    public void hide() {
        slideUp.hide();
    }

    public RecyclerView getTechMapGridView() {
        return techMapGridView;
    }

    public void setTotal(String text) {
        totalText.setText(text);
    }

    public String getTotal() {
        return totalText.getText().toString();
    }
}
