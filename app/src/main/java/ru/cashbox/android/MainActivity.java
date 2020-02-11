package ru.cashbox.android;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import mehdi.sakout.fancybuttons.FancyButton;
import ru.cashbox.android.adapter.CheckPopupHistoryAdapter;
import ru.cashbox.android.auth.LoginEmployeeActivity;
import ru.cashbox.android.auth.Logout;
import ru.cashbox.android.common.HttpStatusKt;
import ru.cashbox.android.fragment.CheckFragment;
import ru.cashbox.android.fragment.ElementFragment;
import ru.cashbox.android.fragment.SettingsFragment;
import ru.cashbox.android.model.Check;
import ru.cashbox.android.model.CheckItem;
import ru.cashbox.android.model.CheckItemCategoryType;
import ru.cashbox.android.saver.CheckStateSaver;
import ru.cashbox.android.saver.TechMapSlideSaver;
import ru.cashbox.android.utils.BillHelper;
import ru.cashbox.android.utils.PrinterHelper;
import ru.cashbox.android.utils.Storage;

public class MainActivity extends AppCompatActivity implements Logout {

    private static final String MAIN_TAG = "MAIN";

    private Storage storage;
    private CheckFragment checkFragment;
    private SettingsFragment settingsFragment;
    private ElementFragment elementFragment;
    private FragmentTransaction transaction;
    private CheckStateSaver checkStateSaver;
    private ImageButton btnHome;
    private ImageButton btnHistory;
    private ImageButton btnSettings;
    private PrinterHelper printerHelper;
    private SlideUp slideUp;
    private View dim;
    private View slideView;
    private RecyclerView techMapGridView;
    private ImageButton btnCloseSlide;
    private TextView totalText;
    private FancyButton btnAddToCheckSlide;
    private TechMapSlideSaver slideSaver;
    private BillHelper billHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        storage = Storage.getStorage();
        checkStateSaver = CheckStateSaver.getInstance();
        slideSaver = TechMapSlideSaver.getInstance();
        billHelper = BillHelper.getInstance();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnHome = findViewById(R.id.btn_action_bar_category_home);
        btnHistory = findViewById(R.id.btn_action_bar_category_history);
        btnSettings = findViewById(R.id.btn_action_bar_category_settings);
        checkFragment = new CheckFragment();
        elementFragment = new ElementFragment();
        settingsFragment = new SettingsFragment();
        printerHelper = PrinterHelper.getInstance();
        techMapGridView = findViewById(R.id.techmap_grid);
        btnCloseSlide = findViewById(R.id.btn_techmap_slide_close);
        totalText = findViewById(R.id.techmap_slide_total);
        btnAddToCheckSlide = findViewById(R.id.btn_techmap_add_to_check);

        btnHome.setColorFilter(getResources().getColor(R.color.colorPrimaryDark),
                PorterDuff.Mode.SRC_ATOP);
        btnHistory.setColorFilter(getResources().getColor(R.color.colorPrimaryDark),
                PorterDuff.Mode.SRC_ATOP);
        btnSettings.setColorFilter(getResources().getColor(R.color.colorPrimaryDark),
                PorterDuff.Mode.SRC_ATOP);

        techMapGridView.setHasFixedSize(true);
        techMapGridView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));

        slideView = findViewById(R.id.techmap_slide_view);
        dim = findViewById(R.id.dim);
        slideUp = new SlideUpBuilder(slideView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
                        float value = 1 - (percent / 100);
                        if (value > 0.0) {
                            dim.setClickable(true);
                        }
                        if (value == 0.0) {
                            dim.setClickable(false);
                        }
                        dim.setAlpha(value);
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {

                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .withStartState(SlideUp.State.HIDDEN)
                .build();

        TechMapSlideSaver.getInstance().init(slideUp, techMapGridView, totalText);

        transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.layout_check_container, checkFragment);
        transaction.replace(R.id.layout_element_container, elementFragment);
        transaction.commit();

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elementFragment = new ElementFragment();
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.layout_element_container, elementFragment);
                transaction.commit();
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStateSaver.getClosedChecks().size() > 0) {
                    startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "История чеков пуста", Toast.LENGTH_SHORT).show();
                }
                //showChecksHistoryPicker(v);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.layout_element_container, settingsFragment);
                transaction.commit();
            }
        });

        btnCloseSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp.hide();
            }
        });

        btnAddToCheckSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String build = slideSaver.getAdapter().build();
                CheckItem checkItemBuild = new CheckItem(slideSaver.getAdapter().getTechmapId(), "", build, 1,
                        Double.parseDouble(slideSaver.getTotal()), Double.parseDouble(slideSaver.getTotal()),
                        CheckItemCategoryType.TECHMAP, slideSaver.getAdapter().getModificators());

                if (checkStateSaver.getCurrentCheck() != null) {
                    checkStateSaver.addItemToCurrentCheck(checkItemBuild);
                    billHelper.updateCurrentCheck();
                } else {
                    billHelper.createCheckAndInsertItem(checkItemBuild);
                }

                slideUp.hide();
            }
        });
        if (!getIntent().getBooleanExtra("isOpenedSession", true)) {
            billHelper.createBill();
        } else {
            checkStateSaver.openLastCheck();
        }
    }

    private void showChecksHistoryPicker(View view) {
        final ListPopupWindow popupWindow = new ListPopupWindow(storage.getContext());
        CheckPopupHistoryAdapter checkPopupAdapter = new CheckPopupHistoryAdapter(storage.getContext(),
                checkStateSaver.getClosedChecks());
        popupWindow.setAnchorView(view);
        popupWindow.setAdapter(checkPopupAdapter);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setWidth(Storage.DEFAULT_POPUP_HISTORY_WIDTH);

        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Check check = (Check) adapterView.getItemAtPosition(i);
                printerHelper.printCheck(storage.getContext(), check);
                popupWindow.dismiss();
            }
        });
        popupWindow.show();
    }

    @Override
    public void onFinishedLogout(Integer code, String value) {
        if (code == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.server_connect_error),
                    Toast.LENGTH_SHORT).show();
            Log.e(MAIN_TAG, getString(R.string.server_connect_error));
            return;
        }
        switch (code) {
            case HttpStatusKt.OK:
                checkStateSaver.setup();
                startActivity(new Intent(getApplicationContext(), LoginEmployeeActivity.class));
                finishAffinity();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (slideUp.isVisible()) {
            slideUp.hide();
        } else {
            new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                    .setTopColorRes(R.color.colorAccent)
                    .setTitle(R.string.exit_title)
                    .setMessage(R.string.exit_msg)
                    .setPositiveButton(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finishAffinity();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

}
