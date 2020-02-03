package ru.cashbox.android.saver;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import mehdi.sakout.fancybuttons.FancyButton;
import ru.cashbox.android.R;
import ru.cashbox.android.adapter.CheckAdapter;
import ru.cashbox.android.model.Check;
import ru.cashbox.android.model.CheckItem;
import ru.cashbox.android.model.CheckStatus;
import ru.cashbox.android.model.PayType;
import ru.cashbox.android.utils.BillHelper;
import ru.cashbox.android.utils.Storage;

public class CheckStateSaver {
    private static CheckStateSaver checkStateSaver;

    public static CheckStateSaver getInstance() {
        return checkStateSaver = checkStateSaver == null ? new CheckStateSaver() : checkStateSaver;
    }

    private Storage storage;
    private List<Check> checks;
    private CheckAdapter checkAdapter;
    private TextView checkCost;
    private TextView checkCostWithoutDiscount;
    private List<Check> closedChecks;
    private UUID currentCheckHash;
    private SlideViewSaver slideViewSaver;

    private ConstraintLayout checkContentLayout;
    private TextView currentCheckText;
    private FancyButton btnNewCheckCenter;
    private BillHelper billHelper;

    public void init() {
        storage = Storage.getStorage();
        checkAdapter = new CheckAdapter(storage.getContext(), new ArrayList<CheckItem>());
        slideViewSaver = SlideViewSaver.getInstance();
        billHelper = BillHelper.getInstance();
        setup();
    }

    public void setCheckCost(TextView checkCost) {
        this.checkCost = checkCost;
    }

    public void setCheckCostWithoutDiscount(TextView checkCostWithoutDiscount) {
        this.checkCostWithoutDiscount = checkCostWithoutDiscount;
    }

    public List<Check> getClosedChecks() {
        return closedChecks;
    }

    public void setCheckContentLayout(ConstraintLayout checkContentLayout) {
        this.checkContentLayout = checkContentLayout;
    }

    public void setCurrentCheckText(TextView currentCheckText) {
        this.currentCheckText = currentCheckText;
    }

    public void setBtnNewCheckCenter(FancyButton btnNewCheckCenter) {
        this.btnNewCheckCenter = btnNewCheckCenter;
    }

    public void setup() {
        checks = new ArrayList<>();
        closedChecks = new ArrayList<>();
        checkAdapter.setCheckItems(new ArrayList<CheckItem>());
    }

    public Check createCheck(Integer number) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        Check check = new Check(number, UUID.randomUUID(), new ArrayList<>(), CheckStatus.OPENED,
                dateFormat.format(date), "", false, PayType.UNDEFINED);

        checks.add(check);
        return check;
    }

    public void createCheck(Check check) {
        check.setStatus(CheckStatus.OPENED);
        checks.add(check);
        visibleCheck(true);
        setCheckTitle(check.getNumber());
        setCurrentCheck(check.getHash());
        slideViewSaver.returnToStartState();
        slideViewSaver.clear();
    }

    public List<Check> getAllChecks() {
        return checks;
    }

    public Check getCurrentCheck() {
        for (int i = 0; i < checks.size(); i++) {
            if (checks.get(i).getHash().equals(currentCheckHash)) {
                return checks.get(i);
            }
        }
        return null;
    }

    public Check getCheckByHash(UUID hash) {
        if (hash != null) {
            for (int i = 0; i < checks.size(); i++) {
                if (checks.get(i).getHash().equals(hash)) {
                    return checks.get(i);
                }
            }
        }
        return null;
    }

    public void setCurrentCheck(UUID hash) {
        Check check = getCheckByHash(hash);
        if (check != null) {

            slideViewSaver.returnToStartState();
            slideViewSaver.clear();

            currentCheckHash = hash;
            checkAdapter.setCheckItems(check.getItems());
            updateAdapterAndCost();
        }
    }

    public void addItemToCurrentCheck(CheckItem checkItem) {
        Check currentCheck = getCurrentCheck();
        if (currentCheck != null) {
            List<CheckItem> items = currentCheck.getItems();
            items.add(checkItem);
            updateAdapterAndCost();
        }
    }

    public void removeItemFromCurrentCheck(int position) {
        Check currentCheck = getCurrentCheck();
        if (currentCheck != null) {
            List<CheckItem> items = currentCheck.getItems();
            items.remove(position);
            updateAdapterAndCost();
        }
    }

    public List<CheckItem> getItemsFromCurrentCheck() {
        Check currentCheck = getCurrentCheck();
        if (currentCheck != null) {
            return currentCheck.getItems();
        }
        return null;
    }

    public void clearItemsInCurrentCheck() {
        Check check = getCurrentCheck();
        if (check != null) {
            check.getItems().clear();
            updateAdapterAndCost();
            billHelper.updateCurrentCheck();
        }
    }

    private void deleteCheck(UUID hash) {
        Check check = getCheckByHash(hash);
        if (check != null) {
            for (int i = 0; i < checks.size(); i++) {
                if (checks.get(i).getHash().equals(hash)) {
                    checks.remove(i);
                    break;
                }
            }
        }
    }

    public void closeCurrentCheck() {
        Check currentCheck = getCurrentCheck();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        if (currentCheck != null) {
            currentCheck.setStatus(CheckStatus.CLOSED);
            currentCheck.setClosed(dateFormat.format(date));
            closedChecks.add(currentCheck);
            billHelper.closeCheck(currentCheck.getNumber());
            deleteCheck(currentCheck.getHash());
        }
    }

    public void paidCurrentCheck() {
        Check currentCheck = getCurrentCheck();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        if (currentCheck != null) {
            currentCheck.setStatus(CheckStatus.PAYED);
            currentCheck.setClosed(dateFormat.format(date));
            closedChecks.add(currentCheck);
            billHelper.paidCheck(currentCheck.getNumber(), currentCheck.getPayType());
            deleteCheck(currentCheck.getHash());
        }
    }

    public CheckAdapter getAdapter() {
        return checkAdapter;
    }

    public void updateAdapterAndCost() {
        checkAdapter.notifyDataSetChanged();
        checkCost.setText(countFullPrice(getCurrentCheck()));
        checkCostWithoutDiscount.setText(countFullPrice(getCurrentCheck()));
    }

    @SuppressLint("DefaultLocale")
    public String countFullPrice(Check check) {
        double result = 0.0;
        if (check != null) {
            List<CheckItem> checkItems = check.getItems();
            for (int i = 0; i < checkItems.size(); i++) {
                result = result + checkItems.get(i).getFullPrice();
            }
        }
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(result).replaceAll("\\,", ".");
    }

    @SuppressLint("SetTextI18n")
    public void setCheckTitle(Integer checkNumber) {
        if (checkNumber == null) {
            currentCheckText.setText(storage.getContext().getString(R.string.create_check));
            return;
        }
        currentCheckText.setText(storage.getContext().getString(R.string.check) + " №"
                + checkNumber + "▼");
    }

    public void visibleCheck(boolean visible) {
        if (checkContentLayout != null && currentCheckText != null && btnNewCheckCenter != null) {
            checkContentLayout.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            currentCheckText.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            btnNewCheckCenter.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
        }
    }

    public void openLastCheck() {
        if (!checks.isEmpty()) {
            Check last = checks.get(checks.size() - 1);
            setCurrentCheck(last.getHash());
        }
    }
}
