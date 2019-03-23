package me.vzhilin.charts.graphics.typewriter;

import android.graphics.Paint;
import android.text.TextPaint;

import java.util.HashMap;
import java.util.Map;

public final class FontContext {
    public final TextPaint textPaint;
    public final Paint.FontMetrics fontMetrics1;
    public final float fontHeight;
    public final float fontWidth;

    private Map<Character, Typewriter.TextureCharacter> characters = new HashMap<>();

    public FontContext(TextPaint textPaint, String alfabet) {
        this.textPaint = textPaint;
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
        if (!characters.containsKey(ch)) {
            throw new RuntimeException("unknown character: " + ch);
        }
        return characters.get(ch);
    }

    public double stringWidth(String label) {
        return textPaint.measureText(label);
    }
}
