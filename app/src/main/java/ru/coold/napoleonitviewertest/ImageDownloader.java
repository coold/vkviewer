package ru.coold.napoleonitviewertest;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Created by rz on 30.05.2015.
 */
public class ImageDownloader extends AsyncTask<Void, Void, Void> {
    private Message mMessage;
    private Context mContext;

    public ImageDownloader(Message m, Context c){
        mMessage = m;
        mContext = c;
    }
    @Override
    protected Void doInBackground(Void... voids) {

        if(mMessage.getPicturesURL().isEmpty()){

        } else {

            for (String addr : mMessage.getPicturesURL()) {
                FileOutputStream out = null;
                InputStream input = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(addr);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    String filename = addr.substring(addr.lastIndexOf('/') + 1, addr.length());
                    out = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    mMessage.getPictures().add(filename);



                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    synchronized (Downloader.counter) {
                        Downloader.counter += 1;
                        if (Downloader.counter == Downloader.size && Downloader.counterMessages==Downloader.sizeMessages) {
                            new Downloader(mContext).finishDownload();
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();

                    }

                }
            }
        }
        return null;
    }


}
