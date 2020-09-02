package com.example.messengerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button continueAndNextButton;
    private String checker = "",phoneNumber = "";
    private RelativeLayout relativeLayout;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;
    private String VarificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;
    private EditText editText;
    private String userName;
    private DatabaseReference refUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);



        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueAndNextButton =findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        ccp = (CountryCodePicker)findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);

        editText = findViewById(R.id.username_phone);




        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);



        continueAndNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(continueAndNextButton.getText().equals("Submit") || checker.equals("Code Sent")){

                    String varificationCode = codeText.getText().toString();
                    if(varificationCode.equals("")){
                        Toast.makeText(PhoneAuthActivity.this, "Please write verification code first", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("Please wait , while we are verifying your code.");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VarificationId,varificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
                else{
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if(!phoneNumber.equals("") && editText.getText().toString().length()>0){
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please wait , while we are verifying your phone number.");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();


                        PhoneAuthProvider.getInstance().verifyPhoneNumber( phoneNumber,60, TimeUnit.SECONDS,PhoneAuthActivity.this,callbacks);
                    }
                    else{
                        Toast.makeText(PhoneAuthActivity.this, "Please write valid phone number & User name first", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                if(isNetworkConnected() == false){
                    Toast.makeText(PhoneAuthActivity.this, "Internet Connection Required ", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    relativeLayout.setVisibility(View.VISIBLE);
                    continueAndNextButton.setText("Continue");
                    codeText.setVisibility(View.GONE);
                    editText.setVisibility(View.VISIBLE);
                }
                else{

                    Toast.makeText(PhoneAuthActivity.this, "Invalid Phone Number...", Toast.LENGTH_SHORT).show();

                    loadingBar.dismiss();
                    relativeLayout.setVisibility(View.VISIBLE);
                    continueAndNextButton.setText("Continue");
                    codeText.setVisibility(View.GONE);
                    editText.setVisibility(View.VISIBLE);}
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                VarificationId=s;
                mResendToken=forceResendingToken;

                relativeLayout.setVisibility(View.GONE);
                editText.setVisibility(View.GONE);

                checker="Code Sent";
                continueAndNextButton.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(PhoneAuthActivity.this, "Code has been sent to your phone, please check.", Toast.LENGTH_SHORT).show();
            }
        };

    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            Intent homeintent = new Intent(PhoneAuthActivity.this, MainActivity.class);
            startActivity(homeintent);
            finish();
        }
    }

    private  void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    userName = editText.getText().toString();

                   refUsers = FirebaseDatabase.getInstance().getReference().child("Users");

                    HashMap<String,Object> userHashmap = new HashMap<>();
                    userHashmap.put("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                    userHashmap.put("username",userName);
                    userHashmap.put("profile","https://firebasestorage.googleapis.com/v0/b/messenger-app-9209b.appspot.com/o/profile.png?alt=media&token=9e3d4057-f6b9-4671-b61f-120352b4a873");
                    userHashmap.put("cover","https://firebasestorage.googleapis.com/v0/b/messenger-app-9209b.appspot.com/o/cover.jpg?alt=media&token=dcdd1ed0-77ad-4504-8d5a-d5250079c751");
                    userHashmap.put("status","offline");
                    userHashmap.put("search",userName.toLowerCase());

                    refUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(userHashmap);

                    loadingBar.dismiss();
                    Toast.makeText(PhoneAuthActivity.this, "Congratulations, your are logged in successfully", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                }
                else{
                    loadingBar.dismiss();
                    String e = task.getException().toString();
                    Toast.makeText(PhoneAuthActivity.this, "Error :- " + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void sendUserToMainActivity(){
        Intent intent = new Intent(PhoneAuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
