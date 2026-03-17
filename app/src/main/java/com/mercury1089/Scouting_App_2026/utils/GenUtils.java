package com.mercury1089.Scouting_App_2026.utils;

import android.content.Context;
import android.widget.Button;
import com.mercury1089.Scouting_App_2026.R;
import androidx.core.content.ContextCompat;

public class GenUtils {

    private final static int QRCodeSize = 500;

    public static int getAColor(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }

    public static void defaultButtonState (Context context, Button button) {
        button.setBackgroundColor(GenUtils.getAColor(context, R.color.light));
        button.setTextColor(GenUtils.getAColor(context, R.color.grey));
    }
    public static void selectedButtonState(Context context, Button button) {
        button.setBackgroundColor(GenUtils.getAColor(context, R.color.orange));
        button.setTextColor(GenUtils.getAColor(context, R.color.light));
    }
    public static void disabledButtonState(Context context, Button button) {
        button.setBackgroundColor(GenUtils.getAColor(context, R.color.grey));
        button.setTextColor(GenUtils.getAColor(context, R.color.light));
        button.setEnabled(false);
    }

    public static String padLeftZeros(String input, int length){
        if (input == null) return new String(new char[length]).replace("\0", "0");
        if(input.length() < length){
            String result = "";
            for(int i = 0; i < length - input.length(); i++){
                result += "0";
            }
            return result + input;
        }
        return input;
    }
}
