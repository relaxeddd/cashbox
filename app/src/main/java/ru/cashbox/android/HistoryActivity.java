package ru.cashbox.android;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.cashbox.android.adapter.CheckHistoryAdapter;
import ru.cashbox.android.adapter.CheckHistoryProductAdapter;
import ru.cashbox.android.model.check.Check;
import ru.cashbox.android.model.check.CheckItem;
import ru.cashbox.android.model.types.CheckStatus;
import ru.cashbox.android.model.types.PayType;
import ru.cashbox.android.query.BillQuery;
import ru.cashbox.android.saver.CheckStateSaver;
import ru.cashbox.android.utils.PrinterHelper;
import ru.cashbox.android.utils.RetrofitInstance;
import ru.cashbox.android.utils.Storage;

public class HistoryActivity extends AppCompatActivity {

    private ListView checkList;
    private ListView productList;
    private CheckStateSaver checkStateSaver;
    private CheckHistoryAdapter checkAdapter;
    private CheckHistoryProductAdapter productAdapter;
    private TextView checkNumber;
    private TextView employeeName;
    private TextView created;
    private Storage storage;
    private FancyButton btnPrint;
    private FancyButton btnActions;
    private Check currentSelectedCheck;
    private TextView status;
    private PrinterHelper printerHelper;
    private ImageButton btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        storage = Storage.getStorage();
        printerHelper = PrinterHelper.getInstance();
        checkStateSaver = CheckStateSaver.getInstance();

        Collections.sort(checkStateSaver.getClosedChecks(), new Comparator<Check>() {
            @Override
            public int compare(Check checkTwo, Check checkOne)
            {
                return  checkOne.getNumber().compareTo(checkTwo.getNumber());
            }
        });

        checkList = findViewById(R.id.history_check_list);
        checkList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        productList = findViewById(R.id.history_product_list);
        checkNumber = findViewById(R.id.check_history_check_number);
        employeeName = findViewById(R.id.check_history_employee_name);
        created = findViewById(R.id.check_history_open_date);
        btnPrint = findViewById(R.id.btn_check_history_print_again);
        btnActions = findViewById(R.id.btn_check_history_change_status);
        checkAdapter = new CheckHistoryAdapter(checkStateSaver.getClosedChecks());
        productAdapter = new CheckHistoryProductAdapter(new ArrayList<CheckItem>());
        status = findViewById(R.id.check_history_status);
        btnClose = findViewById(R.id.btn_history_close_screen);

        btnActions.setVisibility(View.INVISIBLE);
        btnPrint.setVisibility(View.INVISIBLE);

        checkList.setAdapter(checkAdapter);
        productList.addHeaderView(getProductListHeader());
        productList.setAdapter(productAdapter);

        if (!checkStateSaver.getClosedChecks().isEmpty()) {
            Check check = checkStateSaver.getClosedChecks().get(0);
            checkToScreen(check);
        }

