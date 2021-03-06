package com.example.dharmendra.buddy1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class PcChat extends AppCompatActivity implements SecondFragment.OnFragmentInteractionListener{
    String user1;
    private ListView listView;
    Bundle b;
    String user,name;
    String loggedInUserName;
    private FirebaseListAdapter<ChatMessage> adapter;
    FirebaseAuth firebaseAuth;
    TextView t;
    ImageView imageView;
    DatabaseReference mDatabase;
    int message_status=0;
    TextView messageText;
    ArrayList<String> message_list=new ArrayList<>();
    ArrayList<Long> message_time=new ArrayList<>();
    ArrayList<String> message_user=new ArrayList<>();
    ArrayList<Long> search_time_list=new ArrayList<>();
    ArrayList<String> search_user_list=new ArrayList<>();
    ArrayList<String> search_list=new ArrayList<>();
    PcChatAdapterSearch searchadapter;
    EmojiconEditText input;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    int flag=0;
    EmojIconActions emojIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc_chat);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        Intent startingIntent = getIntent();
        if (startingIntent != null) {
                user1 = startingIntent.getStringExtra("Pcchatid");
                name = startingIntent.getStringExtra("Pcname");
                if(user1==null||name==null)
                    flag=0;
                else {
                    flag = 1;

                    setSupportActionBar(toolbar);
                    if (getSupportActionBar() != null){
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        getSupportActionBar().setDisplayShowHomeEnabled(true);
                    }

                }


                Log.d("POPKLJM",user1+"  "+name);
        }
        View rootView = findViewById(R.id.root_view);
        ImageView emojiButton = (ImageView) findViewById(R.id.emoji_btn);





        getWindow().setBackgroundDrawableResource(R.drawable.background) ;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_out_message, null);
        messageText = (TextView) view.findViewById(R.id.message_text);


        ImageView fab = (ImageView) findViewById(R.id.fab);
        input = (EmojiconEditText) findViewById(R.id.input);
        listView = (ListView) findViewById(R.id.list);
        t=(TextView) findViewById(R.id.activity_name);
        imageView=(ImageView)findViewById(R.id.img);
        Log.d("POPKlJM",flag+"");
        if(flag==0) {
            b = getIntent().getExtras();
            if (b != null) {
                user1 = b.getString("int_key");
                name = b.getString("int_key1");
            }
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        t.setText(name);
        emojIcon = new EmojIconActions(this, rootView,input, emojiButton);
        emojIcon.setIconsIds(R.drawable.ic_keyboard, R.drawable.ic_emoji);
        emojIcon.ShowEmojIcon();
        //emojIcon.setUseSystemEmoji(true);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "open");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "close");
            }
        });

        mDatabase=FirebaseDatabase.getInstance().getReference("users").child(user1);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user user = dataSnapshot.getValue(user.class);
                String url=user.getUrl();
                Picasso.with(PcChat.this).load(url).fit().centerCrop().noFade().into(imageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        showAllOldMessages(user1);
        mDatabase=FirebaseDatabase.getInstance().getReference("pcchats").child(user).child(user1);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                message_list.clear();
                message_time.clear();
                message_user.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Log.d("dharam","called");
                    ChatMessage m = postSnapshot.getValue(ChatMessage.class);
                    message_list.add(m.getMessageText());
                    message_time.add(m.getMessageTime());
                    message_user.add(m.getMessageUserId());
                    int message_status=m.getMessage_status();
                    if(message_status==0){
                        postSnapshot.getRef().child("message_status").setValue(1);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckInternetConnection cc=new CheckInternetConnection();
                if(cc.haveNetworkConnection()){
                    if (input.getText().toString().trim().equals("")) {
                        Toast.makeText(PcChat.this, "Please enter some texts!", Toast.LENGTH_SHORT).show();
                    } else {

                        Log.d("lol",String.valueOf(user1));
                        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        message_status=1;
                        FirebaseDatabase.getInstance()
                                .getReference("pcchats").child(user).child(user1)
                                .push()
                                .setValue(new ChatMessage(" "+input.getText().toString(),
                                        FirebaseAuth.getInstance().getCurrentUser().getUid(),message_status)
                                );
                        message_status=0;
                        FirebaseDatabase.getInstance()
                                .getReference("pcchats").child(user1).child(user)
                                .push()
                                .setValue(new ChatMessage(" "+input.getText().toString(),
                                        FirebaseAuth.getInstance().getCurrentUser().getUid(),message_status)
                                );

                        DatabaseReference mDatabase1 =FirebaseDatabase.getInstance().getReference().child("pcnoti");
                                final String id = mDatabase1.push().getKey();
                               ChatMessage2 c=new ChatMessage2(" "+input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getUid(),user1);
                                mDatabase1.child(id).setValue(c);
                        final DatabaseReference mDatabase12 =FirebaseDatabase.getInstance().getReference("users").child(user1).child("connection");
                        mDatabase12.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                        String value=postSnapshot.getValue().toString();
                                        if(value.equals(user)){
                                            final String userId = mDatabase12.push().getKey();
                                            mDatabase12.child(userId).setValue(user);
                                            postSnapshot.getRef().removeValue();
                                            break;
                                        }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        final DatabaseReference mDatabase123 =FirebaseDatabase.getInstance().getReference("users").child(user).child("connection");
                        mDatabase123.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    String value=postSnapshot.getValue().toString();
                                    if(value.equals(user1)){
                                        final String userId = mDatabase123.push().getKey();
                                        mDatabase123.child(userId).setValue(user1);
                                        postSnapshot.getRef().removeValue();
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        input.setText("");
                    }
                }
                else{
                    Toast.makeText(PcChat.this, "Network Error", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main1, menu);
        MenuItem search =  menu.findItem(R.id.search);
        MenuItemCompat.setOnActionExpandListener(search,
                new MenuItemCompat.OnActionExpandListener()
                {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item)
                    {
                     //   Toast.makeText(PcChat.this, "Collapse", Toast.LENGTH_SHORT).show();
                        adapter = new PcMessageAdapter(PcChat.this, ChatMessage.class, R.layout.item_in_message,
                                FirebaseDatabase.getInstance().getReference("pcchats").child(user).child(user1),getApplicationContext());
                        listView.setAdapter(adapter);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item)
                    {
                       // Toast.makeText(PcChat.this, "Expand", Toast.LENGTH_SHORT).show();
                        SearchManager searchManager =
                         (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                         SearchView searchView =
                         (SearchView) menu.findItem(R.id.search).getActionView();
                         searchView.setSearchableInfo(
                         searchManager.getSearchableInfo(getComponentName()));
                         searchView.setSubmitButtonEnabled(true);
                         searchView.setQueryHint(getString(R.string.search_hint));
                         //searchView.setIconifiedByDefault(false);
                         searchView.onActionViewExpanded();
                         searchView.setIconified(false);
                         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query)
                        {

                        return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String query) {
                            search_list.clear();
                            search_time_list.clear();
                            search_user_list.clear();
                            for (int i = 0; i < message_list.size(); i++) {

                                String element = message_list.get(i);
                                if (query.length() <= element.length()) {
                                    //String sub = element.substring(0, query.length());
                                    if (element.contains(query)) {
                                        search_list.add(element);
                                        Long time = message_time.get(i);
                                        search_time_list.add(time);
                                        String user = message_user.get(i);
                                        search_user_list.add(user);

                                }
                            }
                                searchadapter = new PcChatAdapterSearch(query,search_list, search_time_list, search_user_list, PcChat.this, ChatMessage.class, R.layout.item_in_message);
                                listView.setAdapter(searchadapter);
                            }
                        return true;
                        }
                        });
                        return true; // Return true to expand action view
                    }
                });


        return true;
    }
    public void filter_method(String text){

        Log.d("plle",text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
              }
        return true;
    }
    private void goLoginScreen() {
        Intent intent = new Intent(this, Facebook_login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



    private void showAllOldMessages(String user1) {
        loggedInUserName = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("lol",String.valueOf(user1));
        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new PcMessageAdapter(PcChat.this, ChatMessage.class, R.layout.item_in_message,
                FirebaseDatabase.getInstance().getReference("pcchats").child(user).child(user1),getApplicationContext());
        listView.setAdapter(adapter);
         }
    public String getLoggedInUserName() {
        return loggedInUserName;
    }

}
