package com.mercury1089.Scouting_App_2026;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Vibrator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import java.util.LinkedHashMap;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.app.Dialog;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.gridlayout.widget.GridLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.mercury1089.Scouting_App_2026.listeners.NumericalDataInputListener;
import com.mercury1089.Scouting_App_2026.listeners.UpdateListener;
import com.mercury1089.Scouting_App_2026.utils.GenUtils;

public class Auton extends Fragment implements UpdateListener {
    //HashMaps for sending QR data between screens
    private LinkedHashMap<String, String> setupHashMap;
    private LinkedHashMap<String, String> autonHashMap;

    // Instructions
    private TextView scoringDirectionsID;

    // Grid layouts for field zones
    private GridLayout redAllianceGrid;
    private GridLayout blueAllianceGrid;
    private Dialog popupDialog;
    private String currentSelectedZone = null;

    // Robot toggle options
    private TextView miscInstructionsID;
    private TextView leaveID, fellOverID;
    private Switch leaveSwitch;
    private Switch fellOverSwitch;

    // Next button
    private Button nextButton;

    // Auton timer views
    private TextView timerID;
    private TextView secondsRemaining;
    private TextView teleopWarning;

    // Auton border image views
    private ImageView topEdgeBar;
    private ImageView bottomEdgeBar;
    private ImageView leftEdgeBar;
    private ImageView rightEdgeBar;

    // other variables
    private static CountDownTimer timer;
    private boolean firstTime = true;
    private boolean running = true;
    private ValueAnimator teleopButtonAnimation;
    private AnimatorSet animatorSet;
    private MatchActivity context;

    public static Auton newInstance() {
        Auton fragment = new Auton();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = (MatchActivity) getActivity();
        View inflated = null;
        try {
            inflated = inflater.inflate(R.layout.fragment_auton, container, false);
        } catch (InflateException e) {
            Log.d("Oncreateview", "ERROR");
            throw e;
        }
        return inflated;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onStart() {
        setupHashMap = HashMapManager.getSetupHashMap();
        autonHashMap = HashMapManager.getAutonHashMap();
        super.onStart();

        // Initialize grid layouts for field zones
        redAllianceGrid = getView().findViewById(R.id.redAllianceGrid);
        blueAllianceGrid = getView().findViewById(R.id.blueAllianceGrid);

        // Link misc direction labels
        miscInstructionsID = getView().findViewById(R.id.IDMiscDirections);
        leaveID = getView().findViewById(R.id.IDLeave);
        leaveSwitch = getView().findViewById(R.id.LeaveSwitch);
        fellOverID = getView().findViewById(R.id.IDFellOver);
        fellOverSwitch = getView().findViewById(R.id.FellOverSwitch);

        // Link timer views
        timerID = getView().findViewById(R.id.IDAutonSeconds1);
        secondsRemaining = getView().findViewById(R.id.AutonSeconds);
        teleopWarning = getView().findViewById(R.id.TeleopWarning);
        scoringDirectionsID = getView().findViewById(R.id.IDPossessionDirections);

        // Link border bars
        topEdgeBar = getView().findViewById(R.id.topEdgeBar);
        bottomEdgeBar = getView().findViewById(R.id.bottomEdgeBar);
        leftEdgeBar = getView().findViewById(R.id.leftEdgeBar);
        rightEdgeBar = getView().findViewById(R.id.rightEdgeBar);

        // Link next button
        nextButton = getView().findViewById(R.id.NextTeleopButton);

        // Get HashMap data (fill with defaults if empty or null)
        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.SETUP);
        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.AUTON);
        setupHashMap = HashMapManager.getSetupHashMap();
        autonHashMap = HashMapManager.getAutonHashMap();

