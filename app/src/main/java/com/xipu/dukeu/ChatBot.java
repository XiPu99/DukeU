package com.xipu.dukeu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private PriorityQueue<Message> mMessages;

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

//    public boolean reachEnd(){
//
//    }

    private void getMessages(){

    }


    // retrieve current date
    private Calendar getTodayDate() throws ParseException {
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
