package com.example.myotp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myotp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    Button btnPhone,btnVerify;
    EditText tvPhone,tvCode;
    TextView tvResend;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;

    private static final String TAG="MAIN_TAG";

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        btnPhone=findViewById(R.id.btn);
        btnVerify=findViewById(R.id.verfify);
        tvPhone=findViewById(R.id.number);
        tvCode=findViewById(R.id.otp);
        tvResend=findViewById(R.id.resend);

            firebaseAuth=FirebaseAuth.getInstance();

            mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    signInWithAuthCredential(phoneAuthCredential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {

                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(verificationId, token);
                    Log.d(TAG,"onCodeSent: "+verificationId);

                    mVerificationId=verificationId;
                    forceResendingToken=token;

                    Toast.makeText(MainActivity.this, "Verification code sent", Toast.LENGTH_SHORT).show();


                }
            };

            btnPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phone=tvPhone.getText().toString().trim();
                    if(TextUtils.isEmpty(phone)){
                        Toast.makeText(MainActivity.this,"Please Enter a number",Toast.LENGTH_LONG);
                    }
                    else {
                        startPhoneNumberVerification(phone);
                    }
                }
            });

            tvResend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phone=tvPhone.getText().toString().trim();
                    if(TextUtils.isEmpty(phone)){
                        Toast.makeText(MainActivity.this,"Please Enter a number",Toast.LENGTH_LONG);
                    }
                    else {
                        resendVerificationCode(phone,forceResendingToken);
                    }
                }
            });

            btnVerify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String code=tvCode.getText().toString().trim();
                    if(TextUtils.isEmpty(code)){
                        Toast.makeText(MainActivity.this,"Please Enter a code",Toast.LENGTH_LONG);
                    }
                    else {
                        verifyPhoneNumberWithCode(mVerificationId,code);
                    }
                }
            });
    }

    private void startPhoneNumberVerification(String phone) {

        PhoneAuthOptions options=
                PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L,TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String phone,PhoneAuthProvider.ForceResendingToken token ) {


        PhoneAuthOptions options=
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L,TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {


        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId,code);
        signInWithAuthCredential(credential);
    }

    private void signInWithAuthCredential(PhoneAuthCredential credential) {


        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        String phone=firebaseAuth.getCurrentUser().getPhoneNumber();
                        Toast.makeText(MainActivity.this, "Logged in as "+phone, Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(MainActivity.this,Profile.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}