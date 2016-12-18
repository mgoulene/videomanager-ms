package com.accenture.videomanager.web.rest.utils;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by vagrant on 12/9/16.
 */
public class GetPictureFromURL {
    public static byte[] getBytes(TmdbApi api, String imagePath) {
        URL imageURL = Utils.createImageUrl(api, imagePath, "original");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        byte[] imageData = null;
        try {
            is = imageURL.openStream ();
            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
            int n;

            while ( (n = is.read(byteChunk)) > 0 ) {
                baos.write(byteChunk, 0, n);
            }
            imageData = baos.toByteArray();
        }
        catch (IOException e) {
            System.err.printf ("Failed while reading bytes from %s: %s", imageURL.toExternalForm(), e.getMessage());
            e.printStackTrace ();
            // Perform any other exception handling that's appropriate.
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return imageData;

    }
}
