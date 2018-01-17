package com.gjmarkov.greaterthen;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements JoinGame {

  public static final String TAG = "GreaterThan";

  private Button signOut;
  private Button newGame;

  private FirebaseDatabase database;
  private FirebaseService firebaseService;

  private List<Game> games;

  private ListView activeGames;

  private ActiveGamesAdapter adapter;

  private String nextKey;

  private ValueEventListener newGameJoinedListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);

    signOut = (Button) findViewById(R.id.logOutBtn);
    newGame = (Button) findViewById(R.id.newGameBtn);
    activeGames = (ListView) findViewById(R.id.activeGames);

    firebaseService = FirebaseService.getInstance();
    database = FirebaseDatabase.getInstance();

    games = new ArrayList<>();
    adapter = new ActiveGamesAdapter(this, R.layout.active_games_layout, games);
    activeGames.setAdapter(adapter);

    listenForNewGames();
    listenForActiveGames();

    signOut.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AuthUI.getInstance()
          .signOut(GameActivity.this)
          .addOnCompleteListener(new OnCompleteListener<Void>() {
            public void onComplete(@NonNull Task<Void> task) {
              startActivity(new Intent(GameActivity.this, MainActivity.class));
            }
          });

      }
    });


    newGame.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
          if(firebaseService.getGame() == null ) {
            DatabaseReference newGameRef = database.getReference("games").child(nextKey);

            newGameRef.child("creator").child("uid").setValue(firebaseService.getUser().getUid());
            newGameRef.child("status").setValue(Game.STATUS_WAITING);
            newGameRef.child("creator").child("number").setValue(0);

            firebaseService.setGame(new Game(nextKey));

            DatabaseReference gamesNumberRef = database.getReference("gamesNumber");
            gamesNumberRef.setValue(Integer.parseInt(nextKey) + 1);

            newGameJoinedListener = new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Status changed");
                if(dataSnapshot.getValue(Integer.class) == Game.STATUS_READY) {
                  removeListener();
                  firebaseService.setRole(FirebaseService.CREATOR);
                  startActivity(new Intent(GameActivity.this, PlayingAcitvity.class));
                }
              }
              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
            };
            newGameRef.child("status").addValueEventListener(newGameJoinedListener);
          } else {
            Toast.makeText(GameActivity.this, "You have alredy created a game!", Toast.LENGTH_SHORT).show();
          }
      }
    });

  }

  private void listenForNewGames() {
    DatabaseReference gamesNumberRef = database.getReference("gamesNumber");

    gamesNumberRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        nextKey = dataSnapshot.getValue() + "";
        Log.d(TAG, nextKey);
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
  }

  private void listenForActiveGames() {
    DatabaseReference gamesRef = database.getReference("games");

    gamesRef.orderByChild("status").equalTo(Game.STATUS_WAITING).limitToFirst(10).addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, "onChildAdded Called");
        Log.d(TAG, "Key: " + dataSnapshot.getKey());
        if(firebaseService.getGame()!=null) {
          if(!firebaseService.getGame().getKey().equals(dataSnapshot.getKey())) {
            games.add(new Game(dataSnapshot.getKey()));
            adapter.notifyDataSetChanged();
          }
        } else {
          games.add(new Game(dataSnapshot.getKey()));
          adapter.notifyDataSetChanged();
        }
      }

      @Override
      public void onChildChanged(DataSnapshot dataSnapshot, String s) {

      }

      @Override
      public void onChildRemoved(DataSnapshot dataSnapshot) {
        games.remove(new Game(dataSnapshot.getKey()));
        adapter.notifyDataSetChanged();
      }

      @Override
      public void onChildMoved(DataSnapshot dataSnapshot, String s) {

      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
  }

  @Override
  public void joinGame(final String key) {
    DatabaseReference gameRef = database.getReference("games").child(key);

    gameRef.runTransaction(new Transaction.Handler() {
      @Override
      public Transaction.Result doTransaction(MutableData mutableData) {
        Log.d(TAG, "doTransaction in join called");
        if(mutableData.child("player").getValue() == null) {
          mutableData.child("player").child("uid").setValue(firebaseService.getUser().getUid());
          mutableData.child("status").setValue(Game.STATUS_READY);
          mutableData.child("player").child("number").setValue(0);
          firebaseService.setGame(new Game(key));

          return Transaction.success(mutableData);
        }

        return null;
      }

      @Override
      public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

      }
    });

    gameRef.child("status").addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot.getValue(Integer.class) != null) {
          if(dataSnapshot.getValue(Integer.class) == Game.STATUS_READY) {
            firebaseService.setRole(FirebaseService.PLAYER);
            startActivity(new Intent(GameActivity.this, PlayingAcitvity.class));
          }
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
    //startActivity(new Intent(GameActivity.this, PlayingAcitvity.class));
  }

  private void removeListener() {
    DatabaseReference newGameRef = database.getReference("games").child(firebaseService.getGame().getKey());
    newGameRef.child("status").removeEventListener(newGameJoinedListener);
  }
}
