package ru.coold.napoleonitviewertest;

import android.content.Context;
import android.database.sqlite.SQLiteDoneException;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by rz on 30.05.2015.
 */
public class MessagesLab {
    private ArrayList<Message> mMessages;
    private static MessagesLab mMessagesLab;
    private Context mContext;
    private MessagesLab(Context c){

        mContext = c;
        try {
            mMessages = new Downloader(mContext).loadMessages(mContext);
            Log.d("VK", "loaded");
        } catch (Exception e) {
            e.printStackTrace();
            mMessages = new ArrayList<Message>();
            Log.d("VK", "not loaded");
        }
    }

    public static MessagesLab get(Context c){
        if(mMessagesLab==null) {
            mMessagesLab = new MessagesLab(c);
        }
        return mMessagesLab;
    }

    public ArrayList<Message> getMessages(){
        return mMessages;
    }

    public void setMessages(ArrayList<Message> arr){
        mMessages = arr;
    }

    public void saveMessages(){
        try {
            new Downloader(mContext).saveMessages(mMessages);
            Log.d("VK", "saved");
        } catch (Exception e) {
            Log.d("VK", "not saved");
        }
    }
}
