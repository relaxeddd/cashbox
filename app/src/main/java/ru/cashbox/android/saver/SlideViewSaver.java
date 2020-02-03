package ru.cashbox.android.saver;

import java.util.ArrayList;
import java.util.List;

import ru.rambler.libs.swipe_layout.SwipeLayout;

public class SlideViewSaver {
    private static SlideViewSaver slideViewSaver;
    private List<SwipeLayout> swipeLayouts;

    public static SlideViewSaver getInstance() {
        return slideViewSaver = slideViewSaver == null ? new SlideViewSaver() : slideViewSaver;
    }

    public void init() {
        swipeLayouts = new ArrayList<>();
    }

    public void add(SwipeLayout swipeLayout) {
        swipeLayouts.add(swipeLayout);
    }

    public void clear() {
        swipeLayouts = new ArrayList<>();
    }

    public void returnToStartState() {
        for (SwipeLayout swipeLayout : swipeLayouts) {
            swipeLayout.reset();
        }
    }

}
