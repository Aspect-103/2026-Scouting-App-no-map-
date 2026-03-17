package com.mercury1089.Scouting_App_2026;

import com.mercury1089.Scouting_App_2026.qr.QRRunnable;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.button.MaterialButton;


import java.util.LinkedHashMap;
import java.util.Objects;

public class PregameActivity extends AppCompatActivity {
    // Strategy was here
    //Set the default password in HashMapManager.setDefaultValues();
    String password;

    //variables that store elements of the screen for the output variables
    //Buttons
    private ImageButton settingsButton;
    private Button blueButton;
    private Button redButton;
    private Button clearButton;
    private MaterialButton startButton;

    //Text Fields
    private EditText scouterNameInput;
    private EditText matchNumberInput;
    private EditText teamNumberInput;
    private EditText firstAlliancePartnerInput;
    private EditText secondAlliancePartnerInput;
    private TextView startDirectionsToast;

    //Switches & Counters
    private MaterialSwitch noShowSwitch;
    private RadioGroup preloadedCargoToggle;
    private RadioButton preloadCargoDisplay;


    //HashMaps
    private LinkedHashMap<String, String> settingsHashMap;
    private LinkedHashMap<String, String> setupHashMap;
    private Dialog loading_alert;
    private ProgressDialog progressDialog;
    boolean isQRButton = false;

    //Max and Min of of Preloading
    private static final int PRELOAD_CARGO_MAX = 8;
    private static final int PRELOAD_CARGO_MIN = 0;
    //others
    private MediaPlayer rooster;

