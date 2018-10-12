package com.example.firebasechattingapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseRecyclerAdapter<ChatMessage,MessageViewHolder> mFirebaseAdapter;
    public static final String MESSAGE_CHIld = "message";
    private DatabaseReference mFirebaseDatabaseReference;
    private EditText mMessageEdit;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private String mUsername;
    private String mPhotoUrl;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView nameTextView; //사용자 이름
        TextView messageTextView; //메세지 내용
        CircleImageView photoImageView; //프로필사진
        ImageView messageImageView; //메세지 이미지
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.item_message_textview_username);
//            messageImageView = itemView.findViewById(R.id.item_message_imageview);
            messageTextView = itemView.findViewById(R.id.item_message_textview_message);
            photoImageView = itemView.findViewById(R.id.item_message_circleimageview);

        }
    }

    private RecyclerView mMessageRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mMessageEdit = findViewById(R.id.mainactivity_edittext_message);

        mMessageRecyclerView = findViewById(R.id.mainactivity_recyclerview_message);

        findViewById(R.id.mainactivity_button_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatMessage chatMessage = new ChatMessage(mMessageEdit.getText().toString(),
                        mUsername,mPhotoUrl,null);
                mFirebaseDatabaseReference.child(MESSAGE_CHIld)
                        .push().setValue(chatMessage);
                mMessageEdit.setText("");
            }
        });


        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API) //기본값
                .build();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if(mFirebaseUser==null){
            startActivity(new Intent(this, SigninActivity.class));
            finish();
            return;
        }else{
            mUsername = mFirebaseUser.getDisplayName();
            if(mFirebaseUser.getPhotoUrl()!=null){
                mPhotoUrl=mFirebaseUser.getPhotoUrl().toString();
            }
        }
        Query query = mFirebaseDatabaseReference.child(MESSAGE_CHIld);
        FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query,ChatMessage.class)
                .build();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull ChatMessage model) {
                holder.messageTextView.setText(model.getText());
                holder.nameTextView.setText(model.getName());
                if(model.getPhotoUrl()==null){
                    holder.photoImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                            R.drawable.account));
                }else{
                    Glide.with(MainActivity.this)
                            .load(model.getPhotoUrl())
                            .into(holder.photoImageView);

                }
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message,viewGroup,false);
                return new MessageViewHolder(view);
            }
        };
        mMessageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sing_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = "";
                startActivity(new Intent(this,SigninActivity.class));
                        finish();
                return true;

                default:
                    return super.onOptionsItemSelected(item);

        }
    }
}
