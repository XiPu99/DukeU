package com.xipu.dukeu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

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
    private LinearLayout mLinearLayout;
    private Message firstMessage;
    private boolean isFirstTime;
    private boolean allCaughtUp;
    private final Message next = new Message("Next");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if app is launched for the first time
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("Time", false)){
            Log.d("DukeU", "Enter first time");
            mLinearLayout = findViewById(R.id.linearLayout);
            nextButton = findViewById(R.id.nextButton);
            moreInfoButton = findViewById(R.id.moreInfoButton);
            mRecyclerView = findViewById(R.id.recyclerViewID);
            mRecyclerView.setHasFixedSize(true); //potential bug
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            allMessagesList = new LinkedList<>();
            mMessageList = new LinkedList<>();
            next.setIsBot(false);
            mRecyclerView.setNestedScrollingEnabled(false);

            mAdapter = new MyAdapter(this, mMessageList);
            firstTimeSetUp();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Time", true);
            isFirstTime = true;
            editor.commit();
        }

        // execute the following code if the app is not launched for the first time
        else {
            Log.d("DukeU", "Not first time");
            mLinearLayout = findViewById(R.id.linearLayout);
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
            mMessageList.add(new Message(bot.greeting(), ""));
            fetchDataFromAPI();
            mAdapter = new MyAdapter(this, mMessageList);
            mRecyclerView.setAdapter(mAdapter);
            Animation slide_up = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            slide_up.reset();
            mLinearLayout.startAnimation(slide_up);
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    boolean isBottomReached = recyclerView.canScrollVertically(1);
                    Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                    Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                    slide_up.reset();
                    slide_down.reset();

                    if (!isBottomReached) {

                        if (!nextButton.isShown()) {
                            Log.d("Anim", "not shown executed.");
                            mLinearLayout.clearAnimation();
                            mLinearLayout.startAnimation(slide_up);
                            mLinearLayout.setVisibility(View.VISIBLE);
                        }
                        return;
                    }

                    if (dy > 0) {
                        //scroll down
                        if (nextButton.isShown()) {
                            Log.d("Anim", "scrolling down.");
                            slide_down.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    //do nothing
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                                    mLinearLayout.clearAnimation();
                                    mLinearLayout.startAnimation(slide_up);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                    //do nothing
                                }
                            });
                            mLinearLayout.startAnimation(slide_down);
                        }
                    } else if (dy < 0) {
                        //Scroll up
                        if (nextButton.isShown()) {
                            Log.d("Anim", "scrolling up.");
                            mLinearLayout.clearAnimation();
                            mLinearLayout.startAnimation(slide_down);
                            mLinearLayout.setVisibility(View.INVISIBLE);
                        }
                    }

                }

            });
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateMessageList();
        if(allCaughtUp){
            if (!allMessagesList.isEmpty()){
                Message welcome = new Message("Welcome Back.");
                mMessageList.add(welcome);
                mAdapter.notifyItemInserted(mMessageList.size()-1);
                currentMessage = allMessagesList.remove();
                mMessageList.add(currentMessage);
                mAdapter.notifyItemInserted(mMessageList.size()-1);
                nextButton.setVisibility(View.VISIBLE);
                moreInfoButton.setVisibility(View.VISIBLE);
                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                mLinearLayout.startAnimation(slide_up);
            }
            else{
                Message soon = new Message("Sorry, my fellow Dukie. Nothing interesting happened after you left.");
                mMessageList.add(soon);
                mAdapter.notifyItemInserted(mMessageList.size()-1);
            }
        }
    }


    private void updateMessageList(){
        final Message oldFirstMessage = firstMessage;
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
                                        Message nMessage = new Message(newMessage.getString("title"));
                                        nMessage.setUrl(newMessage.getString("source_url"));
                                        if(nMessage.compare(oldFirstMessage)!=0 && i == 0){
                                            Log.d("message", nMessage.getTitle());
                                            allMessagesList.add(nMessage);
                                            firstMessage = nMessage;
                                        }
                                        else if (nMessage.compare(oldFirstMessage) == 0) {
                                            break;
                                        } else {
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
                        Message errorMessage1 = new Message("Oops!!!");
                        Message errorMessage2 = new Message("It seems that I can't connect to Internet. Can you check your WiFi setting?");
                        mMessageList.add(errorMessage1);
                        mMessageList.add(errorMessage2);
                        mAdapter.notifyItemInserted(mMessageList.size()-1);
                        mLinearLayout.clearAnimation();
                        nextButton.setVisibility(View.INVISIBLE);
                        moreInfoButton.setVisibility(View.INVISIBLE);
                        Log.d("DukeU", "There's an error while requesting JSON");
                    }
                });

        queue.add(jsArrayRequest);
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
                            Log.d("Loop", "Loop entered");
                            try {


                                JSONObject newMessage = response.getJSONObject(i);
                                Log.d("Loop", newMessage.getString("title"));
                                String date = newMessage.getString("date_posted").substring(0, date_format_string_length);
                                try {
                                    //if the message is posted on today, add it to mMessageList
                                    if (sdf.parse(date).compareTo(sdf.parse(todayDate)) == 0||sdf.parse(todayDate).before(sdf.parse(date))) {
                                        Message nMessage = new Message(newMessage.getString("title"), newMessage.getString("body"));
                                        nMessage.setUrl(newMessage.getString("source_url"));
                                        if(i==0){
                                            currentMessage = nMessage;
                                            firstMessage = nMessage;
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
                        Message errorMessage1 = new Message("Oops!!!");
                        Message errorMessage2 = new Message("It seems that I can't connect to Internet. Can you check your WiFi setting?");
                        mMessageList.add(errorMessage1);
                        mMessageList.add(errorMessage2);
                        mAdapter.notifyItemInserted(mMessageList.size()-1);
                        mLinearLayout.clearAnimation();
                        nextButton.setVisibility(View.INVISIBLE);
                        moreInfoButton.setVisibility(View.INVISIBLE);
                        Log.d("DukeU", "There's an error while requesting JSON");
                    }
                });

        queue.add(jsArrayRequest);
    }

    //on click method for nextButton textview
    public void getNextMessage(View v){
        if(isFirstTime){
            Message yes = new Message("Yes.");
            yes.setIsBot(false);
            mMessageList.add(yes);
            fetchDataFromAPI();
            isFirstTime = false;
        }

        else {


            if (!allMessagesList.isEmpty()) {
                currentMessage = allMessagesList.remove();
                mMessageList.add(next);
                mMessageList.add(currentMessage);
                allCaughtUp = false;
            } else {
                nextButton.setVisibility(View.GONE);
                moreInfoButton.setVisibility(View.GONE);
                Message test = new Message("You're all caught up! Check back later...");
                mMessageList.add(test);
                mAdapter.notifyItemInserted(mMessageList.size() - 1);
                allCaughtUp = true;
                return;
            }

        }
        Animation slide_down = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        if(!moreInfoButton.isShown()){
            nextButton.startAnimation(slide_down);
        }
        slide_down.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                nextButton.setText("Next");
                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                if(!moreInfoButton.isShown()) moreInfoButton.setVisibility(View.VISIBLE);
                mLinearLayout.startAnimation(slide_up);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //do nothing
            }
        });
        mRecyclerView.smoothScrollToPosition(mMessageList.size()-1);
        mAdapter.notifyItemInserted(mMessageList.size()-1);
        mLinearLayout.startAnimation(slide_down);
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

    public void firstTimeSetUp() {
        Animation slide_up = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slide_up.reset();
        //mLinearLayout.startAnimation(slide_up);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                boolean isBottomReached = recyclerView.canScrollVertically(1);
                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                slide_up.reset();
                slide_down.reset();

                if(!isBottomReached){

                    if(!nextButton.isShown()){
                        Log.d("Anim", "not shown executed.");
                        mLinearLayout.clearAnimation();
                        mLinearLayout.startAnimation(slide_up);
                        mLinearLayout.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                if ( dy > 0){
                    //scroll down
                    if(nextButton.isShown()){
                        Log.d("Anim", "scrolling down.");
                        slide_down.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                //do nothing
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                                mLinearLayout.clearAnimation();
                                mLinearLayout.startAnimation(slide_up);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                //do nothing
                            }
                        });
                        mLinearLayout.startAnimation(slide_down);
                    }
                }

                else if(dy<0){
                    //Scroll up
                    if (nextButton.isShown()) {
                        Log.d("Anim", "scrolling up.");
                        mLinearLayout.clearAnimation();
                        mLinearLayout.startAnimation(slide_down);
                        mLinearLayout.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        mLinearLayout.setVisibility(View.GONE);
        mAdapter = new MyAdapter(this, mMessageList);
        mRecyclerView.setAdapter(mAdapter);
        mMessageList.add(new Message("Hey there."));
        mAdapter.notifyItemInserted(mMessageList.size()-1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMessageList.add(new Message("Thanks for trying my new app! It's like a conversation. "));
                mAdapter.notifyItemInserted(mMessageList.size()-1);
            }
        }, 1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMessageList.add(new Message("I send you messages, and you can respond below by tapping the buttons that appear. Are you ready to get started?"));
                mAdapter.notifyItemInserted(mMessageList.size()-1);
            }
        }, 2500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                mLinearLayout.setVisibility(View.VISIBLE);
                mLinearLayout.startAnimation(slide_up);
                nextButton.setText("Yes.");
                nextButton.setVisibility(View.VISIBLE);
                moreInfoButton.setVisibility(View.GONE);
            }
        }, 3300);
    }

}

