package com.gjmarkov.greaterthen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final int RC_SIGN_IN = 123;

  private FirebaseService firebaseService;

  private List<AuthUI.IdpConfig> providers = null;
  private Button loginbtn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    firebaseService = FirebaseService.getInstance();

    providers = Arrays.asList(
      new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
    );

    loginbtn = (Button) findViewById(R.id.loginBtn);

    loginbtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivityForResult(
          AuthUI.getInstance()
          .createSignInIntentBuilder()
          .setAvailableProviders(providers)
          .build(),
          RC_SIGN_IN
        );
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      IdpResponse response = IdpResponse.fromResultIntent(data);

      if (resultCode == RESULT_OK) {
        firebaseService.setUser(FirebaseAuth.getInstance().getCurrentUser());
        startActivity(new Intent(MainActivity.this, GameActivity.class));
      } else {
        // Sign in failed, check response for error code
        // ...
      }
    }

  }
}