    // Unified listener for the preloaded cargo counter
    private final RadioGroup.OnCheckedChangeListener preLoadListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.PreloadedCargoDisplay) {
                return;
            }
            int currentCargo = getPreloadCargoCount();
            if (checkedId == R.id.PreloadedCargoMinus) {
                if (currentCargo > PRELOAD_CARGO_MIN) {
                    currentCargo--;
                }
            } else if (checkedId == R.id.PreloadedCargoPlus) {
                if (currentCargo < PRELOAD_CARGO_MAX) {
                    currentCargo++;
                }
            }
            updatePreloadCargoCount(currentCargo);
            refreshPreloadDisplay();
            updateXMLObjects(false);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregame);

        // Initialize views here
        scouterNameInput = findViewById(R.id.ScouterNameInput);
        matchNumberInput = findViewById(R.id.MatchNumberInput);
        teamNumberInput = findViewById(R.id.TeamNumberInput);
        firstAlliancePartnerInput = findViewById(R.id.FirstAlliancePartnerInput);
        secondAlliancePartnerInput = findViewById(R.id.SecondAlliancePartnerInput);

        //Alliance Color
        blueButton = findViewById(R.id.BlueButton);
        redButton = findViewById(R.id.RedButton);

        //Buttons
        clearButton = findViewById(R.id.ClearButton);
        startButton = findViewById(R.id.StartButton);
        settingsButton = findViewById(R.id.SettingsButton);

        //Preload Cargo Toggle (RadioGroup)
        preloadedCargoToggle = findViewById(R.id.CollectingCounterToggle);
        preloadCargoDisplay = findViewById(R.id.PreloadedCargoDisplay);

        //misc
        startDirectionsToast = findViewById(R.id.IDStartDirections);
        noShowSwitch = findViewById(R.id.NoShowSwitch);

        rooster = MediaPlayer.create(PregameActivity.this, R.raw.sound);

        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.SETTINGS);
        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.SETUP);
        settingsHashMap = HashMapManager.getSettingsHashMap();
        setupHashMap = HashMapManager.getSetupHashMap();
        password = settingsHashMap.get("DefaultPassword");

        updateXMLObjects(true);

        scouterNameInput.addTextChangedListener(getTextWatcher("ScouterName"));
        matchNumberInput.addTextChangedListener(getTextWatcher("MatchNumber"));
        teamNumberInput.addTextChangedListener(getTextWatcher("TeamNumber"));
        firstAlliancePartnerInput.addTextChangedListener(getTextWatcher("AlliancePartner1"));
        secondAlliancePartnerInput.addTextChangedListener(getTextWatcher("AlliancePartner2"));

        noShowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setupHashMap.put("NoShow", isChecked ? "Y" : "N");
                updateXMLObjects(false);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Settings password check logic...
                HashMapManager.putSetupHashMap(setupHashMap);
                Intent intent = new Intent(PregameActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        blueButton.setOnClickListener(v -> {
            setupHashMap.put("AllianceColor", Objects.equals(setupHashMap.get("AllianceColor"), "Blue") ? "" : "Blue");
            updateXMLObjects(false);
        });

        redButton.setOnClickListener(v -> {
            setupHashMap.put("AllianceColor", Objects.equals(setupHashMap.get("AllianceColor"), "Red") ? "" : "Red");
            updateXMLObjects(false);
        });

        preloadedCargoToggle.setOnCheckedChangeListener(preLoadListener);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isQRButton) {
                    // QR Generation logic...
                } else {
                    HashMapManager.putSetupHashMap(setupHashMap);
                    Intent intent = new Intent(PregameActivity.this, MatchActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        clearButton.setOnClickListener(v -> {
            HashMapManager.setDefaultValues(HashMapManager.HASH.SETUP);
            setupHashMap = HashMapManager.getSetupHashMap();
            updateXMLObjects(true);
        });
    }

    private TextWatcher getTextWatcher(String key) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                setupHashMap.put(key, s.toString());
                updateXMLObjects(false);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private int getPreloadCargoCount() {
        try {
            String cargoStr = setupHashMap.get("PreloadedCargo");
            return Integer.parseInt(cargoStr != null ? cargoStr : "0");
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updatePreloadCargoCount(int count) {
        setupHashMap.put("PreloadedCargo", String.valueOf(count));
        updatePreloadCargoDisplay(count);
    }

    private void updatePreloadCargoDisplay(int count) {
        preloadCargoDisplay.setText(String.format("%03d", count));
    }

    private void refreshPreloadDisplay() {
        preloadedCargoToggle.setOnCheckedChangeListener(null);
        preloadedCargoToggle.check(R.id.PreloadedCargoDisplay);
        preloadedCargoToggle.setOnCheckedChangeListener(preLoadListener);
    }

    private boolean readyToStart() {
        return scouterNameInput.getText().length() > 0 &&
                matchNumberInput.getText().length() > 0 &&
                teamNumberInput.getText().length() > 0 &&
                firstAlliancePartnerInput.getText().length() > 0 &&
                secondAlliancePartnerInput.getText().length() > 0 &&
                setupHashMap.get("AllianceColor") != null && !setupHashMap.get("AllianceColor").isEmpty();
    }

    private boolean canClearInputs() {
        return scouterNameInput.getText().length() > 0 ||
                matchNumberInput.getText().length() > 0 ||
                teamNumberInput.getText().length() > 0 ||
                noShowSwitch.isChecked() ||
                firstAlliancePartnerInput.getText().length() > 0 ||
                secondAlliancePartnerInput.getText().length() > 0 ||
                !Objects.requireNonNull(setupHashMap.get("AllianceColor")).isEmpty() || 
                getPreloadCargoCount() > 0;
    }

    private void updateXMLObjects(boolean updateText) {
        if (updateText) {
            scouterNameInput.setText(setupHashMap.get("ScouterName"));
            matchNumberInput.setText(setupHashMap.get("MatchNumber"));
            teamNumberInput.setText(setupHashMap.get("TeamNumber"));
            firstAlliancePartnerInput.setText(setupHashMap.get("AlliancePartner1"));
            secondAlliancePartnerInput.setText(setupHashMap.get("AlliancePartner2"));
            updatePreloadCargoDisplay(getPreloadCargoCount());
            refreshPreloadDisplay();
        }

        blueButton.setSelected(Objects.equals(setupHashMap.get("AllianceColor"), "Blue"));
        redButton.setSelected(Objects.equals(setupHashMap.get("AllianceColor"), "Red"));

        if (Objects.equals(setupHashMap.get("NoShow"), "Y")) {
            noShowSwitch.setChecked(true);
            startButton.setIconResource(R.drawable.qr_states);
            startButton.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            startButton.setIconSize(20);
            startButton.setText(R.string.GenerateQRCode);
            isQRButton = true;
        } else {
            noShowSwitch.setChecked(false);
            startButton.setIconResource(R.drawable.start_button_symbol_states);
            startButton.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_END);
            startButton.setIconSize(26);
            startButton.setText(R.string.Start);
            isQRButton = false;
        }

        boolean readyToStart = readyToStart();
        startButton.setEnabled(readyToStart);
        startDirectionsToast.setEnabled(readyToStart && !isQRButton);
        clearButton.setEnabled(canClearInputs());
    }

    @Override protected void onResume() { super.onResume(); updateXMLObjects(true); }
    @Override public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
