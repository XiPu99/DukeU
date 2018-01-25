package com.xipu.dukeu;

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
import java.util.PriorityQueue;

import Model.Message;

/**
 * A simple bot that sends greeting to user based on time and
 *
 *
 * Created by xipu on 1/18/18.
 */

public class ChatBot {

    private int pos;
    private ArrayList<Message> mMessages;
    private final String baseURL = "https://streamer.oit.duke.edu/social/messages?access_token=";
    private final String API_KEY = "cdb7865937fd817b583ff5eed3554b50";//expires in 2018 December

    public ChatBot() {
        //System.out.println(greeting());
    }

    public String greeting(){
        Calendar now = Calendar.getInstance();
        int hour_of_day = now.get(Calendar.HOUR_OF_DAY);

        if(hour_of_day>=6&&hour_of_day<13){
            return "Good Morning!";
        }
        else if(hour_of_day>=13&&hour_of_day<6){
            return "Good afternoon!";
        }
        else{
            return "Good evening!";
        }
    }


    // retrieve current date
    public Calendar getTodayDate() throws ParseException {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        now.set(year, month, day);
        String date = "2018-01-20";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        now.setTime(sdf.parse(date));
        return now;
    }
    
}
