package ru.cashbox.android.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;
import ru.cashbox.android.R;
import ru.cashbox.android.adapter.CheckPopupAdapter;
import ru.cashbox.android.model.Check;
import ru.cashbox.android.model.PayType;
import ru.cashbox.android.saver.CheckStateSaver;
import ru.cashbox.android.saver.SlideViewSaver;
import ru.cashbox.android.utils.BillHelper;
import ru.cashbox.android.utils.PrinterHelper;
import ru.cashbox.android.utils.Storage;

public class CheckFragment extends Fragment {

    private Storage storage;
    private CheckStateSaver checkStateSaver;
    private ListView checkList;
    private FancyButton btnCheckPay;
    private TextView checkCost;
    private TextView checkCostWithoutDiscount;
    private TextView currentCheckText;
    private FancyButton btnDots;
    private ConstraintLayout checkContentLayout;
    private PrinterHelper printerHelper;
    private FancyButton btnNewCheckCenter;
    private SlideViewSaver slideViewSaver;
    private BillHelper billHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check, container, false);

        storage = Storage.getStorage();
        checkStateSaver = CheckStateSaver.getInstance();
        slideViewSaver = SlideViewSaver.getInstance();
        billHelper = BillHelper.getInstance();
        btnCheckPay = view.findViewById(R.id.btn_check_pay);
        checkCost = view.findViewById(R.id.check_cost);
        checkCostWithoutDiscount = view.findViewById(R.id.check_cost_without_discount);
        checkList = view.findViewById(R.id.check_list);
        checkList.addHeaderView(getCheckListHeader());
        checkList.setAdapter(checkStateSaver.getAdapter());
        checkStateSaver.setCheckCost(checkCost);
        checkStateSaver.setCheckCostWithoutDiscount(checkCostWithoutDiscount);
        checkStateSaver.updateAdapterAndCost();
        currentCheckText = view.findViewById(R.id.current_check_text);
        btnDots = view.findViewById(R.id.btn_dots);
        checkContentLayout = view.findViewById(R.id.content_check);
        btnNewCheckCenter = view.findViewById(R.id.btn_new_check_center);
        printerHelper = PrinterHelper.getInstance();

        currentCheckText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChecksPicker(view);
            }
        });

        btnDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChecksSettings(view);
            }
        });

        btnCheckPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Check currentCheck = checkStateSaver.getCurrentCheck();
                if (currentCheck != null) {
                    if (currentCheck.getItems().isEmpty()) {
                        new LovelyStandardDialog(view.getContext(), LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                                .setTopColorRes(R.color.colorAccent)
                                .setTitle("Ошибка")
                                .setMessage("Вы не можете оплатить пустой чек")
                                .setPositiveButton("Хорошо", null)
                                .show();
                        return;
                    }
                    showPayType(view, currentCheck);
                }
            }
        });

        checkContentLayout.setVisibility(checkStateSaver.getAllChecks().size() > 0 ? View.VISIBLE
                : View.INVISIBLE);
        btnNewCheckCenter.setVisibility(checkStateSaver.getAllChecks().size() > 0 ? View.INVISIBLE : View.VISIBLE);
        currentCheckText.setVisibility(checkStateSaver.getAllChecks().size() > 0 ? View.VISIBLE : View.INVISIBLE);


        checkStateSaver.setCheckContentLayout(checkContentLayout);
        checkStateSaver.setCurrentCheckText(currentCheckText);
        checkStateSaver.setBtnNewCheckCenter(btnNewCheckCenter);

        btnNewCheckCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billHelper.createBill();
            }
        });

        checkStateSaver.setCheckTitle(checkStateSaver.getAllChecks().size() > 0 ?
                checkStateSaver.getCurrentCheck().getNumber() : null);

        return view;
    }

    private void payCheck() {
        checkStateSaver.paidCurrentCheck();
        afterCheckClosedOrPaid();
    }

    private void closeCheck() {
        checkStateSaver.closeCurrentCheck();
        afterCheckClosedOrPaid();
    }

    private void afterCheckClosedOrPaid() {
        List<Check> allChecks = checkStateSaver.getAllChecks();
        if (allChecks.size() > 0) {
            Check check = allChecks.get(allChecks.size() - 1);
            checkStateSaver.setCurrentCheck(check.getHash());
            checkStateSaver.setCheckTitle(check.getNumber());
        }
        if (allChecks.size() == 0) {
            checkStateSaver.visibleCheck(false);
            checkStateSaver.setCheckTitle(null);
        }
    }

    private void showChecksPicker(View view) {
        final ListPopupWindow popupWindow = new ListPopupWindow(storage.getContext());
        CheckPopupAdapter checkPopupAdapter = new CheckPopupAdapter(view.getContext(),
                checkStateSaver.getAllChecks());
        popupWindow.setAnchorView(view);
        popupWindow.setAdapter(checkPopupAdapter);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setWidth(Storage.DEFAULT_POPUP_WIDTH);

        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Check check = (Check) adapterView.getItemAtPosition(i);
                if (check != null) {
                    checkStateSaver.setCurrentCheck(check.getHash());
                    checkStateSaver.setCheckTitle(check.getNumber());
                }
                popupWindow.dismiss();
            }
        });
        popupWindow.show();
        if (popupWindow.getListView() != null) {
            View checkPickerHeader = getCheckPickerHeader();
            popupWindow.getListView().addHeaderView(checkPickerHeader);
            checkPopupAdapter.notifyDataSetChanged();
            TextView newCheck = checkPickerHeader.findViewById(R.id.check_picker_new_check_text);
            ImageView pickerBottomLine = checkPickerHeader.findViewById(R.id.check_picker_bottom_line);
            pickerBottomLine.setBackgroundColor(checkPickerHeader.getResources().getColor(R.color.colorAccent));
            newCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    billHelper.createBill();
                    popupWindow.dismiss();
                }
            });
        }
    }

    private void showPayType(View view, final Check check) {
        Context wrapper = new ContextThemeWrapper(storage.getContext(), R.style.PopupMenuCustom);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.inflate(R.menu.payed_type_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.payed_type_menu_cash:
                        check.setPayType(PayType.CASH);
                        printerHelper.printCheck(storage.getContext(), check);
                        payCheck();
                        return true;
                    case R.id.payed_type_menu_wire:
                        check.setPayType(PayType.WIRE);
                        printerHelper.printCheck(storage.getContext(), check);
                        payCheck();
                        return true;
                    default:
                        return false;
                }

            }
        });

        popupMenu.show();
    }

    private void showChecksSettings(View view) {
        Context wrapper = new ContextThemeWrapper(storage.getContext(), R.style.PopupMenuCustom);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.inflate(R.menu.check_settings);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.check_settings_clear:
                        checkStateSaver.clearItemsInCurrentCheck();
                        return true;
                    case R.id.check_settings_close:
                        closeCheck();
                        Toast.makeText(storage.getContext(),
                                storage.getContext().getString(R.string.check_close),
                                Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }

    private View getCheckListHeader() {
        return LayoutInflater.from(storage.getContext())
                .inflate(R.layout.check_list_header,
                        null, false);
    }

    private View getCheckPickerHeader() {
        return LayoutInflater.from(storage.getContext())
                .inflate(R.layout.check_picker_header,
                        null, false);
    }

}