        checkList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Check check = (Check) adapterView.getItemAtPosition(i);
                if (check != null) {
                    checkToScreen(check);
                }
            }
        });

        btnActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSelectedCheck != null) {
                    if (currentSelectedCheck.getStatus().equals(CheckStatus.PAYED)) {
                        showPaidMenu(view);
                    }
                    if (currentSelectedCheck.getStatus().equals(CheckStatus.CLOSED)) {
                        showClosedMenu(view);
                    }
                }
            }
        });

        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSelectedCheck != null) {
                    printerHelper.printCheck(getApplicationContext(), currentSelectedCheck);
                }
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void showPaidMenu(View view) {
        Context wrapper = new ContextThemeWrapper(storage.getContext(), R.style.PopupMenuCustom);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.inflate(R.menu.check_history_status_paid);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.check_history_paid_closed:
                        currentSelectedCheck.setStatus(CheckStatus.CLOSED);
                        status.setText(currentSelectedCheck.getStatus().getTranslatedName());
                        checkAdapter.notifyDataSetChanged();
                        changeStatus(CheckStatus.CLOSED);
                        return true;
                    case R.id.check_history_paid_revert:
                        currentSelectedCheck.setStatus(CheckStatus.RETURNED);
                        status.setText(currentSelectedCheck.getStatus().getTranslatedName());
                        btnActions.setVisibility(View.INVISIBLE);
                        btnPrint.setVisibility(View.INVISIBLE);
                        checkAdapter.notifyDataSetChanged();
                        changeStatus(CheckStatus.RETURNED);
                        return true;
                    case R.id.check_history_paid_open:
                        checkStateSaver.createCheck(currentSelectedCheck);
                        status.setText(currentSelectedCheck.getStatus().getTranslatedName());
                        checkStateSaver.getClosedChecks().remove(currentSelectedCheck);
                        checkAdapter.notifyDataSetChanged();
                        changeStatus(CheckStatus.OPENED);
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }

    private void showClosedMenu(final View view) {
        Context wrapper = new ContextThemeWrapper(storage.getContext(), R.style.PopupMenuCustom);
        final PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.inflate(R.menu.check_history_status_closed);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.check_history_closed_pay:
                        if (currentSelectedCheck.getItems().isEmpty()) {
                            new LovelyStandardDialog(view.getContext(), LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                                    .setTopColorRes(R.color.colorAccent)
                                    .setTitle("Ошибка")
                                    .setMessage("Чек пуст. Переведите статус в состояние \"Открыт\"")
                                    .setPositiveButton("Понятно",null)
                                    .show();
                            return false;
                        }
                        popupMenu.dismiss();
                        showPayType(view, currentSelectedCheck);
                        return true;
                    case R.id.check_history_closed_open:
                        checkStateSaver.createCheck(currentSelectedCheck);
                        status.setText(currentSelectedCheck.getStatus().getTranslatedName());
                        checkStateSaver.getClosedChecks().remove(currentSelectedCheck);
                        checkAdapter.notifyDataSetChanged();
                        changeStatus(CheckStatus.OPENED);
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
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
                        check.setStatus(CheckStatus.PAYED);
                        status.setText(check.getStatus().getTranslatedName());
                        checkAdapter.notifyDataSetChanged();
                        changeStatus(CheckStatus.PAYED);
                        return true;
                    case R.id.payed_type_menu_wire:
                        check.setPayType(PayType.WIRE);
                        check.setStatus(CheckStatus.PAYED);
                        status.setText(check.getStatus().getTranslatedName());
                        checkAdapter.notifyDataSetChanged();
                        changeStatus(CheckStatus.PAYED);
                        return true;
                    default:
                        return false;
                }

            }
        });

        popupMenu.show();
    }

    View getProductListHeader() {
        return getLayoutInflater().inflate(R.layout.check_list_header, null);
    }

    private void changeStatus(CheckStatus checkStatus) {
        BillQuery billQuery = RetrofitInstance.getRetrofit().create(BillQuery.class);
        if (currentSelectedCheck != null) {
            billQuery.changeStatus(storage.getUserEmployeeSession().getToken(),
                    currentSelectedCheck.getNumber(), checkStatus.name()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }

    private void unselectAll() {
        List<Check> checks = checkAdapter.getChecks();
        for (Check check : checks) {
            check.setHistorySelected(false);
        }
    }

    private void checkToScreen(Check check) {
        currentSelectedCheck = check;
        checkNumber.setText(String.valueOf(check.getNumber()));
        employeeName.setText(storage.getUserEmployeeSession().getUser().getFullName());
        created.setText(check.getCreated());
        status.setText(check.getStatus().getTranslatedName());
        productAdapter.setItems(check.getItems());
        productAdapter.notifyDataSetChanged();
        btnActions.setVisibility(check.getStatus().equals(CheckStatus.RETURNED) ?
                View.INVISIBLE : View.VISIBLE);
        btnPrint.setVisibility(check.getStatus().equals(CheckStatus.RETURNED) ?
                View.INVISIBLE : View.VISIBLE);
        unselectAll();
        check.setHistorySelected(true);
        checkAdapter.notifyDataSetChanged();
    }

}
