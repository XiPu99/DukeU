package Model;

import android.support.annotation.NonNull;
import android.text.format.Time;

/**
 * Created by xipu on 1/18/18.
 * A model class for message
 */

public class Message {
    private String title;
    private String body;
    private boolean isBot;


    public Message(String title, String body){
        this.title = title;
        this.body = body;
        isBot = true;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setIsBot(boolean bot) {
        bot = isBot;
    }

    @Override
    public String toString(){
        return title;
    }

}
