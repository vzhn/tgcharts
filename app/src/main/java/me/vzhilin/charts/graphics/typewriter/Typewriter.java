package me.vzhilin.charts.graphics.typewriter;

import android.graphics.*;
import android.opengl.GLES31;
import android.opengl.GLUtils;
import android.text.TextPaint;
import me.vzhilin.charts.ViewConstants;

import java.util.HashMap;
import java.util.Map;

public class Typewriter {
    private final int textureId;
    private final Map<Character, TextureCharacter> characters = new HashMap<>();
    private final float textureHeight;
    private final TextPaint textPaint;
    private final Paint.FontMetrics fm;
    private TextureCharacter circleTexture;

    public Typewriter() {
        textPaint = new TextPaint();
        textPaint.setTextSize(ViewConstants.FONT_SIZE);
        textPaint.setAntiAlias(true);
        textPaint.setARGB(0xff, 0, 0, 0);
        fm = textPaint.getFontMetrics();
        textureHeight = fm.descent - fm.top;

        textureId = initTextures();
    }

    public int getTextureId() {
        return textureId;
    }

    private int initTextures() {
        String alfabet =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "01234567890.+-= ";


        float textWidth = textPaint.measureText(alfabet);
        float textureWidth = textWidth + 2 * ViewConstants.LINE_WIDTH;

        float ws = 0;
        for (int i = 0; i < alfabet.length(); i++) {
            char ch = alfabet.charAt(i);
            float charWidth = textPaint.measureText(String.valueOf(ch));
            characters.put(ch, new TextureCharacter(ws / textureWidth, 0, (ws + charWidth) / textureWidth, 1f,
                    charWidth, textureHeight));

            ws += charWidth;
        }

        textPaint.setColor(ViewConstants.VIEW_GRAY);

        Bitmap bitmap = Bitmap.createBitmap((int) textureWidth, (int) textureHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(alfabet, 0, textureHeight - fm.descent, textPaint);

        drawCircle(canvas, textWidth, textureWidth);
        return generateTextures(bitmap);
    }

    private void drawCircle(Canvas canvas, float offset, float textureWidth) {
        float r = ViewConstants.LINE_WIDTH;
        float d = 2 * r;

        canvas.drawCircle(offset + r, r, r, textPaint);
        circleTexture = new TextureCharacter(offset / textureWidth, 0, (offset + d) / textureWidth, d / textureHeight, d, d);
    }

    public TextureCharacter get(char ch) {
        return characters.get(ch);
    }

    private int generateTextures(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES31.glGenTextures(1, textures, 0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, textures[0]);
        GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST);
        GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR);
        GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_REPEAT);
        GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_REPEAT);
        GLUtils.texImage2D(GLES31.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        return textures[0];
    }

    public float getHeight() {
        return textureHeight;
    }

    public TextureCharacter getCircleTexture() {
        return circleTexture;
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
