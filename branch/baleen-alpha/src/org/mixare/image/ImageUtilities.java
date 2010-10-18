package org.mixare.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;

public class ImageUtilities {
    private static final String LOG_TAG = "ImageUtilities";

    private static final boolean FLAG_DECODE_BITMAP_WITH_SKIA = false;

    private static final float EDGE_START = 0.0f;
    private static final float EDGE_END = 4.0f;
    private static final int EDGE_COLOR_START = 0x7F000000;
    private static final int EDGE_COLOR_END = 0x00000000;
    private static final Paint EDGE_PAINT = new Paint();

    private static final int END_EDGE_COLOR_START = 0x00000000;
    private static final int END_EDGE_COLOR_END = 0x4F000000;
    private static final Paint END_EDGE_PAINT = new Paint();

    private static final float FOLD_START = 5.0f;
    private static final float FOLD_END = 13.0f;
    private static final int FOLD_COLOR_START = 0x00000000;
    private static final int FOLD_COLOR_END = 0x26000000;
    private static final Paint FOLD_PAINT = new Paint();

    private static final float SHADOW_RADIUS = 12.0f;
    private static final int SHADOW_COLOR = 0x99000000;
    private static final Paint SHADOW_PAINT = new Paint();

    private static final Paint SCALE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG |
            Paint.FILTER_BITMAP_FLAG);

    private static final FastBitmapDrawable NULL_DRAWABLE = new FastBitmapDrawable(null);

    // TODO: Use a concurrent HashMap to support multiple threads
    private static final HashMap<String, SoftReference<FastBitmapDrawable>> sArtCache =
            new HashMap<String, SoftReference<FastBitmapDrawable>>();

    private static volatile Matrix sScaleMatrix;
    private static SimpleDateFormat sLastModifiedFormat;

    static {
        Shader shader = new LinearGradient(EDGE_START, 0.0f, EDGE_END, 0.0f, EDGE_COLOR_START,
                EDGE_COLOR_END, Shader.TileMode.CLAMP);
        EDGE_PAINT.setShader(shader);

        shader = new LinearGradient(EDGE_START, 0.0f, EDGE_END, 0.0f, END_EDGE_COLOR_START,
                END_EDGE_COLOR_END, Shader.TileMode.CLAMP);
        END_EDGE_PAINT.setShader(shader);

        shader = new LinearGradient(FOLD_START, 0.0f, FOLD_END, 0.0f, new int[] {
                FOLD_COLOR_START, FOLD_COLOR_END, FOLD_COLOR_START
        }, new float[] { 0.0f, 0.5f, 1.0f }, Shader.TileMode.CLAMP);
        FOLD_PAINT.setShader(shader);

        SHADOW_PAINT.setShadowLayer(SHADOW_RADIUS / 2.0f, 0.0f, 0.0f, SHADOW_COLOR);
        SHADOW_PAINT.setAntiAlias(true);
        SHADOW_PAINT.setFilterBitmap(true);
        SHADOW_PAINT.setColor(0xFF000000);
        SHADOW_PAINT.setStyle(Paint.Style.FILL);
    }

    private ImageUtilities() {
    }

    /**
     * A Bitmap associated with its last modification date. This can be used to check
     * whether the book covers should be downloaded again.
     */
    public static class ExpiringBitmap {
        public Bitmap bitmap;
    }






    /**
     * Loads an image from the specified URL.
     *
     * @param url The URL of the image to load.
     *
     * @return The image at the specified URL or null if an error occured.
     */
    public static ExpiringBitmap load(String url) {
        return load(url, null);
    }

    /**
     * Loads an image from the specified URL with the specified cookie.
     *
     * @param url The URL of the image to load.
     * @param cookie The cookie to use to load the image.
     *
     * @return The image at the specified URL or null if an error occured.
     */
    public static ExpiringBitmap load(String url, String cookie) {
        ExpiringBitmap expiring = new ExpiringBitmap();

        final HttpGet get = new HttpGet(url);
        if (cookie != null) get.setHeader("cookie", cookie);

        HttpEntity entity = null;
        try {
            final HttpResponse response = HttpManager.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                setLastModified(expiring, response);

                entity = response.getEntity();

                InputStream in = null;
                OutputStream out = null;

                try {
                    in = entity.getContent();
                    if (FLAG_DECODE_BITMAP_WITH_SKIA) {
                        expiring.bitmap = BitmapFactory.decodeStream(in);
                    } else {
                        final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                        out = new BufferedOutputStream(dataStream, IOUtilities.IO_BUFFER_SIZE);
                        IOUtilities.copy(in, out);
                        out.flush();

                        final byte[] data = dataStream.toByteArray();
                        expiring.bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                } catch (IOException e) {
                    android.util.Log.e(LOG_TAG, "Could not load image from " + url, e);
                } finally {
                    IOUtilities.closeStream(in);
                    IOUtilities.closeStream(out);
                }
            }
        } catch (IOException e) {
            android.util.Log.e(LOG_TAG, "Could not load image from " + url, e);
        } finally {
            if (entity != null) {
                try {
                    entity.consumeContent();
                } catch (IOException e) {
                    android.util.Log.e(LOG_TAG, "Could not load image from " + url, e);
                }
            }
        }

        return expiring;
    }



    /**
     * Create a profile image with the specified bitmap. This method returns a new decored bitmap.
     *
     * @param bitmap The bitmap to decor 
     * @param width The target width of the decored bitmap
     * @param height The target height of the decored bitmap
     *
     * @return A new Bitmap based on the original bitmap
     */
    public static Bitmap createProfileImage(Bitmap bitmap, int width, int height) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        final float scale = Math.min((float) width / (float) bitmapWidth,
                (float) height / (float) bitmapHeight);

        final int scaledWidth = (int) (bitmapWidth * scale);
        final int scaledHeight = (int) (bitmapHeight * scale);

        final Bitmap decored = createScaledBitmap(bitmap, scaledWidth, scaledHeight,
                SHADOW_RADIUS, true, SHADOW_PAINT);
        final Canvas canvas = new Canvas(decored);

        canvas.translate(SHADOW_RADIUS / 2.0f, SHADOW_RADIUS / 2.0f);
        canvas.drawRect(EDGE_START, 0.0f, EDGE_END, scaledHeight, EDGE_PAINT);
        canvas.drawRect(FOLD_START, 0.0f, FOLD_END, scaledHeight, FOLD_PAINT);
        //noinspection PointlessArithmeticExpression
        canvas.translate(scaledWidth - (EDGE_END - EDGE_START), 0.0f);
        canvas.drawRect(EDGE_START, 0.0f, EDGE_END, scaledHeight, END_EDGE_PAINT);

        return decored;
    }



    private static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight,
            float offset, boolean clipShadow, Paint paint) {
        
        Matrix m;
        synchronized (Bitmap.class) {
            m = sScaleMatrix;
            sScaleMatrix = null;
        }

        if (m == null) {
            m = new Matrix();
        }

        final int width = src.getWidth();
        final int height = src.getHeight();
        final float sx = dstWidth  / (float) width;
        final float sy = dstHeight / (float) height;
        m.setScale(sx, sy);

        Bitmap b = createBitmap(src, 0, 0, width, height, m, offset, clipShadow, paint);

        synchronized (Bitmap.class) {
            sScaleMatrix = m;
        }

        return b;
    }

    private static Bitmap createBitmap(Bitmap source, int x, int y, int width,
            int height, Matrix m, float offset, boolean clipShadow, Paint paint) {

        int scaledWidth = width;
        int scaledHeight = height;

        final Canvas canvas = new Canvas();
        canvas.translate(offset / 2.0f, offset / 2.0f);

        Bitmap bitmap;

        final Rect from = new Rect(x, y, x + width, y + height);
        final RectF to = new RectF(0, 0, width, height);

        if (m == null || m.isIdentity()) {
            bitmap = Bitmap.createBitmap(scaledWidth + (int) offset,
                    scaledHeight + (int) (clipShadow ? (offset / 2.0f) : offset),
                    Bitmap.Config.ARGB_8888);
            paint = null;
        } else {
            RectF mapped = new RectF();
            m.mapRect(mapped, to);

            scaledWidth = Math.round(mapped.width());
            scaledHeight = Math.round(mapped.height());

            bitmap = Bitmap.createBitmap(scaledWidth + (int) offset,
                    scaledHeight + (int) (clipShadow ? (offset / 2.0f) : offset),
                    Bitmap.Config.ARGB_8888);
            canvas.translate(-mapped.left, -mapped.top);
            canvas.concat(m);
        }

        canvas.setBitmap(bitmap);
        canvas.drawRect(0.0f, 0.0f, width, height, paint);
        canvas.drawBitmap(source, from, to, SCALE_PAINT);

        return bitmap;
    }
}
