package com.example.chatroom;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN=1;
    private static final String EMAIL = "email";
    private static final String USER_POSTS = "user_posts";
    private static final String AUTH_TYPE = "rerequest";

    private CallbackManager mCallbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        login();
    }

    private void login()
    {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if(account!=null)
        {
            startChatActivity_google(account);
        }

        else if(isLoggedIn)
        {
            startChatActivity_facebook(AccessToken.getCurrentAccessToken());
        }

        else
        {
            setContentView(R.layout.activity_main);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            signInButton=findViewById(R.id.google_signin);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });

            mCallbackManager = CallbackManager.Factory.create();

            LoginButton mLoginButton = findViewById(R.id.facebook_signin);
            mLoginButton.setPermissions(Arrays.asList(EMAIL, USER_POSTS));

            mLoginButton.setAuthType(AUTH_TYPE);
            mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    startChatActivity_facebook(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Toast.makeText(getApplicationContext(),"Login Failed!",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException e) {
                    Log.e(TAG, "onError: "+e.getMessage());
                }
            });
        }

    }

    private void startChatActivity_facebook(AccessToken accessToken)
    {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            String name=response.getJSONObject().getString("first_name");
                            Intent intent=new Intent(MainActivity.this,ChatActivity.class);
                            intent.putExtra("name",name);
                            intent.putExtra("type",1);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void startChatActivity_google(GoogleSignInAccount account)
    {
        Intent intent=new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra("name",account.getGivenName());
        intent.putExtra("type",0);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            startChatActivity_google(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this,"Login Failed!",Toast.LENGTH_SHORT).show();
        }
    }
}
