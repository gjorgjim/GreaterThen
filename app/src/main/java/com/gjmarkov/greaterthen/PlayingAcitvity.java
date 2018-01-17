package com.gjmarkov.greaterthen;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class PlayingAcitvity extends AppCompatActivity {

  private TextView number;
  private Button randomNumber;

  private FirebaseDatabase database;
  private FirebaseService firebaseService;

  private int myNumber = 0;
  private int otherNumber = 0;

  private ValueEventListener numberListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_playing_acitvity);

    number = (TextView) findViewById(R.id.number);
    randomNumber = (Button) findViewById(R.id.randomNumber);

    database = FirebaseDatabase.getInstance();
    firebaseService = FirebaseService.getInstance();

    listenForOtherPlayer();

    randomNumber.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Random r = new Random();
        myNumber = r.nextInt(10000) + 1;
        number.setText(myNumber + "");
        DatabaseReference gameRef = database.getReference("games").child(firebaseService.getGame().getKey());
        gameRef.child(firebaseService.getRole()).child("number").setValue(myNumber);
        if(otherNumber != 0 && myNumber != 0) {
          theWinnerIs();
        }
      }
    });
  }
  private void listenForOtherPlayer() {
    numberListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        otherNumber = dataSnapshot.getValue(Integer.class);
        if(myNumber != 0 && otherNumber != 0) {
          theWinnerIs();
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    };

    DatabaseReference gameRef = database.getReference("games").child(firebaseService.getGame().getKey());
    if(firebaseService.getRole().equals(FirebaseService.CREATOR)) {
      gameRef.child(FirebaseService.PLAYER).child("number").addValueEventListener(numberListener);
    } else {
      gameRef.child(FirebaseService.CREATOR).child("number").addValueEventListener(numberListener);
    }

  }

  private void theWinnerIs() {
    removeListeners();
    String message = "";
    if(myNumber > otherNumber) {
      message = "YOU WON!";
    } else {
      message = "YOU LOST!";
    }
    AlertDialog.Builder builder = new AlertDialog.Builder(PlayingAcitvity.this)
      .setTitle("RESULT:")
      .setMessage(message)
      .setPositiveButton("NEW GAME", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          removeActiveGame();
          restartGame();
          startActivity(new Intent(PlayingAcitvity.this, GameActivity.class));
        }
      });

    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }

  private void restartGame() {
    firebaseService.setRole(null);
    firebaseService.setGame(null);
  }

  private void removeListeners() {
    DatabaseReference gameRef = database.getReference("games").child(firebaseService.getGame().getKey());

    if(firebaseService.getRole().equals(FirebaseService.CREATOR)) {
      gameRef.child(FirebaseService.PLAYER).child("number").removeEventListener(numberListener);
    } else {
      gameRef.child(FirebaseService.CREATOR).child("number").removeEventListener(numberListener);
    }
  }

  private void removeActiveGame() {
    if(firebaseService.getRole().equals(FirebaseService.CREATOR))
    {
      DatabaseReference gameRef = database.getReference("games").child(firebaseService.getGame().getKey());
      gameRef.removeValue();
    }
  }
}
