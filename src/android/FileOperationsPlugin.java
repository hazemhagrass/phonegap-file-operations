/**
 *
 * Phonegap FileOperations plugin
 *
 * Version 1.0
 *
 * Hazem Hagrass 2013
 *
 */

package com.badrit.FileOperations;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Environment;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileOperationsPlugin extends CordovaPlugin {

    private Context context;

    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callbackContext) throws JSONException {
        this.context = cordova.getActivity().getApplicationContext();

        JSONObject parameters = args.getJSONObject(0);
        if ("copy".equals(action)) {
            try {
                if (parameters != null) {
                    String to = Environment.getExternalStorageDirectory() + parameters
                            .getString("to");

                    Boolean status = copy(parameters
                            .getString("from"), to);

                    JSONObject response = new JSONObject();
                    response.put("from", parameters
                            .getString("from"));
                    response.put("to", to);
                    response.put("status", status);

                    callbackContext.success(response.toString());
                }
            } catch (Exception e) {

            }

            return true;
        } else if ("delete".equals(action)) {
            if (parameters != null) {
                Boolean result = delete(parameters
                        .getString("path"));
                callbackContext.success(result + "");
            }
        }
        return false;
    }

    public Boolean delete(String path) {
        File file = getFile(path);
        file.delete();
        return true;
    }

    public Boolean copy(String from, String to) {
        try {
            File src = getFile(from);
            File dst = getFile(to);

            if (!dst.exists())
                dst.createNewFile();

            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private File getFile(String path) {
        File file;
        // Handle the special case where you get an Android content:// uri.
        if (path.contains("content://")) {
            Uri mediaUri = Uri.parse(path);
            file = new File(getMediaFile(mediaUri));
        } else {
            file = new File(path);
        }
        return file;
    }

    private String getMediaFile(Uri thumbUri) {
        AssetFileDescriptor afd = null;
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            afd = context.getContentResolver().
                    openAssetFileDescriptor(thumbUri, "r");

            FileDescriptor fdd = afd.getFileDescriptor();

            inputStream = new FileInputStream(fdd);
            File file = File.createTempFile("media", ".tmp");
            file.deleteOnExit();
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();

            return "file://" + file.getAbsolutePath();
        } catch (Exception e) {
        } finally {
            try {
                if (afd != null)
                    afd.close();
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
            }
        }
        return "";
    }

}

