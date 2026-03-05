package com.mercury1089.Scouting_App_2026;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.mercury1089.Scouting_App_2026.listeners.UpdateListener;

import java.util.LinkedHashMap;

public class Endgame extends Fragment implements UpdateListener {

    private LinkedHashMap<String, String> setupHashMap;
    private LinkedHashMap<String, String> endgameHashMap;

    // Fuel section
    private RadioGroup collectingCounterToggle;
    private RadioGroup ferryingCounterToggle;
    private RadioGroup startLevelToggle;
    private RadioGroup stopLevelToggle;
    private RadioGroup missedCounterToggle;

    // Climbing section
    private RadioGroup attemptedClimbToggle;
    private RadioGroup successfulClimbedToggle;
    private RadioGroup successfullyClimbedLocationToggle;

    // Other controls
    private Switch noShowSwitch;
    private Button nextButtonEndGame;

    // Timer & animation
    private TextView timerID;
    private TextView secondsRemaining;
    private TextView postMatchWarning;
    private ImageView topEdgeBar, bottomEdgeBar, leftEdgeBar, rightEdgeBar;

    private static CountDownTimer timer;
    private boolean firstTime = true;
    private boolean running = true;
    private MatchActivity context;

    // Running counts
    private int collectingCount = 0;
    private int ferryingCount   = 0;
    private int missedCount     = 0;

