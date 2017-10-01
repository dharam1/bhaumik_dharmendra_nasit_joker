package com.example.dharmendra.buddy1;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StreamDownloadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageAdapter extends FirebaseListAdapter<ChatMessage1> {
    ClipData myClip;
    ClipboardManager clipboard ;
    Chat activity;
    String user,user1;
    DatabaseReference mDatabase,mDatabase1;
    int seen;
    boolean exist;
    int cidd;

    public MessageAdapter(Chat activity, Class<ChatMessage1> modelClass, int modelLayout, DatabaseReference ref,int cidd) {
        super(activity, modelClass, modelLayout, ref);
        this.activity = activity;
        this.mDatabase1=ref;
        this.cidd=cidd;
        user=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    @Override
    protected void populateView(View v, final ChatMessage1 model, int position) {
        TextView messageText = (TextView) v.findViewById(R.id.message_text);
        TextView messageUser = (TextView) v.findViewById(R.id.message_user);
        TextView messageTime = (TextView) v.findViewById(R.id.message_time);
        String user=FirebaseAuth.getInstance().getCurrentUser().getUid();
        //TextView messageDate = (TextView) v.findViewById(R.id.message_date);
        messageText.setText(model.getMessageText());
        if(model.getMessageUserId().equals(user)){
            messageUser.setText("You");
        }
        else
        messageUser.setText(model.getNickname());

        String nontime= DateFormat.format("HH:mm:ss",model.getMessageTime()).toString();
        SimpleDateFormat f1 = new SimpleDateFormat("HH:mm:ss");
        try{
            Date d = f1.parse(nontime);
            SimpleDateFormat f2 = new SimpleDateFormat("h:mm a");
            messageTime.setText(f2.format(d).toUpperCase());
        }
        catch (Exception e){

        }
        clipboard=(ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String content =model.getMessageText();
                Log.d("ronaldo",content);
                myClip = ClipData.newPlainText("text",content);
                clipboard.setPrimaryClip(myClip);
                return true;
            }
        });

    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final ChatMessage1 chatMessage = getItem(position);
        if (chatMessage.getMessageUserId().equals(activity.getLoggedInUserName()))
            view = activity.getLayoutInflater().inflate(R.layout.item_out_message, viewGroup, false);
        else
            view = activity.getLayoutInflater().inflate(R.layout.item_in_message, viewGroup, false);

        //generating view
        populateView(view, chatMessage, position);

        return view;
    }

    @Override
    public int getViewTypeCount() {
        // return the total number of view types. this value should never change
        // at runtime
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // return a value between 0 and (getViewTypeCount - 1)
        return position % 2;
    }
}