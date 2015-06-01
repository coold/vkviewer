package ru.coold.napoleonitviewertest;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by rz on 30.05.2015.
 */
public class Message {
    private String mAuthor;
    private String mBody;
    private ArrayList<String> mPictures;
    private ArrayList<String> mPicturesURL;
    private String mAuthorID;
    private String JSON_AUTHOR = "author";
    private String JSON_TEXT = "body";
    private String JSON_PICTURES = "pictures";

    public Message(){
        mPictures = new ArrayList<String>();
        mPicturesURL = new ArrayList<String>();
        mAuthor = "";
        mBody = "";
    }

    public Message(JSONObject json) throws JSONException {
        this.mAuthor = json.getString(JSON_AUTHOR);
        this.mBody = json.getString(JSON_TEXT);
        this.mPictures = new ArrayList<String>();
        if(!json.get(JSON_PICTURES).equals("[]")){
            String[] items = json.getString(JSON_PICTURES).replaceAll("\\[", "").replaceAll("\\]", "").split(",");
            for(String s : items){
                this.mPictures.add(s);
            };
        }


    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_AUTHOR, mAuthor);
        json.put(JSON_TEXT, mBody);
        json.put(JSON_PICTURES, mPictures);
        return json;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(mAuthor+" ");
        sb.append(mBody+" ");
        if(mPictures!=null) {
            for (String s : mPictures) {
                sb.append(s + " ");
            }
        }
        return sb.toString();
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public ArrayList<String> getPictures() {
        return mPictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        mPictures = pictures;
    }

    public ArrayList<String> getPicturesURL() {
        return mPicturesURL;
    }

    public void setPicturesURL(ArrayList<String> picturesURL) {
        mPicturesURL = picturesURL;
    }

    public String getAuthorID() {
        return mAuthorID;
    }

    public void setAuthorID(String authorID) {
        mAuthorID = authorID;
    }
}
