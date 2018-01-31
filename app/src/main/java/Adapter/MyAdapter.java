package Adapter;

import android.app.LauncherActivity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xipu.dukeu.R;

import org.w3c.dom.Text;

import java.util.List;

import Model.Message;

/**
 * Created by xipu on 1/18/18.
 */

public class MyAdapter extends RecyclerView.Adapter{
    private static final int MESSAGE_SENT_BY_BOT = 0;
    private static final int MESSAGE_SENT_BY_USER = 1;

    private Context mContext;
    private List<Message> mMessageList;


    public MyAdapter(Context context, List<Message> messageList){
        mMessageList = messageList;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if(viewType==MESSAGE_SENT_BY_BOT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_message, parent, false);
            return new Bot_Message_ViewHolder(view);
        }
        else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_message, parent, false);
            return new User_Message_ViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        Message message = mMessageList.get(position);

        if(message.isBot()){
            return MESSAGE_SENT_BY_BOT;
        }
        else{
            return MESSAGE_SENT_BY_USER;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        switch (holder.getItemViewType()){
            case MESSAGE_SENT_BY_BOT:
                ((Bot_Message_ViewHolder) holder).bind(message);
                break;
            case MESSAGE_SENT_BY_USER:
                ((User_Message_ViewHolder) holder).bind(message);

        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    // viewholder class for bot message
    public class Bot_Message_ViewHolder extends RecyclerView.ViewHolder{

        public TextView title;
        public TextView body;

        public Bot_Message_ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.bot_text_message_body);
        }

        void bind(Message message){
            title.setText(message.getTitle());
        }
    }

    // viewholder class for user's message
    public class User_Message_ViewHolder extends RecyclerView.ViewHolder{

        private TextView title;
        private TextView body;

        public User_Message_ViewHolder(View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.text_message_body);
        }

        void bind(Message message){
            title.setText(message.getTitle());
        }

    }

}


