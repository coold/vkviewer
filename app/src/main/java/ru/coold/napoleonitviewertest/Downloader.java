package ru.coold.napoleonitviewertest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.dialogs.VKCaptchaDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by rz on 30.05.2015.
 */
public class Downloader {
    public static Integer size;
    public static Integer counter;
    public static boolean isDownloaded;
    public static Integer counterMessages;
    public static Integer sizeMessages;
    private Context mContext;
    public Downloader(Context c){
        mContext = c;
    }
    private static String[] sMyScope = new String[]{VKScope.FRIENDS, VKScope.WALL, VKScope.PHOTOS, VKScope.NOHTTPS, VKScope.GROUPS};

    private VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            VKSdk.authorize(sMyScope);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {

        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {

        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
        }
    };


    public void downloadAndSave(){
        isDownloaded = false;
        counter = 0;
        size = 0;
        sizeMessages = 0;
        counterMessages = 0;
        final String groupId = "93030892";
        MessagesLab.get(mContext).setMessages(new ArrayList<Message>());
        if(DownloaderReceiver.inBackground){
            VKSdk.initialize(sdkListener, "4937473");
            VKSdk.authorize(sMyScope, true, false);
        }


        VKRequest request = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID, "-"+groupId));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                try {
                    JSONObject res = (JSONObject)response.json.get("response");
                    JSONArray arr = (JSONArray)res.get("items");
                    for(int i=0;i<arr.length();i++){
                        Message m = new Message();
                        String body = arr.getJSONObject(i).get("text").toString();
                        m.setBody(body);
                        String ID = arr.getJSONObject(i).get("from_id").toString();
                        m.setAuthorID(ID);

                        if(arr.getJSONObject(i).has("attachments")) {
                            JSONArray photos = (JSONArray) arr.getJSONObject(i).get("attachments");
                            ArrayList<String> photosURL = new ArrayList<String>();


                            for (int j = 0; j < photos.length(); j++) {
                                if (!photos.getJSONObject(j).get("type").equals("photo")) continue;
                                Log.d("VK", ((JSONObject) photos.getJSONObject(j).get("photo")).toString());
                                String url = (String) ((JSONObject) photos.getJSONObject(j).get("photo")).get("photo_604");
                                photosURL.add(url);
                            }
                            synchronized (size){
                                size+=photosURL.size();
                            }
                            m.setPicturesURL(photosURL);
                        }

                        synchronized (sizeMessages){
                            sizeMessages++;
                        }

                        MessagesLab.get(mContext).getMessages().add(m);
                        new ImageDownloader(m, mContext).execute();

                    }

                    Log.d("VK",MessagesLab.get(mContext).getMessages().size()+"");
                    for(Message m : MessagesLab.get(mContext).getMessages()){
                        final int index = MessagesLab.get(mContext).getMessages().indexOf(m);
                        if(m.getAuthorID().contains("-")){
                            VKRequest request = VKApi.groups().getById(VKParameters.from(VKApiConst.GROUP_ID, groupId));
                            request.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    try {
                                        JSONArray arr = response.json.getJSONArray("response");
                                        String name = arr.getJSONObject(0).getString("name");
                                        MessagesLab.get(mContext).getMessages().get(index).setAuthor(name);
                                        Log.d("VK","name: "+name);
                                        synchronized (counterMessages){
                                            counterMessages++;
                                            if(counterMessages==sizeMessages && counter==size) finishDownload();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                                    synchronized (counterMessages){
                                        counterMessages++;
                                        if(counterMessages==sizeMessages && counter==size) finishDownload();
                                    }
                                }

                                @Override
                                public void onError(VKError error) {
                                    synchronized (counterMessages){
                                        counterMessages++;
                                        if(counterMessages==sizeMessages && counter==size) finishDownload();
                                    }
                                }
                            });
                        } else {
                            VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, m.getAuthorID()));
                            request.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    try {
                                        JSONArray res = (JSONArray)response.json.get("response");
                                        String first_name = res.getJSONObject(0).getString("first_name");
                                        String last_name = res.getJSONObject(0).getString("last_name");
                                        MessagesLab.get(mContext).getMessages().get(index).setAuthor(first_name+" "+last_name);
                                        Log.d("VK", "name: " + first_name+" "+last_name);
                                        synchronized (counterMessages){
                                            counterMessages++;
                                            if(counterMessages==sizeMessages && counter==size) finishDownload();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                                    synchronized (counterMessages){
                                        counterMessages++;
                                        if(counterMessages==sizeMessages && counter==size) finishDownload();
                                    }
                                }

                                @Override
                                public void onError(VKError error) {
                                    synchronized (counterMessages){
                                        counterMessages++;
                                        if(counterMessages==sizeMessages && counter==size) finishDownload();
                                    }
                                }
                            });
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                Log.d("VK", "failed");
            }

            @Override
            public void onError(VKError error) {
                Log.d("VK", error.toString());
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
            }
        });


    }

    public void finishDownload(){
        Downloader.isDownloaded = true;
        if(!DownloaderReceiver.inBackground) ListFragment.sListFragment.setRefreshingForSwipe(false);
        MessagesLab.get(mContext).saveMessages();
        if(!DownloaderReceiver.inBackground) ListFragment.sListFragment.readAndUpdateListView();
    }

    public void saveMessages(ArrayList<Message> messages) throws JSONException, IOException{
        JSONArray array = new JSONArray();
        for (Message n : messages){
            array.put(n.toJSON());
        }

        Writer writer = null;
        try {
            OutputStream out = mContext.openFileOutput("messages.json", Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if(writer!=null){
                writer.close();
            }
        }

    }

    public ArrayList<Message> loadMessages(Context c) throws JSONException, IOException {
        ArrayList<Message> notes = new ArrayList<Message>();
        BufferedReader reader = null;
        try{
            InputStream in = c.openFileInput("messages.json");
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while((line = reader.readLine())!=null) {
                jsonString.append(line);
            }
            Log.d("VK","readed "+jsonString.toString());
            JSONArray array = (JSONArray)new JSONTokener(jsonString.toString()).nextValue();
            for(int i =0; i<array.length();i++){
                notes.add(new Message(array.getJSONObject(i)));
            }

        } catch (FileNotFoundException e){
            e.printStackTrace();
        } finally {
            if(reader!=null){
                reader.close();
            }
        }
        return notes;
    }
}
