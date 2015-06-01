package ru.coold.napoleonitviewertest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by rz on 29.05.2015.
 */
public class ListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    public static ListFragment sListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sListFragment = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.vk_grey_color, R.color.vk_black);
        MessagesLab.get(getActivity());

        MessageAdapter adapter = new MessageAdapter(MessagesLab.get(getActivity()).getMessages());

        mListView = (ListView)v.findViewById(R.id.listView);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MessageFragment mf = MessageFragment.newInstance(i);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, mf);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();

            }
        });

        return v;
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);

        new Downloader(getActivity()).downloadAndSave();
    }

    public void setRefreshingForSwipe(boolean b){
        final boolean a =b;
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(a);
            }
        });
    }

    public void readAndUpdateListView(){
        mListView.post(new Runnable() {
            @Override
            public void run() {
                MessageAdapter adapter = new MessageAdapter(MessagesLab.get(getActivity()).getMessages());
                mListView.setAdapter(adapter);
                ((ArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();
                mListView.smoothScrollToPosition(0);
            }
        });
    }

    private class MessageAdapter extends ArrayAdapter<Message>{
        public MessageAdapter(ArrayList<Message> messages){
            super(getActivity(),0,messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item, null);
            }

            Message m = getItem(position);

            TextView authorText = (TextView)convertView.findViewById(R.id.author_text);
            authorText.setText(m.getAuthor());

            TextView bodyText = (TextView)convertView.findViewById(R.id.body_text);
            String temp = "";
            if (m.getBody().length()>50){
                temp = m.getBody().substring(0,49)+"...";
            } else {
                temp = m.getBody();
            }
            bodyText.setText(temp);

            ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);
            imageView.setImageDrawable(null);
            if(!m.getPictures().isEmpty()){
                String path = getActivity().getFileStreamPath(m.getPictures().get(0)).getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                imageView.setImageDrawable(new BitmapDrawable(getActivity().getResources(), bitmap));
            }



            return convertView;
        }
    }
}