    public static Endgame newInstance() {
        Endgame fragment = new Endgame();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = (MatchActivity) getActivity();
        try {
            return inflater.inflate(R.layout.endgame_screen, container, false);
        } catch (InflateException e) {
            Log.d("Endgame", "Inflate error: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.SETUP);
        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.AUTON);
        setupHashMap   = HashMapManager.getSetupHashMap();
        endgameHashMap = HashMapManager.getAutonHashMap();

        // Link views
        collectingCounterToggle          = getView().findViewById(R.id.CollectingCounterToggle);
        ferryingCounterToggle            = getView().findViewById(R.id.FerryingCounterToggle);
        startLevelToggle                 = getView().findViewById(R.id.StartLevelToggle);
        stopLevelToggle                  = getView().findViewById(R.id.StopLevelToggle);
        missedCounterToggle              = getView().findViewById(R.id.MissedCounterToggle);
        attemptedClimbToggle             = getView().findViewById(R.id.AttemptedClimbToggle);
        successfulClimbedToggle          = getView().findViewById(R.id.SuccessfulClimbed);
        successfullyClimbedLocationToggle = getView().findViewById(R.id.SuccessfullyClimbedLocation);
        noShowSwitch                     = getView().findViewById(R.id.NoShowSwitch);
        nextButtonEndGame                = getView().findViewById(R.id.NextButtonEndGame);
        timerID                          = getView().findViewById(R.id.IDEndGameSeconds1);
        secondsRemaining                 = getView().findViewById(R.id.EndGameSeconds);
        postMatchWarning                 = getView().findViewById(R.id.PostMatchWarning);
        topEdgeBar                       = getView().findViewById(R.id.topEdgeBar);
        bottomEdgeBar                    = getView().findViewById(R.id.bottomEdgeBar);
        leftEdgeBar                      = getView().findViewById(R.id.leftEdgeBar);
        rightEdgeBar                     = getView().findViewById(R.id.rightEdgeBar);

        loadEndgameData();
        setupCounterListeners();
        setupCascadingListeners();
        setupTimer();
    }

    // ─────────────────────────────────────────
    // COUNTER LISTENERS
    // ─────────────────────────────────────────

    private void setupCounterListeners() {
        collectingCounterToggle.setOnCheckedChangeListener((g, id) -> {
            if (id == R.id.CollectingCounter) return;
            collectingCount = clamp(collectingCount + deltaFor(id,
                    R.id.CollectingMinus10, R.id.CollectingMinus5, R.id.CollectingMinus,
                    R.id.CollectingPlus,    R.id.CollectingPlus5,  R.id.CollectingPlus10));
            refreshDisplay(collectingCounterToggle, R.id.CollectingCounter, collectingCount);
        });

        ferryingCounterToggle.setOnCheckedChangeListener((g, id) -> {
            if (id == R.id.FerryingZero) return;
            ferryingCount = clamp(ferryingCount + deltaFor(id,
                    R.id.FerryingMinus10, R.id.FerryingMinus5, R.id.FerryingMinus,
                    R.id.FerryingPlus,    R.id.FerryingPlus5,  R.id.FerryingPlus10));
            refreshDisplay(ferryingCounterToggle, R.id.FerryingZero, ferryingCount);
        });

        missedCounterToggle.setOnCheckedChangeListener((g, id) -> {
            if (id == R.id.MissedZero) return;
            missedCount = clamp(missedCount + deltaFor(id,
                    R.id.MissedMinus10, R.id.MissedMinus5, R.id.MissedMinus,
                    R.id.MissedPlus,    R.id.MissedPlus5,  R.id.MissedPlus10));
            refreshDisplay(missedCounterToggle, R.id.MissedZero, missedCount);
        });
    }

    private int deltaFor(int id, int m10, int m5, int m1, int p1, int p5, int p10) {
        if (id == m10) return -10;
        if (id == m5)  return -5;
        if (id == m1)  return -1;
        if (id == p1)  return +1;
        if (id == p5)  return +5;
        if (id == p10) return +10;
        return 0;
    }

    private int clamp(int value) { return Math.max(0, value); }

    private void refreshDisplay(RadioGroup group, int displayId, int count) {
        RadioButton display = group.findViewById(displayId);
        if (display != null) display.setText(String.valueOf(count));
        group.setOnCheckedChangeListener(null);
        group.check(displayId);
        if      (group == collectingCounterToggle) setupCollectingListener();
        else if (group == ferryingCounterToggle)   setupFerryingListener();
        else if (group == missedCounterToggle)      setupMissedListener();
    }

    private void setupCollectingListener() {
        collectingCounterToggle.setOnCheckedChangeListener((g, id) -> {
            if (id == R.id.CollectingCounter) return;
            collectingCount = clamp(collectingCount + deltaFor(id,
                    R.id.CollectingMinus10, R.id.CollectingMinus5, R.id.CollectingMinus,
                    R.id.CollectingPlus,    R.id.CollectingPlus5,  R.id.CollectingPlus10));
            refreshDisplay(collectingCounterToggle, R.id.CollectingCounter, collectingCount);
        });
    }

    private void setupFerryingListener() {
        ferryingCounterToggle.setOnCheckedChangeListener((g, id) -> {
            if (id == R.id.FerryingZero) return;
            ferryingCount = clamp(ferryingCount + deltaFor(id,
                    R.id.FerryingMinus10, R.id.FerryingMinus5, R.id.FerryingMinus,
                    R.id.FerryingPlus,    R.id.FerryingPlus5,  R.id.FerryingPlus10));
            refreshDisplay(ferryingCounterToggle, R.id.FerryingZero, ferryingCount);
        });
    }

    private void setupMissedListener() {
        missedCounterToggle.setOnCheckedChangeListener((g, id) -> {
            if (id == R.id.MissedZero) return;
            missedCount = clamp(missedCount + deltaFor(id,
                    R.id.MissedMinus10, R.id.MissedMinus5, R.id.MissedMinus,
                    R.id.MissedPlus,    R.id.MissedPlus5,  R.id.MissedPlus10));
            refreshDisplay(missedCounterToggle, R.id.MissedZero, missedCount);
        });
    }

    // ─────────────────────────────────────────
    // CASCADING LOGIC
    // ─────────────────────────────────────────

    private void setupCascadingListeners() {
        startLevelToggle.setOnCheckedChangeListener((g, id) -> updateFuelStates());
        stopLevelToggle.setOnCheckedChangeListener((g, id)  -> updateFuelStates());
        updateFuelStates();

        attemptedClimbToggle.setOnCheckedChangeListener((g, id)    -> updateClimbStates());
        successfulClimbedToggle.setOnCheckedChangeListener((g, id) -> updateClimbStates());
        updateClimbStates();
    }

    private void updateFuelStates() {
        boolean bothSet = !getLevelValue(startLevelToggle).equals("EMPTY")
                && !getLevelValue(stopLevelToggle).equals("EMPTY");
        setGroupEnabled(missedCounterToggle, bothSet);
    }

    private void updateClimbStates() {
        String attempted  = getSelectedText(attemptedClimbToggle, "");
        String successful = getSelectedText(successfulClimbedToggle, "");
        setGroupEnabled(successfullyClimbedLocationToggle, "1".equals(attempted) && "1".equals(successful));
    }

    // ─────────────────────────────────────────
    // BUTTON LISTENERS
    // ─────────────────────────────────────────


    // ─────────────────────────────────────────
    // TIMER — 30 seconds, warning at 5s
    // ─────────────────────────────────────────

    private void setupTimer() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long ms) {
                long secs = ms / 1000;
                secondsRemaining.setText(String.valueOf(secs));
                if (!running) return;

                if (secs <= 5 && secs > 0) {
                    postMatchWarning.setVisibility(View.VISIBLE);
                    timerID.setTextColor(context.getResources().getColor(R.color.banana));
                    timerID.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.timer_yellow, 0, 0, 0);
                    if (vibrator != null) vibrator.vibrate(500);
                    pulseEdgeBars();
                }
            }

            @Override
            public void onFinish() {
                if (!running) return;
                secondsRemaining.setText("0");
                setAllEdgeBars(R.drawable.teleop_warning);
                timerID.setTextColor(context.getResources().getColor(R.color.border_warning));
                timerID.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.timer_red, 0, 0, 0);
                postMatchWarning.setVisibility(View.VISIBLE);
                postMatchWarning.setTextColor(getResources().getColor(R.color.white));
                postMatchWarning.setText(getString(R.string.EndGameError));
            }
        };

        if (firstTime) {
            firstTime = false;
            timer.start();
        }
    }

    private void pulseEdgeBars() {
        for (ImageView bar : new ImageView[]{topEdgeBar, bottomEdgeBar, leftEdgeBar, rightEdgeBar}) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(bar, View.ALPHA, 0f, 1f);
            anim.setDuration(500);
            anim.setRepeatMode(ObjectAnimator.REVERSE);
            anim.setRepeatCount(1);
            anim.start();
        }
    }

    private void setAllEdgeBars(int drawableRes) {
        for (ImageView bar : new ImageView[]{topEdgeBar, bottomEdgeBar, leftEdgeBar, rightEdgeBar})
            bar.setBackground(getResources().getDrawable(drawableRes));
    }

    // ─────────────────────────────────────────
    // GET / SET HELPERS
    // ─────────────────────────────────────────

    private String getSelectedText(RadioGroup group, String defaultVal) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return defaultVal;
        RadioButton btn = group.findViewById(id);
        return btn != null ? btn.getText().toString().trim() : defaultVal;
    }

    private String getLevelValue(RadioGroup group) {
        return getSelectedText(group, "EMPTY");
    }

    private void selectByText(RadioGroup group, String value) {
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton btn = (RadioButton) group.getChildAt(i);
            if (btn.getText().toString().trim().equalsIgnoreCase(value)) {
                group.check(btn.getId());
                return;
            }
        }
        if (group.getChildCount() > 0)
            group.check(((RadioButton) group.getChildAt(0)).getId());
    }

    private void setGroupEnabled(RadioGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); i++)
            group.getChildAt(i).setEnabled(enabled);
    }

    // ─────────────────────────────────────────
    // DATA PERSISTENCE
    // ─────────────────────────────────────────

    private void loadEndgameData() {
        collectingCount = parseCount(hm("Collecting", "0"));
        ferryingCount   = parseCount(hm("Ferrying",   "0"));
        missedCount     = parseCount(hm("Missed",     "0"));

        refreshDisplay(collectingCounterToggle, R.id.CollectingCounter, collectingCount);
        refreshDisplay(ferryingCounterToggle,   R.id.FerryingZero,     ferryingCount);
        refreshDisplay(missedCounterToggle,     R.id.MissedZero,       missedCount);

        selectByText(startLevelToggle, hm("StartLevel", "EMPTY"));
        selectByText(stopLevelToggle,  hm("StopLevel",  "EMPTY"));

        selectByText(attemptedClimbToggle,              hm("AttemptedClimb",    "DID NOT ATTEMPT"));
        selectByText(successfulClimbedToggle,           hm("SuccessfulClimbed", "None"));
        selectByText(successfullyClimbedLocationToggle, hm("ClimbLocation",     "LEFT"));

        noShowSwitch.setChecked("Y".equals(hm("RobotFellOver", "N")));

        updateFuelStates();
        updateClimbStates();
    }

    private void saveEndgameData() {
        endgameHashMap.put("Collecting",        String.valueOf(collectingCount));
        endgameHashMap.put("Ferrying",          String.valueOf(ferryingCount));
        endgameHashMap.put("Missed",            String.valueOf(missedCount));
        endgameHashMap.put("StartLevel",        getLevelValue(startLevelToggle));
        endgameHashMap.put("StopLevel",         getLevelValue(stopLevelToggle));
        endgameHashMap.put("AttemptedClimb",    getSelectedText(attemptedClimbToggle,              "DID NOT ATTEMPT"));
        endgameHashMap.put("SuccessfulClimbed", getSelectedText(successfulClimbedToggle,           "None"));
        endgameHashMap.put("ClimbLocation",     getSelectedText(successfullyClimbedLocationToggle, "LEFT"));
        endgameHashMap.put("RobotFellOver",     noShowSwitch.isChecked() ? "Y" : "N");
        HashMapManager.putAutonHashMap(endgameHashMap);
    }

    private String hm(String key, String def) {
        String v = endgameHashMap.get(key);
        return v != null ? v : def;
    }

    private int parseCount(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }

    // ─────────────────────────────────────────
    // LIFECYCLE
    // ─────────────────────────────────────────

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (isVisibleToUser) {
                setupHashMap   = HashMapManager.getSetupHashMap();
                endgameHashMap = HashMapManager.getAutonHashMap();
                loadEndgameData();
            } else {
                saveEndgameData();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        running = false;
        if (timer != null) timer.cancel();
    }

    @Override
    public void onUpdate() { loadEndgameData(); }
}