        // Fill in counters with data
        updateXMLObjects();

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        timer = new CountDownTimer(20000, 1000) {

            public void onTick(long millisUntilFinished) {
                secondsRemaining.setText(GenUtils.padLeftZeros("" + millisUntilFinished / 1000, 2));

                if(!running)
                    return;

                if (millisUntilFinished / 1000 <= 3 && millisUntilFinished / 1000 > 0) {
                    teleopWarning.setVisibility(View.VISIBLE);
                    timerID.setTextColor(context.getResources().getColor(R.color.banana));
                    timerID.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.timer_yellow, 0, 0, 0);

                    vibrator.vibrate(500);

                    ObjectAnimator topEdgeLighter = ObjectAnimator.ofFloat(topEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator bottomEdgeLighter = ObjectAnimator.ofFloat(bottomEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator rightEdgeLighter = ObjectAnimator.ofFloat(rightEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator leftEdgeLighter = ObjectAnimator.ofFloat(leftEdgeBar, View.ALPHA, 0.0f, 1.0f);

                    topEdgeLighter.setDuration(500);
                    bottomEdgeLighter.setDuration(500);
                    leftEdgeLighter.setDuration(500);
                    rightEdgeLighter.setDuration(500);

                    topEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);
                    topEdgeLighter.setRepeatCount(1);
                    bottomEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);
                    bottomEdgeLighter.setRepeatCount(1);
                    leftEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);
                    leftEdgeLighter.setRepeatCount(1);
                    rightEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);
                    rightEdgeLighter.setRepeatCount(1);

                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(topEdgeLighter, bottomEdgeLighter, leftEdgeLighter, rightEdgeLighter);
                    animatorSet.start();
                }
            }

            public void onFinish() {
                if(running) {
                    secondsRemaining.setText("00");
                    topEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_error));
                    bottomEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_error));
                    leftEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_error));
                    rightEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_error));
                    timerID.setTextColor(context.getResources().getColor(R.color.border_warning));
                    timerID.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.timer_red, 0, 0, 0);
                    teleopWarning.setTextColor(getResources().getColor(R.color.white));
                    teleopWarning.setBackground(getResources().getDrawable(R.drawable.teleop_error));
                    teleopWarning.setText(getResources().getString(R.string.TeleopError));

                    ObjectAnimator topEdgeLighter = ObjectAnimator.ofFloat(topEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator bottomEdgeLighter = ObjectAnimator.ofFloat(bottomEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator rightEdgeLighter = ObjectAnimator.ofFloat(rightEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator leftEdgeLighter = ObjectAnimator.ofFloat(leftEdgeBar, View.ALPHA, 0.0f, 1.0f);

                    int currentButtonColor = GenUtils.getAColor(context, R.color.melon);
                    if(!nextButton.isEnabled())
                        currentButtonColor = GenUtils.getAColor(context, R.color.night);

                    ValueAnimator teleopButtonAnim = ValueAnimator.ofArgb(currentButtonColor, GenUtils.getAColor(context, R.color.fire));
                    teleopButtonAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            nextButton.setBackgroundColor((Integer)animation.getAnimatedValue());
                        }
                    });

                    int currentArrowColor = GenUtils.getAColor(context, R.color.ice);
                    if(!nextButton.isEnabled())
                        currentArrowColor = GenUtils.getAColor(context, R.color.ocean);

                    ValueAnimator teleopArrowAnim = ValueAnimator.ofArgb(currentArrowColor, GenUtils.getAColor(context, R.color.ice));
                    teleopArrowAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            nextButton.getCompoundDrawablesRelative()[2].setColorFilter((Integer)animation.getAnimatedValue(), PorterDuff.Mode.SRC_IN);
                        }
                    });

                    teleopArrowAnim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            nextButton.getCompoundDrawablesRelative()[2].clearColorFilter();
                            nextButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.right,0);
                        }
                    });

                    ValueAnimator teleopTextAnim = ValueAnimator.ofArgb(nextButton.getCurrentTextColor(), GenUtils.getAColor(context, R.color.ice));
                    teleopTextAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            nextButton.setTextColor((Integer)animation.getAnimatedValue());
                        }
                    });

                    topEdgeLighter.setDuration(500);
                    bottomEdgeLighter.setDuration(500);
                    leftEdgeLighter.setDuration(500);
                    rightEdgeLighter.setDuration(500);
                    teleopButtonAnim.setDuration(500);
                    teleopTextAnim.setDuration(500);
                    teleopArrowAnim.setDuration(500);

                    AnimatorSet animatorSet1 = new AnimatorSet();
                    animatorSet1.playTogether(topEdgeLighter, bottomEdgeLighter, leftEdgeLighter, rightEdgeLighter, teleopButtonAnim, teleopTextAnim, teleopArrowAnim);

                    teleopButtonAnimation = ValueAnimator.ofArgb(GenUtils.getAColor(context, R.color.fire), GenUtils.getAColor(context, R.color.ocean));

                    teleopButtonAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            nextButton.setBackgroundColor((Integer)animation.getAnimatedValue());
                        }
                    });

                    teleopButtonAnimation.setDuration(500);
                    teleopButtonAnimation.setRepeatMode(ValueAnimator.REVERSE);
                    teleopButtonAnimation.setRepeatCount(ValueAnimator.INFINITE);

                    animatorSet = new AnimatorSet();
                    animatorSet.playSequentially(animatorSet1, teleopButtonAnimation);
                    animatorSet.start();
                }
            }
        };

        if(firstTime) {
            firstTime = false;
            timer.start();
        }
        else {
            topEdgeBar.setAlpha(1);
            bottomEdgeBar.setAlpha(1);
            rightEdgeBar.setAlpha(1);
            leftEdgeBar.setAlpha(1);
        }

        // Set listeners for buttons and fill the hashmap with data
        leaveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autonHashMap.put("Leave", isChecked ? "Y" : "N");
                updateXMLObjects();
            }
        });

        fellOverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setupHashMap.put("FellOver", isChecked ? "Y" : "N");
                updateXMLObjects();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.tabs.getTabAt(1).select();
            }
        });

        // Setup grid listeners for clicking on zones
        setupRedAllianceGridListeners();
        setupBlueAllianceGridListeners();
    }

    /**
     * Setup click listeners for all red alliance grid zones (6 columns x 5 rows)
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupRedAllianceGridListeners() {
        if (redAllianceGrid == null) return;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 6; col++) {
                String zoneId = "red_" + row + "_" + col;
                ImageButton button = findGridButton(redAllianceGrid, zoneId);
                if (button != null) {
                    button.setOnClickListener(v -> onGridZoneClicked(zoneId, "field"));
                }
            }
        }
    }

    /**
     * Setup click listeners for all blue alliance grid zones (6 columns x 5 rows)
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupBlueAllianceGridListeners() {
        if (blueAllianceGrid == null) return;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 6; col++) {
                String zoneId = "blue_" + row + "_" + col;
                ImageButton button = findGridButton(blueAllianceGrid, zoneId);
                if (button != null) {
                    button.setOnClickListener(v -> onGridZoneClicked(zoneId, "field"));
                }
            }
        }
    }

    /**
     * Find an ImageButton in the grid by its tag
     * Tags should be in format "red_row_col" or "blue_row_col" (e.g., "red_0_0", "blue_2_3")
     */
    private ImageButton findGridButton(GridLayout grid, String zoneId) {
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (child instanceof ImageButton) {
                ImageButton button = (ImageButton) child;
                // //FIX: Match button tags to zone IDs - convert tag to String for comparison
                Object tag = button.getTag();
                if (tag != null && zoneId.equals(tag.toString())) {
                    return button;
                }
            }
        }
        return null;
    }

    /**
     * Handle grid zone click - show field popup
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onGridZoneClicked(String zoneId, String popupType) {
        currentSelectedZone = zoneId;
        if ("field".equals(popupType)) {
            showFieldPopup(zoneId);
        }
    }

    /**
     * Show the field popup for a specific grid zone
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showFieldPopup(String zoneId) {
        popupDialog = new Dialog(context);
        popupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popupDialog.setContentView(R.layout.popup_auton_field_screen);

        // Get UI elements from popup
        RadioGroup collectingToggle = popupDialog.findViewById(R.id.CollectingCounterToggle);
        RadioGroup ferryingToggle = popupDialog.findViewById(R.id.FerryingCounterToggle);
        RadioGroup startLevelToggle = popupDialog.findViewById(R.id.StartLevelToggle);
        RadioGroup stopLevelToggle = popupDialog.findViewById(R.id.StopLevelToggle);
        RadioGroup missedToggle = popupDialog.findViewById(R.id.MissedCounterToggle);
        Switch robotFellOverSwitch = popupDialog.findViewById(R.id.NoShowSwitch);

        Button saveButton = popupDialog.findViewById(R.id.SaveButton);
        Button cancelButton = popupDialog.findViewById(R.id.CancelButton);

        // Load existing data for this zone
        loadFieldPopupData(zoneId, collectingToggle, ferryingToggle, startLevelToggle, stopLevelToggle, missedToggle, robotFellOverSwitch);

        // Add listeners to all radio groups to update button states when selections change
        RadioGroup.OnCheckedChangeListener stateUpdateListener = (group, checkedId) ->
                updatePopupButtonStates(collectingToggle, ferryingToggle, startLevelToggle, stopLevelToggle, missedToggle);

        startLevelToggle.setOnCheckedChangeListener(stateUpdateListener);
        stopLevelToggle.setOnCheckedChangeListener(stateUpdateListener);
        missedToggle.setOnCheckedChangeListener(stateUpdateListener);

        // Update button states on popup open
        updatePopupButtonStates(collectingToggle, ferryingToggle, startLevelToggle, stopLevelToggle, missedToggle);

        // Save button listener
        saveButton.setOnClickListener(v -> {
            saveFieldPopupData(zoneId, collectingToggle, ferryingToggle, startLevelToggle, stopLevelToggle, missedToggle, robotFellOverSwitch);
            popupDialog.dismiss();
            updateGridVisuals();
        });

        // Cancel button listener
        cancelButton.setOnClickListener(v -> popupDialog.dismiss());

        popupDialog.show();
    }

    /**
     * Load field popup data from HashMap for the given zone
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadFieldPopupData(String zoneId, RadioGroup collectingToggle, RadioGroup ferryingToggle,
                                    RadioGroup startLevelToggle, RadioGroup stopLevelToggle,
                                    RadioGroup missedToggle, Switch robotFellOverSwitch) {
        String collectingValue = autonHashMap.getOrDefault(zoneId + "_Collecting", "000");
        String ferryingValue = autonHashMap.getOrDefault(zoneId + "_Ferrying", "000");
        String startLevelValue = autonHashMap.getOrDefault(zoneId + "_StartLevel", "Empty");
        String stopLevelValue = autonHashMap.getOrDefault(zoneId + "_StopLevel", "Empty");
        String missedValue = autonHashMap.getOrDefault(zoneId + "_Missed", "000");
        String robotFellValue = autonHashMap.getOrDefault(zoneId + "_RobotFellOver", "N");

        // Set radio button selections based on values
        setCounterValue(collectingToggle, collectingValue);
        setCounterValue(ferryingToggle, ferryingValue);
        setLevelValue(startLevelToggle, startLevelValue);
        setLevelValue(stopLevelToggle, stopLevelValue);
        setCounterValue(missedToggle, missedValue);
        robotFellOverSwitch.setChecked("Y".equals(robotFellValue));
    }

    /**
     * Save field popup data to HashMap for the given zone
     */
    private void saveFieldPopupData(String zoneId, RadioGroup collectingToggle, RadioGroup ferryingToggle,
                                    RadioGroup startLevelToggle, RadioGroup stopLevelToggle,
                                    RadioGroup missedToggle, Switch robotFellOverSwitch) {
        // Get selected values from radio groups
        String collectingValue = getCounterValue(collectingToggle);
        String ferryingValue = getCounterValue(ferryingToggle);
        String startLevelValue = getLevelValue(startLevelToggle);
        String stopLevelValue = getLevelValue(stopLevelToggle);
        String missedValue = getCounterValue(missedToggle);
        String robotFellValue = robotFellOverSwitch.isChecked() ? "Y" : "N";

        // Store in HashMap
        autonHashMap.put(zoneId + "_Collecting", collectingValue);
        autonHashMap.put(zoneId + "_Ferrying", ferryingValue);
        autonHashMap.put(zoneId + "_StartLevel", startLevelValue);
        autonHashMap.put(zoneId + "_StopLevel", stopLevelValue);
        autonHashMap.put(zoneId + "_Missed", missedValue);
        autonHashMap.put(zoneId + "_RobotFellOver", robotFellValue);

        Toast.makeText(context, "Data saved for " + zoneId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Set counter value for Collecting, Ferrying, or Missed radio groups
     * Stored values match button text: "-10", "-5", "-", "000", "+", "+5", "+10"
     */
    private void setCounterValue(RadioGroup group, String value) {
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton button = (RadioButton) group.getChildAt(i);
            String buttonText = button.getText().toString().trim();
            // Match exact button text with the stored value
            if (buttonText.equals(value)) {
                group.check(button.getId());
                return;
            }
        }
        // Default to "000" (middle button at index 3) if no match found
        if (group.getChildCount() > 3) {
            group.check(((RadioButton) group.getChildAt(3)).getId());
        }
    }

    /**
     * Get counter value from Collecting, Ferrying, or Missed radio groups
     * Returns the text of the selected radio button (e.g., "-10", "-", "000", "+5", "+10")
     */
    private String getCounterValue(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return "000"; // Default if none selected
        }
        RadioButton selectedButton = group.findViewById(selectedId);
        if (selectedButton != null) {
            return selectedButton.getText().toString().trim();
        }
        return "000";
    }

    /**
     * Set level value for Start Level or Stop Level radio groups
     * Expected values: "Empty", "25", "50", "75", "Full" or similar button text
     */
    private void setLevelValue(RadioGroup group, String value) {
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton button = (RadioButton) group.getChildAt(i);
            String buttonText = button.getText().toString().trim();

            // Match button text - handle both exact matches and partial matches
            if (buttonText.equalsIgnoreCase(value)) {
                group.check(button.getId());
                return;
            }

            // Handle numeric percentage matches (if stored as "25" but button shows "25%")
            if (value.equals("25") && buttonText.contains("25")) {
                group.check(button.getId());
                return;
            }
            if (value.equals("50") && buttonText.contains("50")) {
                group.check(button.getId());
                return;
            }
            if (value.equals("75") && buttonText.contains("75")) {
                group.check(button.getId());
                return;
            }
        }
        // Default to first button (Empty) if no match found
        if (group.getChildCount() > 0) {
            group.check(((RadioButton) group.getChildAt(0)).getId());
        }
    }

    /**
     * Get level value from Start Level or Stop Level radio groups
     * Returns the button text or a normalized value: "Empty", "25", "50", "75", "Full"
     */
    private String getLevelValue(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return "Empty";
        }
        RadioButton selectedButton = group.findViewById(selectedId);
        if (selectedButton != null) {
            String text = selectedButton.getText().toString().trim();
            // Extract numeric values or return text as-is
            if (text.contains("25")) return "25";
            if (text.contains("50")) return "50";
            if (text.contains("75")) return "75";
            if (text.toLowerCase().contains("full")) return "Full";
            if (text.toLowerCase().contains("empty")) return "Empty";
            return text; // Return button text if no pattern match
        }
        return "Empty";
    }

    /**
     * Update grid visuals to show which zones have been filled
     * TODO: Enhance this to change button colors/styling based on whether data exists
     */
    private void updateGridVisuals() {
        // Placeholder for visual feedback
        // Could change button colors based on whether zones have data
    }

    /**
     * Update all button states in popup based on cascading sequential logic:
     * - Level 1: Collecting & Ferrying always enabled
     * - Level 2: START & STOP always enabled
     * - Level 3: MISSED enabled only if both START and STOP are selected
     */
    private void updatePopupButtonStates(RadioGroup collectingToggle, RadioGroup ferryingToggle,
                                         RadioGroup startLevelToggle, RadioGroup stopLevelToggle,
                                         RadioGroup missedToggle) {
        // Get current selections
        String startLevel = getLevelValue(startLevelToggle);
        String stopLevel = getLevelValue(stopLevelToggle);

        // Level 1: Collecting & Ferrying always enabled
        setRadioGroupEnabled(collectingToggle, true);
        setRadioGroupEnabled(ferryingToggle, true);

        // Level 2: START & STOP always enabled (can select anytime)
        setRadioGroupEnabled(startLevelToggle, true);
        setRadioGroupEnabled(stopLevelToggle, true);

        // Level 3: MISSED - enable only if BOTH START and STOP are selected
        boolean bothScoringLevelsSelected = !startLevel.equals("Empty") && !stopLevel.equals("Empty");
        setRadioGroupEnabled(missedToggle, bothScoringLevelsSelected);
    }

    /**
     * Helper method - Enable or disable all buttons in a RadioGroup
     */
    private void setRadioGroupEnabled(RadioGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(enabled);
        }
    }

    /**
     * Update XML objects with data from HashMaps
     */
    public void updateXMLObjects(){
        setupHashMap = HashMapManager.getSetupHashMap();
        autonHashMap = HashMapManager.getAutonHashMap();

        if(setupHashMap == null || autonHashMap == null) {
            return;
        }

        // Update Leave switch based on saved data
        if(autonHashMap.containsKey("Leave")) {
            leaveSwitch.setChecked(autonHashMap.get("Leave").equals("Y"));
        }

        // Update FellOver switch based on saved data and update button state
        if(setupHashMap.containsKey("FellOver")) {
            if(setupHashMap.get("FellOver").equals("Y")) {
                fellOverSwitch.setChecked(true);
                nextButton.setPadding(150, 0, 150, 0);
                nextButton.setText(R.string.GenerateQRCode);
                miscButtonsEnabledState(false);
            } else {
                fellOverSwitch.setChecked(false);
                nextButton.setPadding(150, 0, 185, 0);
                nextButton.setText(R.string.TeleopNext);
                miscButtonsEnabledState(true);
            }
        }

        // Call grid visual update to show saved data
        updateGridVisuals();
    }

    /**
     * Enable or disable misc buttons based on robot state
     */
    private void miscButtonsEnabledState(boolean enable){
        miscInstructionsID.setEnabled(enable);
        leaveSwitch.setEnabled(enable);
        leaveID.setEnabled(enable);
        nextButton.setEnabled(enable);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            // If we are becoming visible, then...
            if (isVisibleToUser) {
                setupHashMap = HashMapManager.getSetupHashMap();
                autonHashMap = HashMapManager.getAutonHashMap();
                updateXMLObjects();
                // Set all objects in the fragment to their values from the HashMaps
            } else {
                if(teleopButtonAnimation != null) {
                    teleopButtonAnimation.cancel();
                    nextButton.setBackground(getResources().getDrawable(R.drawable.button_next_states));
                    nextButton.setTextColor(new ColorStateList(
                            new int [] [] {
                                    new int [] {android.R.attr.state_enabled},
                                    new int [] {}
                            },
                            new int [] {
                                    GenUtils.getAColor(context, R.color.ice),
                                    GenUtils.getAColor(context, R.color.ocean)
                            }
                    ));
                    nextButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.right_states,0);
                    nextButton.setSelected(true);
                }
                HashMapManager.putSetupHashMap(setupHashMap);
                HashMapManager.putAutonHashMap(autonHashMap);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        running = false;
        timer.cancel();
    }

    @Override
    public void onUpdate() {
        updateXMLObjects();
    }
}