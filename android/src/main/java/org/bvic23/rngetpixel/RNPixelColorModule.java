package org.bvic23.rngetpixel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.net.Uri;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

class RNPixelColorModule extends ReactContextBaseJavaModule {
    private final Context context;
    private static final double rotation =  PI / 2;

    public RNPixelColorModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "RNPixelColor";
    }

    @ReactMethod
    public void getPixelRGBAofImage(final String imageName, final int x, final int y, final Callback callback) {
        try {
            final Bitmap bitmap = loadImage(imageName);
            final int pixel = bitmap.getPixel(x, y);
            respondWithPixel(callback, pixel);
        } catch (Exception e) {
            callback.invoke(e.getMessage());
        }
    }

    @ReactMethod
    public void getPixelRGBAPolarOfImage(final String imageName, final double angle, final double radius, final Callback callback) {
        try {
            final Bitmap image = loadImage(imageName);
            final double width = image.getWidth();
            final double height = image.getHeight();
            final double rotatedAngle = angle + rotation;

            final double centerX = width * 0.5;
            final double centerY = height * 0.5;

            final int x = (int)(centerX + radius * cos(rotatedAngle));
            final int y = (int)(centerY + radius * sin(rotatedAngle));

            final int pixel = image.getPixel(x, y);
            respondWithPixel(callback, pixel);
        } catch (Exception e) {
            callback.invoke(e.getMessage());
        }
    }

    @ReactMethod
    public void findAngleOfNearestColor(final String imageName, final double minAngle, final double maxAngle, final double radius, final ReadableArray targetColor, final Callback callback) {
        try {
            final Bitmap image = loadImage(imageName);
            final double width = image.getWidth();
            final double height = image.getHeight();

            final double centerX = width * 0.5;
            final double centerY = height * 0.5;

            double angle = minAngle;

            final int targetRed = targetColor.getInt(0);
            final int targetGreen = targetColor.getInt(1);
            final int targetBlue = targetColor.getInt(2);

            double minDistance = Double.MAX_VALUE;
            double resultAngle = Double.MAX_VALUE;

            while (angle <= maxAngle) {
                final double rotatedAngle = angle + rotation;
                final int x = (int)(centerX + radius * cos(rotatedAngle));
                final int y = (int)(centerY + radius * sin(rotatedAngle));

                final int pixel = image.getPixel(x, y);
                final int red = Color.red(pixel);
                final int green = Color.green(pixel);
                final int blue = Color.blue(pixel);

                final double distance = pow(targetRed - red, 2) + pow(targetGreen - green, 2) + pow(targetBlue - blue, 2);

                if (distance < minDistance) {
                    minDistance = distance;
                    resultAngle = angle;
                }

                angle += PI / 180;
            }

            callback.invoke(null, resultAngle);
        } catch (Exception e) {
            callback.invoke(e.getMessage());
        }
    }

    private void respondWithPixel(final Callback callback, final int pixel) {
        final int r = Color.red(pixel);
        final int g = Color.green(pixel);
        final int b = Color.blue(pixel);

        final WritableArray result = new WritableNativeArray();
        result.pushInt(r);
        result.pushInt(g);
        result.pushInt(b);
        callback.invoke(null, result);
    }

    private Bitmap loadImage(final String imageName) throws IOException {
        Uri UriImageName = Uri.parse(imageName);
	final InputStream inputStream = context.getContentResolver().openInputStream( UriImageName );
        final Drawable drawable = Drawable.createFromStream(inputStream, null);
        return ((BitmapDrawable) drawable).getBitmap();
    }
}
