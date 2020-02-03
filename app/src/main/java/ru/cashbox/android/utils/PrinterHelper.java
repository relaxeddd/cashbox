package ru.cashbox.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import lombok.Getter;
import ru.atol.drivers10.fptr.Fptr;
import ru.atol.drivers10.fptr.IFptr;
import ru.cashbox.android.adapter.PrinterSettingsHistoryAdapter;
import ru.cashbox.android.model.auth.User;
import ru.cashbox.android.model.check.Check;
import ru.cashbox.android.model.check.CheckItem;
import ru.cashbox.android.model.printer.Printer;
import ru.cashbox.android.model.printer.PrinterAction;
import ru.cashbox.android.model.types.PrinterConnectionType;
import ru.cashbox.android.model.printer.PrinterQueueWrapper;

public class PrinterHelper {
    private static PrinterHelper printerHelper;
    private String PRINTER_SETTINGS_TAG = "Printer_Settings";
    private boolean printerBusy = false;
    @Getter
    private boolean scanExecuted = false;
    private ScanTask scanTask;
    private Queue<PrinterQueueWrapper> queue = new ArrayDeque<>();

    public static PrinterHelper getInstance() {
        return printerHelper = printerHelper == null ? new PrinterHelper() : printerHelper;
    }

    public interface ScanListener {
        void onScanned(Printer printer);
    }

    private class ScanTask extends AsyncTask<Void, Void, Printer> {

        private Context context;
        private Fptr fptr;
        private ScanListener scanListener;
        private String ip;
        private String port;

        public ScanTask(Context context, ScanListener scanListener) {
            this.context = context;
            this.scanListener = scanListener;
        }

        public ScanTask(Context context, ScanListener scanListener, String ip, String port) {
            this.context = context;
            this.scanListener = scanListener;
            this.ip = ip;
            this.port = port;
        }

        @Override
        protected void onPreExecute() {
            scanExecuted = true;
            printerBusy = true;
        }

