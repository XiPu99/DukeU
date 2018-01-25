package com.xipu.dukeu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewAccessibilityDelegate;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private List<Message> mMessagesList;
    private String baseURL = "https://streamer.oit.duke.edu/social/messages?access_token=";
    private String API_KEY = "cdb7865937fd817b583ff5eed3554b50";//expires in 2018 December
    private final int date_format_string_length = 10;
    //private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recyclerViewID);
        mRecyclerView.setHasFixedSize(true); //potential bug
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMessagesList = new ArrayList<>();

        ChatBot bot = new ChatBot();
        mMessagesList.add(new Message(bot.greeting(),"")); //display greeting on screen
        fetchDataFromAPI();
        //AVLoadingIndicatorView avi = findViewById(R.id.avi1);
        mAdapter = new MyAdapter(this, mMessagesList);
        mRecyclerView.setAdapter(mAdapter);
        //avi.smoothToShow();
    }

    /**
     *
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
                                    //
                                    if (sdf.parse(date).compareTo(sdf.parse(todayDate)) == 0) {
                                        mMessagesList.add(new Message(newMessage.getString("title"), newMessage.getString("body")));
                                    }
                                    else{
                                        break; //if the message fetched was posted on an earlier day, stop fetching data
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("DukeU", "There's an error while requesting JSON");
                    }
                });
        // if no new messages are added, give users information
        if(mMessagesList.size()==1){
            mMessagesList.add(new Message("There's no news!", "You're all caught up! Check back later..."));
        }
        queue.add(jsArrayRequest);
    }

}

