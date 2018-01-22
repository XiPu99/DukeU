package Model;

import android.support.annotation.NonNull;
import android.text.format.Time;

/**
 * Created by xipu on 1/18/18.
 * A model class for message
 */

public class Message implements Comparable<Message>{
    private String title;
    private String body;
    private Time date_posted;
    private boolean isRead;

    public Message(){

    }

    public Message(String title, String body){
        this.title = title;
        this.body = body;
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

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString(){
        return title;
    }

    @Override
    public int compareTo(@NonNull Message o) {
        return Time.compare(this.date_posted, o.date_posted);
    }
}
