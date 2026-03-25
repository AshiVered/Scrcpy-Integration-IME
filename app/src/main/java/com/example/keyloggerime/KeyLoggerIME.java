package com.example.keyloggerime;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class KeyLoggerIME extends InputMethodService {

    private TextView logView;
    private boolean hebrewMode = true;
    private boolean shiftPressed = false;
    private TextView langIndicator;

    private void log(String msg) {
        if (logView != null) {
            logView.append(msg + "\n");

            // הגבלת גודל לוג
            if (logView.getLineCount() > 5) {
                logView.setText("");
            }
        }
    }

    @Override
    public View onCreateInputView() {

        ScrollView scroll = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView langIndicator = new TextView(this);
        langIndicator.setTextSize(18);
        langIndicator.setPadding(16,16,16,16);
        langIndicator.setText("HE");

        layout.addView(langIndicator, 0); // עכשיו layout קיים

        this.langIndicator = langIndicator; // שמור רפרנס

        logView = new TextView(this);
        logView.setTextSize(16);
        logView.setPadding(16, 16, 16, 16);

        layout.addView(logView);
        scroll.addView(layout);

        log("IME started");

        return scroll;
    }
    private void updateLangUI() {
        if (langIndicator != null) {
            langIndicator.setText(hebrewMode ? "HE" : "EN");
        }
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        log("onStartInput: " + attribute.packageName);
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        log("onFinishInput");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // ALT מחליף שפה
        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            hebrewMode = !hebrewMode;
            updateLangUI();
            log("LANG SWITCH: " + (hebrewMode ? "HE" : "EN"));
            return true;
        }

        // SHIFT
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            shiftPressed = true;
            return false;
        }

        if (!hebrewMode) {
            return false; // אנגלית רגילה
        }

        String mapped = mapKey(keyCode, shiftPressed);

        if (mapped != null) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.commitText(mapped, 1);
                log("MAPPED: " + mapped);
                return true;
            }
        }

        return false;
    }

    private String mapKey(int keyCode, boolean shift) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_Q: return "/";
            case KeyEvent.KEYCODE_W: return "'";
            case KeyEvent.KEYCODE_E: return "ק";
            case KeyEvent.KEYCODE_R: return "ר";
            case KeyEvent.KEYCODE_T: return "א";
            case KeyEvent.KEYCODE_Y: return "ט";
            case KeyEvent.KEYCODE_U: return "ו";
            case KeyEvent.KEYCODE_I: return "ן";
            case KeyEvent.KEYCODE_O: return "ם";
            case KeyEvent.KEYCODE_P: return shift ? "ף" : "פ";

            case KeyEvent.KEYCODE_A: return "ש";
            case KeyEvent.KEYCODE_S: return "ד";
            case KeyEvent.KEYCODE_D: return "ג";
            case KeyEvent.KEYCODE_F: return "כ";
            case KeyEvent.KEYCODE_G: return "ע";
            case KeyEvent.KEYCODE_H: return "י";
            case KeyEvent.KEYCODE_J: return "ח";
            case KeyEvent.KEYCODE_K: return "ל";
            case KeyEvent.KEYCODE_L: return shift ? "ך" : "ך";

            case KeyEvent.KEYCODE_Z: return "ז";
            case KeyEvent.KEYCODE_X: return "ס";
            case KeyEvent.KEYCODE_C: return "ב";
            case KeyEvent.KEYCODE_V: return "ה";
            case KeyEvent.KEYCODE_B: return "נ";
            case KeyEvent.KEYCODE_N: return "מ";
            case KeyEvent.KEYCODE_M: return "צ";

            case KeyEvent.KEYCODE_SPACE: return " ";

            case KeyEvent.KEYCODE_DEL:
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    ic.deleteSurroundingText(1, 0);
                }
                return "";

        }

        return null;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            shiftPressed = false;
        }

        return false;
    }
}
