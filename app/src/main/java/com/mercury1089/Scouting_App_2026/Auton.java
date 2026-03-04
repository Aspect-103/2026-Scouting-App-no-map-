package com.mercury1089.Scouting_App_2026;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import com.mercury1089.Scouting_App_2026.utils.GenUtils;

import java.util.LinkedHashMap;

public class Auton extends Fragment implements UpdateListener {

    // HashMaps for data persistence
    private LinkedHashMap<String, String> setupHashMap;
    private LinkedHashMap<String, String> autonHashMap;

    // ═════════════════════════════════════════
    // FUEL SECTION — counter toggles
    // ═════════════════════════════════════════
    private RadioGroup collectingCounterToggle;
    private RadioGroup ferryingCounterToggle;
    private RadioGroup startLevelToggle;
    private RadioGroup stopLevelToggle;
    private RadioGroup missedCounterToggle;

    // ═════════════════════════════════════════
    // CLIMBING SECTION
    // ═════════════════════════════════════════
    private RadioGroup attemptedClimbToggle;
    private RadioGroup successfulClimbedToggle;
    private RadioGroup successfullyClimbedLocationToggle;

    // ═════════════════════════════════════════
    // OTHER CONTROLS
    // ═════════════════════════════════════════
    private Switch noShowSwitch;
    private Button saveButton;
    private Button cancelButton;
    private Button nextButtonAuton;

    // ═════════════════════════════════════════
    // TIMER & ANIMATION
    // ═════════════════════════════════════════
    private TextView timerID;
    private TextView secondsRemaining;
    private TextView teleopWarning;

    private ImageView topEdgeBar;
    private ImageView bottomEdgeBar;
    private ImageView leftEdgeBar;
    private ImageView rightEdgeBar;

    private static CountDownTimer timer;
    private boolean firstTime = true;
    private boolean running = true;
    private ValueAnimator teleopButtonAnimation;
    private AnimatorSet animatorSet;
    private MatchActivity context;

    // ═════════════════════════════════════════
    // RUNNING COUNTS (held in memory, persisted to HashMap on save)
    // ═════════════════════════════════════════
    private int collectingCount = 0;
    private int ferryingCount = 0;
    private int missedCount = 0;

