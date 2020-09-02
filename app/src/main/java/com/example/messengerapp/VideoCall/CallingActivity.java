package com.example.messengerapp.VideoCall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.messengerapp.MainActivity;
import com.example.messengerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CallingActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_Key = "46729482";  // Vontage API Key
    private static String Session_id = "2_MX40NjcyOTQ4Mn5-MTU4OTEyMzE4NDM2Nn4zTEtiNVVzRVNidDZZZnRuK1lBMmZUNUp-fg";  //Vontage video call session id
    private static String Token = "T1==cGFydG5lcl9pZD00NjcyOTQ4MiZzaWc9MGIwOWE2MjEyOThmOTQ4ZjQyOTBkYzFjODY5OTkyYmI4ZDJkMzkzNDpzZXNzaW9uX2lkPTJfTVg0ME5qY3lPVFE0TW41LU1UVTRPVEV5TXpFNE5ETTJObjR6VEV0aU5WVnpSVk5pZERaWlpuUnVLMWxCTW1aVU5VcC1mZyZjcmVhdGVfdGltZT0xNTg5MTIzMjI3Jm5vbmNlPTAuNDI4NTM3MjYzMTg0Njk2ODcmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU5MTcxNTIyNSZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";  // Streaming access token
    private static final String Log_tag = CallingActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERMISSION = 124;

    private FrameLayout mPublisherViewControler;
    private FrameLayout mSubscriberViewControler;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;


    private ImageView closeVideoChatBtn;
    private DatabaseReference  usersRef;
    private String userId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        closeVideoChatBtn =findViewById(R.id.close_video_chat_btn);

        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(userId).hasChild("Ringing")){
                            usersRef.child(userId).child("Ringing").removeValue();

                            if(mPublisher != null){
                                mPublisher.destroy();
                            }
                            if(mSubscriber != null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(CallingActivity.this,MainActivity.class));
                            finish();
                        }
                        if(dataSnapshot.child(userId).hasChild("Calling")){
                            usersRef.child(userId).child("Calling").removeValue();

                            if(mPublisher != null){
                                mPublisher.destroy();
                            }
                            if(mSubscriber != null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(CallingActivity.this,MainActivity.class));
                            finish();
                        }
                        else{

                            if(mPublisher != null){
                                mPublisher.destroy();
                            }
                            if(mSubscriber != null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(CallingActivity.this, MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, CallingActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERMISSION)
    private  void requestPermissions(){
        String[] perms = {Manifest.permission.INTERNET,Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA};

        if(EasyPermissions.hasPermissions(this, perms)){

            mPublisherViewControler = findViewById(R.id.publisher_container);
            mSubscriberViewControler = findViewById(R.id.subscriber_container);

            //initialize and connect to the session

            mSession = new Session.Builder(this, API_Key, Session_id).build();
            mSession.setSessionListener(CallingActivity.this);
            mSession.connect(Token);

        }
        else{
            EasyPermissions.requestPermissions(this, "Hey this app needs Audio & Camera, Please allow.",RC_VIDEO_APP_PERMISSION,perms);

        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }


    //To Publishing the stream to the session
    @Override
    public void onConnected(Session session) {

        Log.i(Log_tag, "Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(CallingActivity.this);

        mPublisherViewControler.addView(mPublisher.getView());
        if(mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {

        Log.i(Log_tag, "Stream Disconnected");
    }



    // Subscribing to the streams
    @Override
    public void onStreamReceived(Session session, Stream stream) {

        Log.i(Log_tag, "Stream Received");

        if(mSubscriber == null){
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewControler.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        Log.i(Log_tag, "Stream Dropped");
        if(mSubscriber != null){
            mSubscriber = null;
            mSubscriberViewControler.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

        Log.i(Log_tag, "Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
