package com.xipu.dukeu;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import Adapter.MyAdapter;
import Model.Message;

/**
 * created on Dec 26, 2017
 *
 */

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinkedList<Message> allMessagesList;
    private List<Message> mMessageList;
    private String baseURL = "https://streamer.oit.duke.edu/social/messages?access_token=";
    private String API_KEY = "cdb7865937fd817b583ff5eed3554b50";//expires in 2018 December
    private final int date_format_string_length = 10;
    private Button nextButton;
    private Button moreInfoButton;
    private Message currentMessage;
    private final Message next = new Message("Next");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nextButton = findViewById(R.id.nextButton);
        moreInfoButton = findViewById(R.id.moreInfoButton);
        mRecyclerView = findViewById(R.id.recyclerViewID);
        mRecyclerView.setHasFixedSize(true); //potential bug
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allMessagesList = new LinkedList<>();
        mMessageList = new LinkedList<>();
        next.setIsBot(false);

        ChatBot bot = new ChatBot();
        mMessageList.add(new Message(bot.greeting(),""));
        fetchDataFromAPI();
        //AVLoadingIndicatorView avi = findViewById(R.id.avi1);
        mAdapter = new MyAdapter(this, mMessageList);
        mRecyclerView.setAdapter(mAdapter);
        //avi.smoothToShow();

    }


    /**
     *  todo: add a counter variable to keep track of the size of linkedlist
     */
    private void fetchDataFromAPI(){
        String url = baseURL + API_KEY;
        RequestQueue queue = Volley.newRequestQueue(this);//using Google volley library

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH)+1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        final String todayDate = String.valueOf(year) + "-" + String.valueOf(month) + "-" +String.valueOf(day);

        //using Google volley library to fetch a JSON array from API
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i = 0; i < response.length(); i++) {

                            try {


                                JSONObject newMessage = response.getJSONObject(i);
                                String date = newMessage.getString("date_posted").substring(0, date_format_string_length);
                                try {
                                    //if the message is posted on today, add it to mMessageList
                                    if (sdf.parse(date).compareTo(sdf.parse(todayDate)) == 0) {
                                        Message nMessage = new Message(newMessage.getString("title"), newMessage.getString("body"));
                                        nMessage.setUrl(newMessage.getString("source_url"));
                                        if(i==0){
                                            currentMessage = nMessage;
                                            mMessageList.add(nMessage);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                        else {
                                            allMessagesList.add(nMessage);
                                        }

                                    }
                                    else{
                                        break; //if the message fetched was posted on an earlier day, stop fetching data
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Log.d("DukeU", "Size:" + allMessagesList.size());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("DukeU", "There's an error while requesting JSON");
                    }
                });

        queue.add(jsArrayRequest);
    }

    //on click method for nextButton textview
    public void getNextMessage(View v){
        if(!allMessagesList.isEmpty()) {
            currentMessage = allMessagesList.remove();
            mMessageList.add(next);
            mMessageList.add(currentMessage);
        }
        else{
            currentMessage = null;
            nextButton.setVisibility(View.INVISIBLE);
            moreInfoButton.setVisibility(View.INVISIBLE);
            Message test = new Message("You're all caught up! Check back later...");
            mMessageList.add(test);
        }
        mRecyclerView.smoothScrollToPosition(mMessageList.size()-1);
        mAdapter.notifyDataSetChanged();
    }

    //on click method when user request more information
    public void getMoreInfo(View v){
        if(currentMessage==null) {
            return;
        }
        Button b = (Button) v;
        Message response = new Message(b.getText().toString());
        response.setIsBot(false);
        mMessageList.add(response);
        //mMessageList.add(new Message(currentMessage.getBody(),""));
        mMessageList.add(new Message(currentMessage.getUrl()));
        Uri uri = Uri.parse(currentMessage.getUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
        mRecyclerView.smoothScrollToPosition(mMessageList.size()-1);
        mAdapter.notifyDataSetChanged();
    }



}

