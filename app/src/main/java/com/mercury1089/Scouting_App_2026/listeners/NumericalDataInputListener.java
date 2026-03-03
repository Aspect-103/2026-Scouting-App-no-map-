package com.mercury1089.Scouting_App_2026.listeners;

import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class NumericalDataInputListener implements View.OnClickListener {
    private final TextView counterView;
    private HashMap<String, String> map;
    private final String key;
    private final boolean add;
    private final UpdateListener listener;
    public NumericalDataInputListener(TextView counterView, LinkedHashMap<String, String> map, String key, boolean add, UpdateListener listener) {
        this.counterView = counterView;
        this.map = map;
        this.key = key;
        this.add = add; // If add = false, subtract
        this.listener = listener;
    }
    @Override
    public void onClick(View v) {
        int currentCount = Integer.parseInt((String) counterView.getText());
            currentCount += add ? 1 : (currentCount  > 0 ? -1 : 0);
        map.put(key, String.valueOf(currentCount));
        listener.onUpdate();
    }
}
