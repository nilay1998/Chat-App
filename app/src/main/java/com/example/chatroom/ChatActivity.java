package com.example.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatroom.Retrofit.Chat;
import com.example.chatroom.Retrofit.NetworkClient;
import com.example.chatroom.Retrofit.RequestService;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChatActivity extends AppCompatActivity {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://polar-coast-14493.herokuapp.com");
        } catch (URISyntaxException e) {}
    }
    String name;
    int type;

    Button button;
    EditText editText;
    ArrayList<Chat> arrayList=new ArrayList<>();
    ArrayList<Message> messageArrayList=new ArrayList<>();
    RecyclerView recyclerView;
    CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final Intent intent=getIntent();
        name=intent.getStringExtra("name");
        type=intent.getIntExtra("type",-1);

        initviews();
        show_prev_msg();
        run_socket();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void show_prev_msg()
    {
        Retrofit retrofit = NetworkClient.getRetrofitClient();
        final RequestService requestService=retrofit.create(RequestService.class);
        Call<List<Chat>> call=requestService.requestGet();
        call.enqueue(new Callback<List<Chat>>() {
            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                if(response.body()!=null)
                {
                    arrayList=new ArrayList<>(response.body());
                    for(int i=0;i<arrayList.size();i++)
                    {
                        messageArrayList.add(new Message(arrayList.get(i).getSender(),arrayList.get(i).getMessage()));
                        recyclerView.smoothScrollToPosition(customAdapter.getItemCount());
                        customAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {
                Log.e("ChatActivity", "onFailure: "+t.getMessage());
            }
        });
    }

    private void initviews()
    {
        editText=findViewById(R.id.message2);
        button=findViewById(R.id.send);
        recyclerView=findViewById(R.id.messageList);
        customAdapter=new CustomAdapter(messageArrayList,name);
        recyclerView.setAdapter(customAdapter);
        LinearLayoutManager mLayoutManager=new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(false);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    private void run_socket()
    {
        mSocket.connect();
        mSocket.emit("join",name);

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
                            recyclerView.smoothScrollToPosition(customAdapter.getItemCount());
                            customAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
