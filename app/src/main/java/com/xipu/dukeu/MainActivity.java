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
    //private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recyclerViewID);
        mRecyclerView.setHasFixedSize(true); //potential bug
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMessagesList = new ArrayList<>();

        String url = baseURL + API_KEY;
        RequestQueue queue = Volley.newRequestQueue(this);

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH)+1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        final String todayDate = String.valueOf(year) + "-" + String.valueOf(month) + "-" +String.valueOf(day);
        Log.d("date", todayDate);
        try {
            Date today = sdf.parse(todayDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        for(int i = 0; i < response.length(); i++)
                            try {
                                JSONObject newMessage = response.getJSONObject(i);
                                String date = newMessage.getString("date_posted").substring(0,10);
                                Log.d("DukeU", date);
                                try {
                                    Log.d("format", sdf.parse(date).toString());
                                    Log.d("format", sdf.parse(todayDate).toString());
                                    Log.d("format", String.valueOf(sdf.parse(date).compareTo(sdf.parse(todayDate))));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                mMessagesList.add(new Message(newMessage.getString("title"), newMessage.getString("body")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("DukeU", "There's an error while requesting JSON");
                    }
                });

        queue.add(jsArrayRequest);
        //getInfoFromURL(baseURL+API_KEY);
        //Log.d("DukeU",  System.currentTimeMillis()));
        //Calendar now = Calendar.getInstance();
        Date today = now.getTime();
        Log.d("time", today.toString());
        mAdapter = new MyAdapter(this, mMessagesList);
        mRecyclerView.setAdapter(mAdapter);
    }









}

