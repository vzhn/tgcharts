package me.vzhilin.charts.graphics.typewriter;

import android.content.res.Resources;
import android.graphics.*;
import android.opengl.GLES31;
import android.opengl.GLUtils;
import android.text.TextPaint;
import me.vzhilin.charts.R;
import me.vzhilin.charts.ViewConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Typewriter {
    private final Map<FontType, FontContext> fontContexts;
    private final List<BitmapSprite> bitmapSprites = new ArrayList<>();
    private final List<TextureCharacter> textures = new ArrayList<>();

    String alfabet =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
        "abcdefghijklmnopqrstuvwxyz" +
        "01234567890.,+-= ";

    private float textureHeight;
    private final Resources resources;

    private int textureId;
    private float textureWidth;

    private TextureCharacter circleTexture;
    private TextureCharacter cornersTexture;
    private int markerFillerId;

    public Typewriter(Resources resources) {
        this.resources = resources;

        fontContexts = new HashMap<>();

    }

    public void init() {
        addNormalFont();
        addBigFont();
        addBoldFont();

        addSprites();

        float maxWidth = 0;
        for (FontContext ctx: fontContexts.values()) {
            textureHeight += ctx.fontHeight;
            maxWidth = Math.max(maxWidth, ctx.fontWidth);
        }

        float bmHeight = 0;
        for (BitmapSprite s: bitmapSprites) {
            bmHeight = Math.max(bmHeight, s.w);
        }
        textureHeight += bmHeight;

        textureWidth = maxWidth;
        textureId = initTextures();
    }

    private void addSprites() {
        int d = (int) ViewConstants.MARKER_INNER_RADIUS * 2;
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);

        Canvas cv = new Canvas(bm);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        cv.drawCircle(d / 2, d / 2, ViewConstants.MARKER_INNER_RADIUS, paint);
        bitmapSprites.add(new BitmapSprite(bm));

        this.markerFillerId = bitmapSprites.size() - 1;
    }

    private void addNormalFont() {
        TextPaint normalTextPaint = new TextPaint();
        normalTextPaint.setAntiAlias(true);
        normalTextPaint.setARGB(0xff, 0, 0, 0);
        normalTextPaint.setTextSize(ViewConstants.FONT_SIZE_1);
        fontContexts.put(FontType.NORMAL_FONT, new FontContext(normalTextPaint, alfabet));
    }

    private void addBigFont() {
        TextPaint normalTextPaint = new TextPaint();
        normalTextPaint.setAntiAlias(true);
        normalTextPaint.setARGB(0xff, 0, 0, 0);
        normalTextPaint.setTextSize(ViewConstants.FONT_SIZE_2);
        fontContexts.put(FontType.BIG_FONT, new FontContext(normalTextPaint, alfabet));
    }

    private void addBoldFont() {
        TextPaint normalTextPaint = new TextPaint();
        normalTextPaint.setAntiAlias(true);
        normalTextPaint.setARGB(0xff, 0, 0, 0);
        normalTextPaint.setTextSize(ViewConstants.FONT_SIZE_1);
        normalTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        fontContexts.put(FontType.BOLD_FONT, new FontContext(normalTextPaint, alfabet));
    }

    public int getTextureId() {
        return textureId;
    }

    private int initTextures() {
        Bitmap bitmap = Bitmap.createBitmap((int) textureWidth, (int) textureHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float yOffset = 0;
        for (FontContext ctx: fontContexts.values()) {
            float xOffset = 0;
            canvas.drawText(alfabet, 0, yOffset + ctx.fontHeight - 5, ctx.textPaint);

            for (int i = 0; i < alfabet.length(); i++) {
                char ch = alfabet.charAt(i);
                float charWidth = ctx.measureText(String.valueOf(ch));
                float x1 = xOffset / textureWidth;
                float y1 = yOffset / textureHeight;
                float x2 = (xOffset + charWidth) / textureWidth;
                float y2 = (yOffset + ctx.fontHeight) / textureHeight;
                xOffset += charWidth;

                TextureCharacter tc =  new TextureCharacter(x1, y1, x2, y2, charWidth, ctx.fontHeight);
                ctx.put(ch, tc);
            }

            yOffset += ctx.fontHeight;
        }

        float xOffset = 0;
        for (BitmapSprite bm: bitmapSprites) {
//            canvas.drawBitmap(bm, y);
            canvas.drawBitmap(bm.bm, xOffset, yOffset, null);

            float charWidth = bm.w;
            float x1 = xOffset / textureWidth;
            float y1 = yOffset / textureHeight;
            float x2 = (xOffset + charWidth) / textureWidth;
            float y2 = (yOffset + bm.h) / textureHeight;
            xOffset += charWidth;

            TextureCharacter tc = new TextureCharacter(x1, y1, x2, y2, charWidth, bm.h);
            textures.add(tc);
        }

        return generateTextures(bitmap);
    }

    private int drawCorners(Canvas canvas, float ws, float textureWidth) {
        Bitmap bm = BitmapFactory.decodeResource(resources, R.drawable.corner);
        canvas.drawBitmap(bm, ws, 0f, null);
        int w = bm.getWidth();
        int h = bm.getHeight();
        bm.recycle();

        cornersTexture = new TextureCharacter(ws / textureWidth,0, (ws + w) / textureWidth, h / textureHeight,
                (float) w, (float) h);

        return w;
    }

    public FontContext getContext(FontType size) {
        return fontContexts.get(size);
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

    public TextureCharacter getCornersTexture() {
        return cornersTexture;
    }

    public TextureCharacter getSprite(int id) {
        return textures.get(id);
    }

    public int newMarker(int columnColor) {
        int d = (int) ViewConstants.MARKER_EXTERNAL_RADIUS * 2;
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);

        Canvas cv = new Canvas(bm);
        Paint paint = new Paint();

        paint.setColor(columnColor);
        cv.drawCircle(d / 2, d / 2, ViewConstants.MARKER_EXTERNAL_RADIUS, paint);

        paint.setColor(Color.WHITE);
        cv.drawCircle(d / 2, d / 2, ViewConstants.MARKER_INNER_RADIUS, paint);

//        Bitmap bm = BitmapFactory.decodeResource(resources, R.drawable.corner);
        bitmapSprites.add(new BitmapSprite(bm));
        return bitmapSprites.size() - 1;
    }

    public int getMarkerFillerId() {
        return markerFillerId;
    }

    public enum FontType {
        NORMAL_FONT,
        BOLD_FONT,
        BIG_FONT
    }

    public final static class BitmapSprite {
        public final float w;
        public final float h;
        private final Bitmap bm;

        public BitmapSprite(Bitmap bm) {
            this.w = bm.getWidth();
            this.h = bm.getHeight();
            this.bm = bm;
        }
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