    public static Auton newInstance() {
        Auton fragment = new Auton();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = (MatchActivity) getActivity();
        View inflated = null;
        try {
            inflated = inflater.inflate(R.layout.auton_screen, container, false);
        } catch (InflateException e) {
            Log.d("Auton", "Layout inflation error: " + e.getMessage());
            throw e;
        }
        return inflated;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get HashMap data
        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.SETUP);
        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.AUTON);
        setupHashMap = HashMapManager.getSetupHashMap();
        autonHashMap = HashMapManager.getAutonHashMap();

        // ── Fuel section ──────────────────────────────
        collectingCounterToggle = getView().findViewById(R.id.CollectingCounterToggle);
        ferryingCounterToggle   = getView().findViewById(R.id.FerryingCounterToggle);
        startLevelToggle        = getView().findViewById(R.id.StartLevelToggle);
        stopLevelToggle         = getView().findViewById(R.id.StopLevelToggle);
        missedCounterToggle     = getView().findViewById(R.id.MissedCounterToggle);

        // ── Climbing section ──────────────────────────
        attemptedClimbToggle             = getView().findViewById(R.id.AttemptedClimbToggle);
        successfulClimbedToggle          = getView().findViewById(R.id.SuccessfulClimbed);
        successfullyClimbedLocationToggle = getView().findViewById(R.id.SuccessfullyClimbedLocation);

        // ── Other controls ────────────────────────────
        noShowSwitch   = getView().findViewById(R.id.NoShowSwitch);
        saveButton     = getView().findViewById(R.id.SaveButton);
        cancelButton   = getView().findViewById(R.id.CancelButton);
        nextButtonAuton = getView().findViewById(R.id.NextButtonAuton);

        // ── Timer views ───────────────────────────────
        timerID          = getView().findViewById(R.id.IDAutonSeconds1);
        secondsRemaining = getView().findViewById(R.id.AutonSeconds);
        teleopWarning    = getView().findViewById(R.id.TeleopWarning);

        topEdgeBar    = getView().findViewById(R.id.topEdgeBar);
        bottomEdgeBar = getView().findViewById(R.id.bottomEdgeBar);
        leftEdgeBar   = getView().findViewById(R.id.leftEdgeBar);
        rightEdgeBar  = getView().findViewById(R.id.rightEdgeBar);

        // Load saved data into UI
        loadAutonData();

        // ═════════════════════════════════════════
        // COUNTER TOGGLE LISTENERS
        // Each counter toggle adjusts a running int count,
        // then resets the selection back to the display (000) button.
        // ═════════════════════════════════════════

        collectingCounterToggle.setOnCheckedChangeListener((group, checkedId) -> {
            collectingCount = applyCounterDelta(collectingCount, checkedId,
                    R.id.CollectingMinus10, R.id.CollectingMinus5, R.id.CollectingMinus,
                    R.id.CollectingCounter,
                    R.id.CollectingPlus, R.id.CollectingPlus5, R.id.CollectingPlus10);
            updateCounterDisplay(collectingCounterToggle, R.id.CollectingCounter, collectingCount);
        });

        ferryingCounterToggle.setOnCheckedChangeListener((group, checkedId) -> {
            ferryingCount = applyCounterDelta(ferryingCount, checkedId,
                    R.id.FerryingMinus10, R.id.FerryingMinus5, R.id.FerryingMinus,
                    R.id.FerryingZero,
                    R.id.FerryingPlus, R.id.FerryingPlus5, R.id.FerryingPlus10);
            updateCounterDisplay(ferryingCounterToggle, R.id.FerryingZero, ferryingCount);
        });

        missedCounterToggle.setOnCheckedChangeListener((group, checkedId) -> {
            missedCount = applyCounterDelta(missedCount, checkedId,
                    R.id.MissedMinus10, R.id.MissedMinus5, R.id.MissedMinus,
                    R.id.MissedZero,
                    R.id.MissedPlus, R.id.MissedPlus5, R.id.MissedPlus10);
            updateCounterDisplay(missedCounterToggle, R.id.MissedZero, missedCount);
        });

        // ═════════════════════════════════════════
        // CASCADING LOGIC LISTENERS
        // ═════════════════════════════════════════

        RadioGroup.OnCheckedChangeListener fuelStateListener = (group, checkedId) ->
                updateFuelButtonStates();
        startLevelToggle.setOnCheckedChangeListener(fuelStateListener);
        stopLevelToggle.setOnCheckedChangeListener(fuelStateListener);
        updateFuelButtonStates();

        RadioGroup.OnCheckedChangeListener climbingStateListener = (group, checkedId) ->
                updateClimbingButtonStates();
        attemptedClimbToggle.setOnCheckedChangeListener(climbingStateListener);
        successfulClimbedToggle.setOnCheckedChangeListener(climbingStateListener);
        updateClimbingButtonStates();

        // ═════════════════════════════════════════
        // BUTTON LISTENERS
        // ═════════════════════════════════════════

        saveButton.setOnClickListener(v -> {
            saveAutonData();
            Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show();
        });

        cancelButton.setOnClickListener(v -> {
            loadAutonData();
            Toast.makeText(context, "Changes cancelled", Toast.LENGTH_SHORT).show();
        });

        nextButtonAuton.setOnClickListener(v -> {
            saveAutonData();
            context.tabs.getTabAt(1).select();
        });

        // ═════════════════════════════════════════
        // TIMER
        // ═════════════════════════════════════════

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        timer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                secondsRemaining.setText(GenUtils.padLeftZeros("" + millisUntilFinished / 1000, 2));

                if (!running) return;

                // Warning at 3 seconds remaining
                if (millisUntilFinished / 1000 <= 3 && millisUntilFinished / 1000 > 0) {
                    teleopWarning.setVisibility(View.VISIBLE);
                    timerID.setTextColor(context.getResources().getColor(R.color.banana));
                    timerID.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.timer_yellow, 0, 0, 0);

                    if (vibrator != null) vibrator.vibrate(500);

                    ObjectAnimator topEdgeLighter    = ObjectAnimator.ofFloat(topEdgeBar,    View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator bottomEdgeLighter = ObjectAnimator.ofFloat(bottomEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator rightEdgeLighter  = ObjectAnimator.ofFloat(rightEdgeBar,  View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator leftEdgeLighter   = ObjectAnimator.ofFloat(leftEdgeBar,   View.ALPHA, 0.0f, 1.0f);

                    topEdgeLighter.setDuration(500);
                    bottomEdgeLighter.setDuration(500);
                    leftEdgeLighter.setDuration(500);
                    rightEdgeLighter.setDuration(500);

                    topEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);    topEdgeLighter.setRepeatCount(1);
                    bottomEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE); bottomEdgeLighter.setRepeatCount(1);
                    leftEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);   leftEdgeLighter.setRepeatCount(1);
                    rightEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);  rightEdgeLighter.setRepeatCount(1);

                    AnimatorSet edgeSet = new AnimatorSet();
                    edgeSet.playTogether(topEdgeLighter, bottomEdgeLighter, leftEdgeLighter, rightEdgeLighter);
                    edgeSet.start();
                }
            }

            @Override
            public void onFinish() {
                if (running) {
                    secondsRemaining.setText("00");
                    topEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_warning));
                    bottomEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_warning));
                    leftEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_warning));
                    rightEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_warning));
                    timerID.setTextColor(context.getResources().getColor(R.color.border_warning));
                    timerID.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.timer_red, 0, 0, 0);
                    teleopWarning.setVisibility(View.VISIBLE);
                    teleopWarning.setTextColor(getResources().getColor(R.color.white));
                    teleopWarning.setText(getResources().getString(R.string.TeleopError));
                }
            }
        };

        if (firstTime) {
            firstTime = false;
            timer.start();
        }
    }

    // ═════════════════════════════════════════
    // COUNTER LOGIC
    // ═════════════════════════════════════════

    /**
     * Given the currently checked button ID, apply the correct delta to the count.
     * The center "display" button ID is ignored (it's just the readout).
     * Returns the new count (clamped to 0 minimum).
     */
    private int applyCounterDelta(int currentCount, int checkedId,
                                  int minus10Id, int minus5Id, int minusId,
                                  int displayId,
                                  int plusId, int plus5Id, int plus10Id) {
        if (checkedId == displayId) return currentCount; // tapped the display — no change

        int delta = 0;
        if      (checkedId == minus10Id) delta = -10;
        else if (checkedId == minus5Id)  delta = -5;
        else if (checkedId == minusId)   delta = -1;
        else if (checkedId == plusId)    delta = +1;
        else if (checkedId == plus5Id)   delta = +5;
        else if (checkedId == plus10Id)  delta = +10;

        int newCount = currentCount + delta;
        if (newCount < 0) newCount = 0; // clamp at zero
        return newCount;
    }

    /**
     * Updates the display (centre) button's text to show the current count,
     * then snaps selection back to that display button.
     */
    private void updateCounterDisplay(RadioGroup group, int displayButtonId, int count) {
        RadioButton display = group.findViewById(displayButtonId);
        if (display != null) {
            display.setText(String.format("%03d", count));
        }
        // Temporarily remove listener so programmatic check doesn't re-trigger delta logic
        group.setOnCheckedChangeListener(null);
        group.check(displayButtonId);

        // Re-attach the correct listener
        if (group == collectingCounterToggle) {
            group.setOnCheckedChangeListener((g, id) -> {
                collectingCount = applyCounterDelta(collectingCount, id,
                        R.id.CollectingMinus10, R.id.CollectingMinus5, R.id.CollectingMinus,
                        R.id.CollectingCounter,
                        R.id.CollectingPlus, R.id.CollectingPlus5, R.id.CollectingPlus10);
                updateCounterDisplay(collectingCounterToggle, R.id.CollectingCounter, collectingCount);
            });
        } else if (group == ferryingCounterToggle) {
            group.setOnCheckedChangeListener((g, id) -> {
                ferryingCount = applyCounterDelta(ferryingCount, id,
                        R.id.FerryingMinus10, R.id.FerryingMinus5, R.id.FerryingMinus,
                        R.id.FerryingZero,
                        R.id.FerryingPlus, R.id.FerryingPlus5, R.id.FerryingPlus10);
                updateCounterDisplay(ferryingCounterToggle, R.id.FerryingZero, ferryingCount);
            });
        } else if (group == missedCounterToggle) {
            group.setOnCheckedChangeListener((g, id) -> {
                missedCount = applyCounterDelta(missedCount, id,
                        R.id.MissedMinus10, R.id.MissedMinus5, R.id.MissedMinus,
                        R.id.MissedZero,
                        R.id.MissedPlus, R.id.MissedPlus5, R.id.MissedPlus10);
                updateCounterDisplay(missedCounterToggle, R.id.MissedZero, missedCount);
            });
        }
    }

    // ═════════════════════════════════════════
    // CASCADING LOGIC — FUEL
    // Missed is only enabled when BOTH Start & Stop are not Empty
    // ═════════════════════════════════════════

    private void updateFuelButtonStates() {
        String startLevel = getLevelValue(startLevelToggle);
        String stopLevel  = getLevelValue(stopLevelToggle);

        setRadioGroupEnabled(collectingCounterToggle, true);
        setRadioGroupEnabled(ferryingCounterToggle, true);
        setRadioGroupEnabled(startLevelToggle, true);
        setRadioGroupEnabled(stopLevelToggle, true);

        boolean bothSelected = !startLevel.equals("Empty") && !stopLevel.equals("Empty");
        setRadioGroupEnabled(missedCounterToggle, bothSelected);
    }

    // ═════════════════════════════════════════
    // CASCADING LOGIC — CLIMBING
    // Location only enabled when Attempted = "1" AND Successful = "1"
    // ═════════════════════════════════════════

    private void updateClimbingButtonStates() {
        String attempted   = getClimbValue(attemptedClimbToggle);
        String successful  = getClimbValue(successfulClimbedToggle);

        setRadioGroupEnabled(attemptedClimbToggle, true);
        setRadioGroupEnabled(successfulClimbedToggle, true);

        boolean climbed = "1".equals(attempted) && "1".equals(successful);
        setRadioGroupEnabled(successfullyClimbedLocationToggle, climbed);
    }

    // ═════════════════════════════════════════
    // GET VALUES
    // ═════════════════════════════════════════

    private String getLevelValue(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId == -1) return "Empty";
        RadioButton btn = group.findViewById(selectedId);
        if (btn == null) return "Empty";
        String text = btn.getText().toString().trim();
        if (text.toLowerCase().contains("empty")) return "Empty";
        if (text.contains("25"))                  return "25";
        if (text.contains("50"))                  return "50";
        if (text.contains("75"))                  return "75";
        if (text.toLowerCase().contains("full"))  return "Full";
        return text;
    }

    private String getClimbValue(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId == -1) return "DNA";
        RadioButton btn = group.findViewById(selectedId);
        return btn != null ? btn.getText().toString().trim() : "DNA";
    }

    private String getClimbLocationValue() {
        int selectedId = successfullyClimbedLocationToggle.getCheckedRadioButtonId();
        if (selectedId == -1) return "None";
        RadioButton btn = successfullyClimbedLocationToggle.findViewById(selectedId);
        return btn != null ? btn.getText().toString().trim() : "None";
    }

    // ═════════════════════════════════════════
    // SET VALUES
    // ═════════════════════════════════════════

    private void setLevelValue(RadioGroup group, String value) {
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton btn = (RadioButton) group.getChildAt(i);
            String text = btn.getText().toString().trim();
            if (text.equalsIgnoreCase(value)
                    || (value.equals("25") && text.contains("25"))
                    || (value.equals("50") && text.contains("50"))
                    || (value.equals("75") && text.contains("75"))) {
                group.check(btn.getId());
                return;
            }
        }
        if (group.getChildCount() > 0)
            group.check(((RadioButton) group.getChildAt(0)).getId());
    }

    private void setClimbValue(RadioGroup group, String value) {
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton btn = (RadioButton) group.getChildAt(i);
            if (btn.getText().toString().trim().equals(value)) {
                group.check(btn.getId());
                return;
            }
        }
        if (group.getChildCount() > 0)
            group.check(((RadioButton) group.getChildAt(0)).getId());
    }

    // ═════════════════════════════════════════
    // ENABLE / DISABLE
    // ═════════════════════════════════════════

    private void setRadioGroupEnabled(RadioGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(enabled);
        }
    }

    // ═════════════════════════════════════════
    // DATA PERSISTENCE
    // ═════════════════════════════════════════

    private void loadAutonData() {
        // Load counts from HashMap
        collectingCount = parseCount(getHashMapValue("Collecting", "0"));
        ferryingCount   = parseCount(getHashMapValue("Ferrying",   "0"));
        missedCount     = parseCount(getHashMapValue("Missed",     "0"));

        // Refresh counter displays
        updateCounterDisplay(collectingCounterToggle, R.id.CollectingCounter, collectingCount);
        updateCounterDisplay(ferryingCounterToggle,   R.id.FerryingZero,     ferryingCount);
        updateCounterDisplay(missedCounterToggle,     R.id.MissedZero,       missedCount);

        // Load level toggles
        setLevelValue(startLevelToggle, getHashMapValue("StartLevel", "Empty"));
        setLevelValue(stopLevelToggle,  getHashMapValue("StopLevel",  "Empty"));

        // Load climbing toggles
        setClimbValue(attemptedClimbToggle,              getHashMapValue("AttemptedClimb",    "DNA"));
        setClimbValue(successfulClimbedToggle,           getHashMapValue("SuccessfulClimbed", "None"));
        setClimbValue(successfullyClimbedLocationToggle, getHashMapValue("ClimbLocation",     "None"));

        // Load switch
        noShowSwitch.setChecked("Y".equals(getHashMapValue("RobotFellOver", "N")));

        updateFuelButtonStates();
        updateClimbingButtonStates();
    }

    private void saveAutonData() {
        autonHashMap.put("Collecting",        String.valueOf(collectingCount));
        autonHashMap.put("Ferrying",          String.valueOf(ferryingCount));
        autonHashMap.put("Missed",            String.valueOf(missedCount));
        autonHashMap.put("StartLevel",        getLevelValue(startLevelToggle));
        autonHashMap.put("StopLevel",         getLevelValue(stopLevelToggle));
        autonHashMap.put("AttemptedClimb",    getClimbValue(attemptedClimbToggle));
        autonHashMap.put("SuccessfulClimbed", getClimbValue(successfulClimbedToggle));
        autonHashMap.put("ClimbLocation",     getClimbLocationValue());
        autonHashMap.put("RobotFellOver",     noShowSwitch.isChecked() ? "Y" : "N");

        HashMapManager.putAutonHashMap(autonHashMap);
    }

    // ═════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════

    /** API 21-compatible replacement for getOrDefault() */
    private String getHashMapValue(String key, String defaultValue) {
        String value = autonHashMap.get(key);
        return (value != null) ? value : defaultValue;
    }

    private int parseCount(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ═════════════════════════════════════════
    // LIFECYCLE
    // ═════════════════════════════════════════

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (isVisibleToUser) {
                setupHashMap = HashMapManager.getSetupHashMap();
                autonHashMap = HashMapManager.getAutonHashMap();
                loadAutonData();
            } else {
                saveAutonData();
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
    public void onUpdate() {
        loadAutonData();
    }
}