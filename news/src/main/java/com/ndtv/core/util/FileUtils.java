/**
 Project      : Awaaz
 Filename     : FileUtils.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author anudeep
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context    The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use
        // external cache dir
        // otherwise use internal cache dir

        // TODO: getCacheDir() should be moved to a background thread as it
        // attempts to create the
        // directory if it does not exist (no disk access should happen on the
        // main/UI thread).
        // String path = getExternalCacheDir(context).getAbsolutePath();
        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable() ? getExternalCacheDir(context) == null ? context
                .getCacheDir().getPath() : getExternalCacheDir(context).getAbsolutePath() : context.getCacheDir()
                .getPath();
        // final File cachePath = context.getCacheDir();
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    private static File getExternalCacheDir(Context context) {
        // TODO: This needs to be moved to a background thread to ensure no disk
        // access on the
        // main/UI thread as unfortunately getExternalCacheDir() calls mkdirs()
        // for us (even
        // though the Volley library will later try and call mkdirs() as well
        // from a background
        // thread).
        File externalFilesDir = context.getExternalFilesDir(null);

        Log.v(TAG, "context.getExternalFilesDir(null) is null");
        return externalFilesDir;
    }

    public static boolean saveStream(InputStream is, String filePath) {
        OutputStream os = null;
        try {
            FileOutputStream fos = new FileOutputStream(filePath, true);
            os = new BufferedOutputStream(fos);
            byte[] buffer = new byte[8 * 1024];
            int byteRead = 0;
            while ((byteRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static boolean clearApplicationData(Context ctx) {
        File cache = getExternalCacheDir(ctx);
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.v("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }

        cache = ctx.getCacheDir();
        appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.v("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }

        return true;
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public static Uri getUri(String type) {
        String state = Environment.getExternalStorageState();
        if ("image".equals(type)) {
            if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
                return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
                return MediaStore.Video.Media.INTERNAL_CONTENT_URI;

            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type))
            if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
                return MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

}
