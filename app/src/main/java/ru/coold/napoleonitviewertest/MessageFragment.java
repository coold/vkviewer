package ru.coold.napoleonitviewertest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by rz on 31.05.2015.
 */
public class MessageFragment extends Fragment{
    private Message mMessage;
    public final static String MESSAGE = "ro.coold.napoleonitviewertest.message";
    public static  MessageFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putSerializable(MESSAGE, position);
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Integer position = (Integer)getArguments().getSerializable(MESSAGE);
        mMessage = MessagesLab.get(getActivity()).getMessages().get(position);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_message, container, false);

        TextView bodyText = (TextView)v.findViewById(R.id.bodyText);
        bodyText.setText(mMessage.getBody());

        if(!mMessage.getPictures().isEmpty()){
            for(String pic : mMessage.getPictures()){
                ImageView img = new ImageView(getActivity());
                String path = getActivity().getFileStreamPath(pic).getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                img.setImageDrawable(new BitmapDrawable(getActivity().getResources(), bitmap));
                ((LinearLayout)v.findViewById(R.id.linear)).addView(img);
            }
        }

        return v;
    }


}