        @Override
        protected Printer doInBackground(Void... voids) {
            if (ip != null && port != null) {
                fptr = new Fptr(context);

                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL,
                        String.valueOf(IFptr.LIBFPTR_MODEL_ATOL_AUTO));
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT,
                        String.valueOf(IFptr.LIBFPTR_PORT_TCPIP));
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, ip);
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, port);
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE,
                        String.valueOf(IFptr.LIBFPTR_PORT_BR_115200));
                fptr.applySingleSettings();

                fptr.open();

                if (fptr.isOpened()) {
                    fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_STATUS);
                    fptr.queryData();
                    String modelName = fptr.getParamString(IFptr.LIBFPTR_PARAM_MODEL_NAME);
                    Printer printer = Printer.builder()
                            .name(modelName)
                            .ip(ip)
                            .port(Storage.DEFAULT_PRINTER_PORT)
                            .selected(false)
                            .connectionType(PrinterConnectionType.WIFI)
                            .build();
                    fptr.close();
                    fptr.destroy();
                    return printer;
                }
                return null;
            }

            List<Printer> printers = new ArrayList<>();
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                String[] ipSplit = Formatter.formatIpAddress(wifiManager.getConnectionInfo()
                        .getIpAddress()).split("\\.");
                StringBuilder baseIpBuilder = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    baseIpBuilder.append(ipSplit[i]).append(".");
                }
                String baseIp = baseIpBuilder.toString();

                List<String> ips = new ArrayList<>();
                for (int i = 1; i < 256; i++) {
                    if (!scanExecuted) {
                        return null;
                    }
                    try {
                        InetAddress tempAddress = InetAddress.getByName(baseIp + i);
                        if (tempAddress.isReachable(150)) {
                            ips.add(baseIp + i);
                        }
                    } catch (IOException ignore) {
                    }
                }

                for (String ip : ips) {
                    if (!scanExecuted) {
                        return null;
                    }
                    fptr = new Fptr(context);

                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL,
                            String.valueOf(IFptr.LIBFPTR_MODEL_ATOL_AUTO));
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT,
                            String.valueOf(IFptr.LIBFPTR_PORT_TCPIP));
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, ip);
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, Storage.DEFAULT_PRINTER_PORT);
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE,
                            String.valueOf(IFptr.LIBFPTR_PORT_BR_115200));
                    fptr.applySingleSettings();

                    fptr.open();

                    if (fptr.isOpened()) {
                        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_STATUS);
                        fptr.queryData();
                        String modelName = fptr.getParamString(IFptr.LIBFPTR_PARAM_MODEL_NAME);
                        Printer printer = Printer.builder()
                                .name(modelName)
                                .ip(ip)
                                .port(Storage.DEFAULT_PRINTER_PORT)
                                .selected(false)
                                .connectionType(PrinterConnectionType.WIFI)
                                .build();
                        printers.add(printer);
                        fptr.close();
                        fptr.destroy();
                    }
                }

                for (Printer printer : printers) {
                    if (getSavedPrinterByIp(printer.getIp()) == null) {
                        return printer;
                    }
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Printer printer) {
            scanExecuted = false;
            printerBusy = false;
            scanListener.onScanned(printer);
        }
    }

    private class PrintTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        private Check check;

        public PrintTask(Context context, Check check) {
            this.context = context;
            this.check = check;
        }

        @Override
        protected void onPreExecute() {
            printerBusy = true;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Printer selectedPrinter = printerHelper.getSelectedPrinter();
            Storage storage = Storage.getStorage();
            if (selectedPrinter != null) {
                Fptr fptr = new Fptr(context);
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL, String.valueOf(IFptr.LIBFPTR_MODEL_ATOL_AUTO));
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, String.valueOf(IFptr.LIBFPTR_PORT_TCPIP));
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, selectedPrinter.getIp());
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, selectedPrinter.getPort());
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE, String.valueOf(IFptr.LIBFPTR_PORT_BR_115200));
                fptr.applySingleSettings();

                fptr.open();

                User user = storage.getUserEmployeeSession().getUser();
                fptr.setParam(1021, user.getFullName());
                fptr.operatorLogin();

                fptr.setParam(IFptr.LIBFPTR_PARAM_RECEIPT_TYPE, IFptr.LIBFPTR_RT_SELL);
                fptr.openReceipt();

                List<CheckItem> checkItems = check.getItems();
                for (CheckItem checkItem : checkItems) {
                    fptr.setParam(IFptr.LIBFPTR_PARAM_COMMODITY_NAME, checkItem.getName());
                    fptr.setParam(IFptr.LIBFPTR_PARAM_PRICE, checkItem.getPrice());
                    fptr.setParam(IFptr.LIBFPTR_PARAM_QUANTITY, checkItem.getAmount());
                    fptr.setParam(IFptr.LIBFPTR_PARAM_TAX_TYPE, IFptr.LIBFPTR_TAX_VAT0);
                    fptr.registration();
                }

                fptr.setParam(IFptr.LIBFPTR_PARAM_PAYMENT_TYPE, IFptr.LIBFPTR_PT_CASH);
                fptr.payment();

                fptr.closeReceipt();

                fptr.continuePrint();

                fptr.close();
                fptr.destroy();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            nextAction();
        }
    }

    private class CloseShiftTask extends AsyncTask<Void, Void, Void> {

        private Context context;

        public CloseShiftTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            printerBusy = true;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Printer selectedPrinter = printerHelper.getSelectedPrinter();
            Storage storage = Storage.getStorage();
            if (selectedPrinter != null) {
                Fptr fptr = new Fptr(context);
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL, String.valueOf(IFptr.LIBFPTR_MODEL_ATOL_AUTO));
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, String.valueOf(IFptr.LIBFPTR_PORT_TCPIP));
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, selectedPrinter.getIp());
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, selectedPrinter.getPort());
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE, String.valueOf(IFptr.LIBFPTR_PORT_BR_115200));
                fptr.applySingleSettings();

                fptr.open();

                User user = storage.getUserEmployeeSession().getUser();
                fptr.setParam(1021, user.getFullName());
                fptr.operatorLogin();

                fptr.setParam(IFptr.LIBFPTR_PARAM_REPORT_TYPE, IFptr.LIBFPTR_RT_CLOSE_SHIFT);
                fptr.report();

                fptr.close();
                fptr.destroy();

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            nextAction();
        }
    }

    private class TestPrintTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        private String ip;
        private String port;

        public TestPrintTask(Context context, String ip, String port) {
            this.context = context;
            this.ip = ip;
            this.port = port;
        }

        @Override
        protected void onPreExecute() {
            printerBusy = true;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Fptr fptr = new Fptr(context);
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL,
                    String.valueOf(IFptr.LIBFPTR_MODEL_ATOL_AUTO));
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT,
                    String.valueOf(IFptr.LIBFPTR_PORT_TCPIP));
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, ip);
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, port);
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE,
                    String.valueOf(IFptr.LIBFPTR_PORT_BR_115200));
            fptr.applySingleSettings();

            fptr.open();

            if (fptr.isOpened()) {
                fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT, "Успешное подключение\n\n\n\n\n");
                fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER);
                fptr.setParam(IFptr.LIBFPTR_PARAM_FONT, 2);
                fptr.setParam(IFptr.LIBFPTR_PARAM_FONT_DOUBLE_WIDTH, true);
                fptr.setParam(IFptr.LIBFPTR_PARAM_FONT_DOUBLE_HEIGHT, true);
                fptr.printText();
                fptr.cut();
                fptr.close();
                fptr.destroy();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            nextAction();
        }
    }

    private void nextAction() {
        PrinterQueueWrapper poll = queue.poll();
        if (poll != null) {
            List<Object> params = poll.getParams();
            switch (poll.getAction()) {
                case PRINT:
                    Context printerContext = (Context) params.get(0);
                    Check check = (Check) params.get(1);
                    new PrintTask(printerContext, check).execute();
                    break;
                case CLOSE_SHIFT:
                    Context closeShiftContext = (Context) params.get(0);
                    new CloseShiftTask(closeShiftContext).execute();
                    break;
                case TEST_PRINT:
                    Context testPrintContext = (Context) params.get(0);
                    String ip = (String) params.get(1);
                    String port = (String) params.get(2);
                    new TestPrintTask(testPrintContext, ip, port).execute();
                    break;
                default:
                    break;
            }
        } else {
            printerBusy = false;
        }
    }

    public void startScan(Context context, ScanListener scanListener) {
        scanTask = new ScanTask(context, scanListener);
        scanTask.execute();
    }

    public void startScan(Context context, ScanListener scanListener, String ip, String port) {
        scanTask = new ScanTask(context, scanListener, ip, port);
        scanTask.execute();
    }

    public void printCheck(Context context, Check check) {
        if (printerBusy) {
            PrinterQueueWrapper printerQueueWrapper = new PrinterQueueWrapper();
            printerQueueWrapper.setAction(PrinterAction.PRINT);
            List<Object> params = new ArrayList<>();
            params.add(context);
            params.add(check);
            printerQueueWrapper.setParams(params);
            queue.offer(printerQueueWrapper);
        } else {
            new PrintTask(context, check).execute();
        }
    }

    public void closeShift(Context context) {
        if (printerBusy) {
            PrinterQueueWrapper printerQueueWrapper = new PrinterQueueWrapper();
            printerQueueWrapper.setAction(PrinterAction.CLOSE_SHIFT);
            List<Object> params = new ArrayList<>();
            params.add(context);
            printerQueueWrapper.setParams(params);
            queue.offer(printerQueueWrapper);
        } else {
            new CloseShiftTask(context).execute();
        }
    }

    public void closeShiftAsync(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Printer selectedPrinter = getSelectedPrinter();
                if (selectedPrinter != null) {
                    Fptr fptr = new Fptr(context);

                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL,
                            String.valueOf(IFptr.LIBFPTR_MODEL_ATOL_AUTO));
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT,
                            String.valueOf(IFptr.LIBFPTR_PORT_TCPIP));
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, selectedPrinter.getIp());
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, selectedPrinter.getPort());
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE,
                            String.valueOf(IFptr.LIBFPTR_PORT_BR_115200));
                    fptr.applySingleSettings();

                    fptr.open();

                    fptr.setParam(IFptr.LIBFPTR_PARAM_REPORT_TYPE, IFptr.LIBFPTR_RT_CLOSE_SHIFT);
                    fptr.report();

                    fptr.close();
                    fptr.destroy();
                }
            }
        }).start();
    }

    public void testPrint(Context context, String ip, String port) {
        if (printerBusy) {
            PrinterQueueWrapper printerQueueWrapper = new PrinterQueueWrapper();
            printerQueueWrapper.setAction(PrinterAction.TEST_PRINT);
            List<Object> params = new ArrayList<>();
            params.add(context);
            params.add(ip);
            params.add(port);
            printerQueueWrapper.setParams(params);
            queue.offer(printerQueueWrapper);
        } else {
            new TestPrintTask(context, ip, port).execute();
        }
    }

    public void stopScan() {
        scanExecuted = false;
    }

    public void savePrinters(List<Printer> printerList) {
        Storage storage = Storage.getStorage();
        SharedPreferences sharedPreferences = storage.getContext()
                .getSharedPreferences(Storage.APP_NAME + " Settings", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String printerData = gson.toJson(printerList);
        editor.putString(PRINTER_SETTINGS_TAG + storage.getUserTerminalSession().getUser().getId(), printerData);
        editor.apply();
    }

    public List<Printer> getPrinters() {
        Storage storage = Storage.getStorage();
        SharedPreferences sharedPreferences = storage.getContext()
                .getSharedPreferences(Storage.APP_NAME + " Settings", Context.MODE_PRIVATE);
        String printerData = sharedPreferences.getString(PRINTER_SETTINGS_TAG +
                storage.getUserTerminalSession().getUser().getId(), null);
        Gson gson = new Gson();
        if (printerData != null) {
            return gson.fromJson(printerData, new TypeToken<List<Printer>>() {
            }.getType());
        }
        return new ArrayList<>();
    }

    public Printer getSavedPrinterByIp(String ip) {
        List<Printer> printers = getPrinters();
        for (Printer printer : printers) {
            if (printer.getIp().equals(ip)) {
                return printer;
            }
        }
        return null;
    }

    public Printer getSelectedPrinter() {
        List<Printer> printers = getPrinters();
        for (Printer printer : printers) {
            if (printer.getSelected()) {
                return printer;
            }
        }
        return null;
    }

    public void deletePrinter(String ip) {
        List<Printer> printers = getPrinters();
        for (Printer printer : printers) {
            if (printer.getIp().equals(ip)) {
                printers.remove(printer);
                break;
            }
        }
        savePrinters(printers);
    }

    public void deletePrinter(String ip, PrinterSettingsHistoryAdapter adapter) {
        List<Printer> printers = getPrinters();
        for (Printer printer : printers) {
            if (printer.getIp().equals(ip)) {
                printers.remove(printer);
                break;
            }
        }
        List<Printer> adapterPrinters = adapter.getPrinters();
        for (Printer printer : adapterPrinters) {
            if (printer.getIp().equals(ip)) {
                adapterPrinters.remove(printer);
                break;
            }
        }
        savePrinters(printers);
    }

    public void selectPrinter(String ip) {
        List<Printer> printers = getPrinters();
        for (Printer printer : printers) {
            printer.setSelected(false);
            if (printer.getIp().equals(ip)) {
                printer.setSelected(true);
            }
        }
        savePrinters(printers);
    }

    public void selectPrinter(String ip, PrinterSettingsHistoryAdapter adapter) {
        List<Printer> printers = getPrinters();
        for (Printer printer : printers) {
            printer.setSelected(false);
            if (printer.getIp().equals(ip)) {
                printer.setSelected(true);
            }
        }
        List<Printer> adapterPrinters = adapter.getPrinters();
        for (Printer printer : adapterPrinters) {
            printer.setSelected(false);
            if (printer.getIp().equals(ip)) {
                printer.setSelected(true);
            }
        }
        savePrinters(printers);
    }

    public void unSelectPrinter(String ip, PrinterSettingsHistoryAdapter adapter) {
        List<Printer> printers = getPrinters();
        for (Printer printer : printers) {
            if (printer.getIp().equals(ip)) {
                printer.setSelected(false);
            }
        }
        List<Printer> adapterPrinters = adapter.getPrinters();
        for (Printer printer : adapterPrinters) {
            if (printer.getIp().equals(ip)) {
                printer.setSelected(false);
            }
        }
        savePrinters(printers);
    }

}
