package me.vzhilin.charts.graphics.typewriter;

import android.graphics.*;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.text.TextPaint;

import java.util.HashMap;
import java.util.Map;

public class Typewriter {
    private final int textureId;
    private final Map<Character, TextureCharacter> characters = new HashMap<>();
    private final float textHeight;
    private final TextPaint textPaint;
    private final Paint.FontMetrics fm;

    public Typewriter() {
        textPaint = new TextPaint();
        textPaint.setTextSize(38);
        textPaint.setAntiAlias(true);
        textPaint.setARGB(0xff, 0, 0, 0);
        fm = textPaint.getFontMetrics();
        textHeight = fm.descent - fm.top;

        textureId = initTextures();
    }

    public int getTextureId() {
        return textureId;
    }

    private int initTextures() {
        String alfabet =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "01234567890";


        float textWidth = textPaint.measureText(alfabet);

        float ws = 0;
        for (int i = 0; i < alfabet.length(); i++) {
            char ch = alfabet.charAt(i);
            float charWidth = textPaint.measureText(String.valueOf(ch));
            float aspect = charWidth / textHeight;
            characters.put(ch, new TextureCharacter(ws / textWidth, 0, (ws + charWidth) / textWidth, 1f,
                    charWidth, textHeight));

            ws += charWidth;
        }

        Bitmap bitmap = Bitmap.createBitmap((int) textWidth, (int) textHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(alfabet, 0,textHeight - fm.descent, textPaint);
        return generateTextures(bitmap);
    }

    public TextureCharacter get(char ch) {
        return characters.get(ch);
    }

    private int generateTextures(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        return textures[0];
    }

    public float getHeight() {
        return textHeight;
    }

    public final static class TextureCharacter {
        public final float x1;
        public final float y1;
        public final float x2;
        public final float y2;

        public final float width;
        public final float height;

        public TextureCharacter(float x1, float y1, float x2, float y2, float width, float height) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;

            this.width = width;
            this.height = height;
        }
    }
}
