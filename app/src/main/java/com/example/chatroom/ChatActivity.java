package com.example.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://polar-coast-14493.herokuapp.com");
        } catch (URISyntaxException e) {}
    }
    String name;
    int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final Intent intent=getIntent();
        name=intent.getStringExtra("name");
        type=intent.getIntExtra("type",-1);

        final EditText editText=findViewById(R.id.message2);
        Button button=findViewById(R.id.send);

        mSocket.connect();

        mSocket.emit("join",name);

        final ArrayList<Message> messageArrayList=new ArrayList<>();
        final RecyclerView recyclerView=findViewById(R.id.messageList);
        final CustomAdapter customAdapter=new CustomAdapter(messageArrayList);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editText.getText().toString().isEmpty())
                {
                    mSocket.emit("messagedetection",name,editText.getText().toString());
                    editText.setText("");
                }
            }
        });

        mSocket.on("userjoinedthechat", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data=(String) args[0];
                        Toast.makeText(ChatActivity.this,data,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mSocket.on("userdisconnect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data=(String) args[0];
                        Toast.makeText(ChatActivity.this,data,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mSocket.on("message", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try
                        {
                            String nickName=data.getString("senderNickname");
                            String msg=data.getString("message");
                            Message m =new Message(nickName,msg);
                            messageArrayList.add(m);
                            customAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });


        Button button1=findViewById(R.id.logout);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }


    private void logout()
    {
       if(type==0)
       {
           GoogleSignInClient mGoogleSignInClient;
           GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                   .requestEmail()
                   .build();

           mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

           mGoogleSignInClient.signOut()
                   .addOnCompleteListener(ChatActivity.this, new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           Intent intent1=new Intent(ChatActivity.this,MainActivity.class);
                           startActivity(intent1);
                           finish();
                       }
                   });
       }
       else if(type==1)
       {
           FacebookSdk.sdkInitialize(getApplicationContext());
           LoginManager.getInstance().logOut();
           AccessToken.setCurrentAccessToken(null);
           Intent intent1=new Intent(ChatActivity.this,MainActivity.class);
           startActivity(intent1);
           finish();
       }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.emit("kill",name);
        mSocket.disconnect();
    }
}
