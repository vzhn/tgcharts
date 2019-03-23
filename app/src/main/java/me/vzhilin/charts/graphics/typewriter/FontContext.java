package me.vzhilin.charts.graphics.typewriter;

import android.graphics.Paint;
import android.text.TextPaint;
import me.vzhilin.charts.ViewConstants;

import java.util.HashMap;
import java.util.Map;

public final class FontContext {
    public final TextPaint textPaint;
    public final Paint.FontMetrics fontMetrics1;
    public final float fontHeight;
    public final float fontWidth;
    public float fontSize;

    private Map<Character, Typewriter.TextureCharacter> characters = new HashMap<>();

    public FontContext(float fontSize, String alfabet) {
        this.fontSize = fontSize;
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setARGB(0xff, 0, 0, 0);

        textPaint.setTextSize(ViewConstants.FONT_SIZE_1);
        fontMetrics1 = textPaint.getFontMetrics();
        fontHeight = fontMetrics1.descent - fontMetrics1.top;
        fontWidth = textPaint.measureText(alfabet);
    }

    public float measureText(String ch) {
        return textPaint.measureText(ch);
    }

    public void put(char ch, Typewriter.TextureCharacter tc) {
        characters.put(ch, tc);
    }

    public Typewriter.TextureCharacter get(char ch) {
        return characters.get(ch);
    }
}
