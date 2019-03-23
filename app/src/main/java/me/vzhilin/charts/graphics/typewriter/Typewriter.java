package me.vzhilin.charts.graphics.typewriter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.opengl.GLES31;
import android.opengl.GLUtils;
import me.vzhilin.charts.R;
import me.vzhilin.charts.ViewConstants;

import java.util.HashMap;
import java.util.Map;

public class Typewriter {
    private final Map<Float, FontContext> fontContexts;

    String alfabet =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
        "abcdefghijklmnopqrstuvwxyz" +
        "01234567890.,+-= ";

    private final int textureId;
    private float textureHeight;
    private final Resources resources;
    private final float textureWidth;


    private TextureCharacter circleTexture;
    private TextureCharacter cornersTexture;

    public Typewriter(Resources resources) {
        this.resources = resources;

        fontContexts = new HashMap<>();
        fontContexts.put(ViewConstants.FONT_SIZE_1, new FontContext(ViewConstants.FONT_SIZE_1, alfabet));
        fontContexts.put(ViewConstants.FONT_SIZE_2, new FontContext(ViewConstants.FONT_SIZE_2, alfabet));

        float maxWidth = 0;
        for (FontContext ctx: fontContexts.values()) {
            textureHeight += ctx.fontHeight;
            maxWidth = Math.max(maxWidth, ctx.fontWidth);
        }

        textureWidth = maxWidth;
        textureId = initTextures();
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
            canvas.drawText(alfabet, 0, yOffset, ctx.textPaint);

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


//        xOffset += drawCircle(canvas, xOffset, textureWidth);
//        xOffset += drawCorners(canvas, xOffset, textureWidth);
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

//    private float drawCircle(Canvas canvas, float offset, float textureWidth) {
//        float r = ViewConstants.LINE_WIDTH;
//        float d = 2 * r;
//
//        canvas.drawCircle(offset + r, r, r, textPaint);
//        circleTexture = new TextureCharacter(offset / textureWidth, 0, (offset + d) / textureWidth, d / textureHeight, d, d);
//        return d;
//    }

//    public TextureCharacter get(char ch) {
//        return characters.get(ch);
//    }

    public FontContext getContext(float size) {
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
