package com.kevinkarma.park;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final  int ERROR_DIALOG_REQUEST = 9001;
    private FirebaseAuth mAuth;
    private EditText email, password;
    private Button signup, signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_main);

        email = findViewById(R.id.emailView);
        password = findViewById(R.id.passwordView);
        signup = findViewById(R.id.signupBtn);
        signin = findViewById(R.id.signinBtn);

        if(isServicesOK()){
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser();
            }
        });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(mAuth.getCurrentUser() != null){
            //handle the already login user
        }
    }

    private void signInUser(){
        final String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this, "Error SignIn", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUser(){
        final String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();

        if(Email.isEmpty()){
            email.setError("email required");
            email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            email.setError("Enter a valid email");
            email.requestFocus();
            return;
        }

        if(Password.isEmpty()){
            password.setError("Password error");
            password.requestFocus();
            return;
        }

        if(Password.length() < 6 ){
            password.setError("Passward must be atleast 6 character long");
            password.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    User user = new User(Email);

                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "User register successfully.....", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MainActivity.this, "ERROR registering user", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else {
                    if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already Registered", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


        public boolean isServicesOK(){
            Log.d(TAG, "isServicesOK: Checking google services version");

            int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

            if(available == ConnectionResult.SUCCESS){
                Log.d(TAG, "isServicesOK: Google Play Services is working");
                return  true;
            }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
                Log.d(TAG, "isServicesOK: an error occured but we can fix it");
                Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
                dialog.show();
            }else {
                Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

}
