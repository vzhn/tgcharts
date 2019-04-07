package me.vzhilin.charts.graphics.typewriter;

import android.content.res.Resources;
import android.graphics.*;
import android.text.TextPaint;
import me.vzhilin.charts.R;
import me.vzhilin.charts.ViewConstants;

import java.util.HashMap;
import java.util.Map;

public class Typewriter {
    private final String alfabet =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "01234567890.,+-= ";

    private final Map<FontType, FontInfo> fontContexts;
    private final SpritePack spritePack;
    private final Resources resources;
    private int markerFillerId;
    private int cornerSideId;

    public Typewriter(Resources resources) {
        this.resources = resources;
        this.spritePack = new SpritePack();

        fontContexts = new HashMap<>();
    }

    public void init() {
        addNormalFont();
        addBigFont();
        addBoldFont();
        addSprites();

        initTextures();
    }

    private void addSprites() {
        addMarker();
        addCorner();
    }

    private void addCorner() {
        cornerSideId = spritePack.put(BitmapFactory.decodeResource(resources, R.drawable.corner));
    }

    public int getCornerSideId() {
        return cornerSideId;
    }

    private void addMarker() {
        int d = (int) ViewConstants.MARKER_INNER_RADIUS * 2;
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);

        Canvas cv = new Canvas(bm);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        cv.drawCircle(d / 2, d / 2, ViewConstants.MARKER_INNER_RADIUS, paint);
        this.markerFillerId = spritePack.put(bm);
    }

    private void addNormalFont() {
        TextPaint normalTextPaint = new TextPaint();
        normalTextPaint.setAntiAlias(true);
        normalTextPaint.setARGB(0xff, 0, 0, 0);
        normalTextPaint.setTextSize(ViewConstants.FONT_SIZE_1);
        fontContexts.put(FontType.NORMAL_FONT, new FontInfo(normalTextPaint, alfabet));
    }

    private void addBigFont() {
        TextPaint normalTextPaint = new TextPaint();
        normalTextPaint.setAntiAlias(true);
        normalTextPaint.setARGB(0xff, 0, 0, 0);
        normalTextPaint.setTextSize(ViewConstants.FONT_SIZE_2);
        fontContexts.put(FontType.BIG_FONT, new FontInfo(normalTextPaint, alfabet));
    }

    private void addBoldFont() {
        TextPaint normalTextPaint = new TextPaint();
        normalTextPaint.setAntiAlias(true);
        normalTextPaint.setARGB(0xff, 0, 0, 0);
        normalTextPaint.setTextSize(ViewConstants.FONT_SIZE_1);
        normalTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        fontContexts.put(FontType.BOLD_FONT, new FontInfo(normalTextPaint, alfabet));
    }

    public int getTextureId() {
        return spritePack.getTextureId();
    }

    private void initTextures() {
        for (FontInfo ctx: fontContexts.values()) {
            for (int i = 0; i < alfabet.length(); i++) {
                char ch = alfabet.charAt(i);
                int charWidth = (int) Math.ceil(ctx.measureText(String.valueOf(ch)));
                int charHeight = (int) Math.ceil(ctx.fontHeight);

                Bitmap bitmap = Bitmap.createBitmap(charWidth, charHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawText(String.valueOf(ch), 0, charHeight - ctx.fontMetrics.descent, ctx.textPaint);

                int spriteId = spritePack.put(bitmap);
                ctx.put(ch, spriteId);
            }
        }

        spritePack.build();
    }

    public FontInfo getContext(FontType size) {
        return fontContexts.get(size);
    }

    public Sprite getSprite(int id) {
        return spritePack.get(id);
    }

    public int newMarker(int columnColor) {
        int d = (int) ViewConstants.MARKER_EXTERNAL_RADIUS * 2;
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);

        Canvas cv = new Canvas(bm);
        Paint paint = new Paint();

        paint.setColor(columnColor);
        cv.drawCircle((float) d / 2, (float)d / 2, ViewConstants.MARKER_EXTERNAL_RADIUS, paint);

        paint.setColor(Color.WHITE);
        cv.drawCircle((float) d / 2, (float) d / 2, ViewConstants.MARKER_INNER_RADIUS, paint);

        return spritePack.put(bm);
    }

    public int getMarkerFillerId() {
        return markerFillerId;
    }

    public enum FontType {
        NORMAL_FONT,
        BOLD_FONT,
        BIG_FONT
    }
}
