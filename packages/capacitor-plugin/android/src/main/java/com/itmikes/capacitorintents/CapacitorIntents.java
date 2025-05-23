package com.itmikes.capacitorintents;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.webkit.MimeTypeMap;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(name = "CapacitorIntentsPlugin")
public class CapacitorIntents extends Plugin {

    private static final String LOG_TAG = "Capacitor Intents";
    private Map<String, PluginCall> watchingCalls = new HashMap<>();
    private Map<String, BroadcastReceiver> receiverMap = new HashMap<>();

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void registerBroadcastReceiver(PluginCall call) throws JSONException {
        call.setKeepAlive(true);
        requestBroadcastUpdates(call);
        watchingCalls.put(call.getCallbackId(), call);
    }

    @PluginMethod
    public void unregisterBroadcastReceiver(PluginCall call) {
        String callbackId = call.getString("id");
        if (callbackId != null) {
            PluginCall removed = watchingCalls.remove(callbackId);
            if (removed != null) {
                removeReceiver(callbackId);
                removed.release(bridge);
            }
        }
        call.resolve();
    }

    @PluginMethod
    public void sendBroadcastIntent(PluginCall call) {
        String actionToUse = call.getString("action");
        JSObject passingData = call.getObject("value");
        Intent intended = new Intent(actionToUse);
        intended.putExtra("value", passingData.toString());
        this.getContext().sendBroadcast(intended);
        call.resolve();
    }

    @PluginMethod
    public void getDeviceInfo(PluginCall call) {
        PackageManager packageManager = this.getContext().getPackageManager();
        // 检查是否有条码扫描相关的功能
        Boolean status = packageManager.hasSystemFeature("com.google.zxing.client.android.SCAN");
        JSObject jsonObject = new JSObject();
        jsonObject.put("hasBarcodeScanner", status);

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        jsonObject.put("manufacturer", manufacturer);
        jsonObject.put("model", model);

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.getContext());
        boolean hasNfc = nfcAdapter != null && nfcAdapter.isEnabled();
        jsonObject.put("hasRfidReader", hasNfc);

        call.resolve(jsonObject);
    }

    private void requestBroadcastUpdates(final PluginCall call) throws JSONException {
        final String callBackID = call.getCallbackId();
        IntentFilter ifilt = new IntentFilter();
        JSArray jsArr = call.getArray("filters");
        if (jsArr.length() >= 1) {
            for (int i = 0; i < jsArr.length(); i++) {
                ifilt.addAction(jsArr.getString(i));
            }
            receiverMap.put(
                    callBackID,
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            PluginCall refCall = watchingCalls.get(callBackID);
                            if (refCall != null) {
                                JSObject jsO = null;
                                try {
                                    jsO = JSObject.fromJSONObject(getIntentJson(intent));
                                    refCall.resolve(jsO);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.getContext().registerReceiver(receiverMap.get(callBackID), ifilt, Context.RECEIVER_EXPORTED);
            } else {
                this.getContext().registerReceiver(receiverMap.get(callBackID), ifilt);
            }
        } else {
            call.reject("Filters are required: at least 1 entry");
        }
    }

    private void removeReceiver(String callBackID) {
        this.getContext().unregisterReceiver(receiverMap.get(callBackID));
        this.receiverMap.remove(callBackID);
    }

    private static Object toJsonValue(final Object value) throws JSONException {
        //  Credit: https://github.com/napolitano/cordova-plugin-intent
        if (value == null) {
            return null;
        } else if (value instanceof Bundle) {
            final Bundle bundle = (Bundle) value;
            final JSONObject result = new JSONObject();
            for (final String key : bundle.keySet()) {
                result.put(key, toJsonValue(bundle.get(key)));
            }
            return result;
        } else if ((value.getClass().isArray())) {
            final JSONArray result = new JSONArray();
            int length = Array.getLength(value);
            for (int i = 0; i < length; ++i) {
                result.put(i, toJsonValue(Array.get(value, i)));
            }
            return result;
        } else if (value instanceof ArrayList<?>) {
            final ArrayList arrayList = (ArrayList<?>) value;
            final JSONArray result = new JSONArray();
            for (int i = 0; i < arrayList.size(); i++) result.put(toJsonValue(arrayList.get(i)));
            return result;
        } else if (
                value instanceof String ||
                        value instanceof Boolean ||
                        value instanceof Integer ||
                        value instanceof Long ||
                        value instanceof Double
        ) {
            return value;
        } else {
            return String.valueOf(value);
        }
    }

    private static JSONObject toJsonObject(Bundle bundle) {
        //  Credit: https://github.com/napolitano/cordova-plugin-intent
        try {
            return (JSONObject) toJsonValue(bundle);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Cannot convert bundle to JSON: " + e.getMessage(), e);
        }
    }

    private JSONObject getIntentJson(Intent intent) {
        //  Credit: https://github.com/darryncampbell/darryncampbell-cordova-plugin-intent
        JSONObject intentJSON = null;
        ClipData clipData = null;
        JSONObject[] items = null;
        ContentResolver cR = this.getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            clipData = intent.getClipData();
            if (clipData != null) {
                int clipItemCount = clipData.getItemCount();
                items = new JSONObject[clipItemCount];

                for (int i = 0; i < clipItemCount; i++) {
                    ClipData.Item item = clipData.getItemAt(i);

                    try {
                        items[i] = new JSONObject();
                        items[i].put("htmlText", item.getHtmlText());
                        items[i].put("intent", item.getIntent());
                        items[i].put("text", item.getText());
                        items[i].put("uri", item.getUri());

                        if (item.getUri() != null) {
                            String type = cR.getType(item.getUri());
                            String extension = mime.getExtensionFromMimeType(cR.getType(item.getUri()));

                            items[i].put("type", type);
                            items[i].put("extension", extension);
                        }
                    } catch (JSONException e) {
                        Log.d(LOG_TAG, " Error thrown during intent > JSON conversion");
                        Log.d(LOG_TAG, e.getMessage());
                        Log.d(LOG_TAG, Arrays.toString(e.getStackTrace()));
                    }
                }
            }
        }

        try {
            intentJSON = new JSONObject();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (items != null) {
                    intentJSON.put("clipItems", new JSONArray(items));
                }
            }

            intentJSON.put("type", intent.getType());
            intentJSON.put("extras", toJsonObject(intent.getExtras()));
            intentJSON.put("action", intent.getAction());
            intentJSON.put("categories", intent.getCategories());
            intentJSON.put("flags", intent.getFlags());
            intentJSON.put("component", intent.getComponent());
            intentJSON.put("data", intent.getData());
            intentJSON.put("package", intent.getPackage());

            return intentJSON;
        } catch (JSONException e) {
            Log.d(LOG_TAG, " Error thrown during intent > JSON conversion");
            Log.d(LOG_TAG, e.getMessage());
            Log.d(LOG_TAG, Arrays.toString(e.getStackTrace()));

            return null;
        }
    }
}
