package com.example.keyloggerime;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KeyLoggerIME extends InputMethodService {

    private boolean hebrewMode = true;
    private boolean shiftPressed = false;
    private boolean touchMode = true;
    private boolean symbolsMode = false;

    private TextView langIndicator;

    // ================= UI =================

    @Override
    public View onCreateInputView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // ==== TOP BAR ====
        LinearLayout top = new LinearLayout(this);

        langIndicator = new TextView(this);
        langIndicator.setText(hebrewMode ? "HE" : "EN");
        langIndicator.setPadding(20,20,20,20);

        TextView kbBtn = createKey("⌨", 1f);
        kbBtn.setOnClickListener(v -> {
            touchMode = true;
            setInputView(onCreateInputView());
        });

        top.addView(langIndicator);
        top.addView(kbBtn);
        root.addView(top);

        if (!touchMode) return root;

        // ==== KEYBOARD ====
        LinearLayout kb = new LinearLayout(this);
        kb.setOrientation(LinearLayout.VERTICAL);

        if (symbolsMode) {
            kb.addView(createRow("1234567890"));
            kb.addView(createRow("!@#$%^&*()"));
            kb.addView(createRow(".,?/:;\"'"));
        } else if (hebrewMode) {
            kb.addView(createRow("קראטוןםפ"));
            kb.addView(createRow("שדגכעיחלך"));
            kb.addView(createRow(" זסבהנמצת "));
        } else {
            kb.addView(createRow("qwertyuiop"));
            kb.addView(createRow("asdfghjkl"));
            kb.addView(createRow(" zxcvbnm ")); // רווחים = padding ויזואלי
        }

        // ==== BOTTOM ROW ====
        LinearLayout bottom = new LinearLayout(this);

// SHIFT
        TextView shift = createKey("⇧", 1.2f);
        shift.setTextSize(20);
        shift.setBackgroundColor(0xFFDDDDDD); // אפור בולט
        shift.setOnClickListener(v -> {
            shiftPressed = !shiftPressed;
            setInputView(onCreateInputView()); // רענון UI
        });

// שפה
        TextView lang = createKey("🌐", 1f);
        lang.setOnClickListener(v -> {
            hebrewMode = !hebrewMode;
            updateLang();
            setInputView(onCreateInputView());
        });

// סימנים
        TextView sym = createKey("❶", 1f);
        sym.setOnClickListener(v -> {
            symbolsMode = !symbolsMode;
            setInputView(onCreateInputView());
        });

// רווח (גדול)
        TextView space = createKey("SPACE", 3f);
        space.setOnClickListener(v -> commit(" "));

// מחיקה
        TextView del = createKey("⌫", 1f);
        del.setOnClickListener(v -> delete());

// Enter
        TextView enter = createKey("⏎", 1f);
        enter.setOnClickListener(v -> commit("\n"));

// ⬇ בולט במיוחד
        TextView hide = createKey("⬇", 1.5f);
        hide.setTextSize(20);
        hide.setBackgroundColor(0xFFDDDDDD); // אפור בולט
        hide.setOnClickListener(v -> {
            touchMode = false;
            setInputView(onCreateInputView());
        });

        bottom.addView(shift);
        bottom.addView(lang);
        bottom.addView(sym);
        bottom.addView(space);
        bottom.addView(del);
        bottom.addView(enter);
        bottom.addView(hide);

        kb.addView(bottom);
        root.addView(kb);

        return root;
    }

    private LinearLayout createRow(String keys) {
        LinearLayout row = new LinearLayout(this);

        float weight = 1f;

        for (char c : keys.toCharArray()) {

            char displayChar = c;

            // Shift באנגלית → אותיות גדולות
            if (!hebrewMode && shiftPressed) {
                displayChar = Character.toUpperCase(c);
            }

            TextView key = createKey(String.valueOf(displayChar), weight);

            char finalChar = c;
            key.setOnClickListener(v -> handleTouch(finalChar));

            row.addView(key);
        }

        return row;
    }

    private TextView createKey(String text, float weight) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(16);
        t.setPadding(10, 20, 10, 20);
        t.setGravity(android.view.Gravity.CENTER);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        params.setMargins(4,4,4,4);
        t.setLayoutParams(params);

        return t;
    }

    // ================= TOUCH =================

    private void handleTouch(char c) {

        if (symbolsMode) {
            commit(String.valueOf(c));
            return;
        }

        if (hebrewMode) {
            commit(String.valueOf(c));
            return;
        }

        if (shiftPressed) {
            commit(String.valueOf(Character.toUpperCase(c)));
            shiftPressed = false;
            setInputView(onCreateInputView()); // עדכון חזרה לקטנות
        } else {
            commit(String.valueOf(c));
        }
    }

    private void commit(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(text, 1);
    }

    private void delete() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.deleteSurroundingText(1, 0);
    }

    private void updateLang() {
        if (langIndicator != null) {
            langIndicator.setText(hebrewMode ? "HE" : "EN");
        }
    }

    // ================= PHYSICAL KEYBOARD =================

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // ALT = החלפת שפה
        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            hebrewMode = !hebrewMode;
            updateLang();
            setInputView(onCreateInputView());
            return true;
        }

        // SHIFT
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            shiftPressed = true;
            return false;
        }

        if (!hebrewMode) return false;

        String mapped = mapKey(keyCode, shiftPressed);

        if (mapped != null) {
            commit(mapped);
            return true;
        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            shiftPressed = false;
        }
        return false;
    }

    private String mapKey(int keyCode, boolean shift) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_Q: return "ק";
            case KeyEvent.KEYCODE_W: return "ש";
            case KeyEvent.KEYCODE_E: return "א";
            case KeyEvent.KEYCODE_R: return "ר";
            case KeyEvent.KEYCODE_T: return "ט";
            case KeyEvent.KEYCODE_Y: return "ו";
            case KeyEvent.KEYCODE_U: return "ן";
            case KeyEvent.KEYCODE_I: return "ם";
            case KeyEvent.KEYCODE_O: return "פ";
            case KeyEvent.KEYCODE_P: return shift ? "ף" : "פ";

            case KeyEvent.KEYCODE_A: return "ש";
            case KeyEvent.KEYCODE_S: return "ד";
            case KeyEvent.KEYCODE_D: return "ג";
            case KeyEvent.KEYCODE_F: return "כ";
            case KeyEvent.KEYCODE_G: return "ע";
            case KeyEvent.KEYCODE_H: return "י";
            case KeyEvent.KEYCODE_J: return "ח";
            case KeyEvent.KEYCODE_K: return "ל";
            case KeyEvent.KEYCODE_L: return "ך";

            case KeyEvent.KEYCODE_Z: return "ז";
            case KeyEvent.KEYCODE_X: return "ס";
            case KeyEvent.KEYCODE_C: return "ב";
            case KeyEvent.KEYCODE_V: return "ה";
            case KeyEvent.KEYCODE_B: return "נ";
            case KeyEvent.KEYCODE_N: return "מ";
            case KeyEvent.KEYCODE_M: return "צ";

            case KeyEvent.KEYCODE_SPACE: return " ";

            case KeyEvent.KEYCODE_DEL:
                delete();
                return "";
        }
        return null;
    }
}