package com.xipu.dukeu;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

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
import it.gmariotti.recyclerview.adapter.AlphaAnimatorAdapter;
import it.gmariotti.recyclerview.adapter.SlideInLeftAnimatorAdapter;
import it.gmariotti.recyclerview.itemanimator.SlideInOutLeftItemAnimator;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

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
        mRecyclerView.setNestedScrollingEnabled(false);

        ChatBot bot = new ChatBot();
        mMessageList.add(new Message(bot.greeting(),""));
        fetchDataFromAPI();
        mAdapter = new MyAdapter(this, mMessageList);
        mRecyclerView.setAdapter(mAdapter);
        Animation slide_up = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slide_up.reset();
        nextButton.startAnimation(slide_up);
        moreInfoButton.startAnimation(slide_up);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                boolean isBottomReached = recyclerView.canScrollVertically(1);
                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                slide_up.reset();
                slide_down.reset();



                if(dy==0){
                    if(nextButton.isShown()){
                        slide_down.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                                nextButton.clearAnimation();
                                moreInfoButton.clearAnimation();
                                nextButton.startAnimation(slide_up);
                                moreInfoButton.startAnimation(slide_up);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        slide_down.reset();
                        nextButton.clearAnimation();
                        moreInfoButton.clearAnimation();
                        nextButton.startAnimation(slide_down);
                        moreInfoButton.startAnimation(slide_down);
                    }
                }

                if(!isBottomReached){

                    if(!nextButton.isShown()){
                        nextButton.setVisibility(View.VISIBLE);
                        moreInfoButton.setVisibility(View.VISIBLE);
                        nextButton.clearAnimation();
                        moreInfoButton.clearAnimation();
                        nextButton.startAnimation(slide_up);
                        moreInfoButton.startAnimation(slide_up);
                    }
                    return;
                }

                if ( dy>0){
                    //scroll down
                    if(nextButton.isShown()){
                        slide_down.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                                //nextButton.clearAnimation();
                                //moreInfoButton.clearAnimation();
                                nextButton.startAnimation(slide_up);
                                moreInfoButton.startAnimation(slide_up);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        slide_down.reset();
                        nextButton.clearAnimation();
                        moreInfoButton.clearAnimation();
                        nextButton.startAnimation(slide_down);
                        moreInfoButton.startAnimation(slide_down);
                    }
                }

                else if(dy<0){
                    //Scroll up
                    if (nextButton.isShown()) {
                        nextButton.clearAnimation();
                        moreInfoButton.clearAnimation();
                        nextButton.startAnimation(slide_down);
                        moreInfoButton.startAnimation(slide_down);
                        nextButton.setVisibility(View.INVISIBLE);
                        moreInfoButton.setVisibility(View.INVISIBLE);
                    }
                }


            }

        });


    }


    /**
     *  add all messages fetched from the Internet to linkedlist allMessagesList
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
                                    if (sdf.parse(date).compareTo(sdf.parse(todayDate)) == 0||sdf.parse(todayDate).before(sdf.parse(date))) {
                                        Message nMessage = new Message(newMessage.getString("title"), newMessage.getString("body"));
                                        nMessage.setUrl(newMessage.getString("source_url"));
                                        if(i==0){
                                            currentMessage = nMessage;
                                            mMessageList.add(nMessage);
                                            mAdapter.notifyItemInserted(mMessageList.size()-1);
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
            nextButton.setVisibility(View.GONE);
            moreInfoButton.setVisibility(View.GONE);
            Message test = new Message("You're all caught up! Check back later...");
            mMessageList.add(test);
        }
        if(!moreInfoButton.isShown()){
            moreInfoButton.setVisibility(View.VISIBLE);
        }
        Animation slide_down = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        slide_down.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                nextButton.startAnimation(slide_up);
                moreInfoButton.startAnimation(slide_up);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slide_down.reset();
        nextButton.startAnimation(slide_down);
        moreInfoButton.startAnimation(slide_down);

        mRecyclerView.smoothScrollToPosition(mMessageList.size()-1);
        mAdapter.notifyItemInserted(mMessageList.size()-1);

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
        mMessageList.add(new Message(currentMessage.getUrl()));
        Uri uri = Uri.parse(currentMessage.getUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
        mRecyclerView.smoothScrollToPosition(mMessageList.size()-1);
        moreInfoButton.setVisibility(View.GONE);
        mAdapter.notifyItemInserted(mMessageList.size()-1);
    }



}

