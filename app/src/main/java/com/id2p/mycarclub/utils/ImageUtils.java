package com.id2p.mycarclub.utils;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;

/**
 * Created by anfraga on 2015-06-19.
 */
public class ImageUtils {

    public static final int PROFILE_WIDTH = 200;
    public static final int PROFILE_HEIGHT = 200;
    public static final int THUMBNAIL_WIDTH = 200;
    public static final int THUMBNAIL_HEIGHT = 200;
    public static final int IMAGE_WIDTH = 600;
    public static final int IMAGE_HEIGHT = 480;

    /**
     *
     * @param image
     * @param width
     * @param height
     * @return
     */
    public static byte[] getScaledPhoto(Bitmap image, int width, int height) {
        final Bitmap userImageScaled = Bitmap.createScaledBitmap(image, width, height
                * image.getHeight() / image.getWidth(), false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        userImageScaled.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] scaledData = bos.toByteArray();
        return scaledData;
    }

    /**
     *
     * @param activity
     * @param uri
     * @return
     */
    public static String getPath(Activity activity, Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

}
