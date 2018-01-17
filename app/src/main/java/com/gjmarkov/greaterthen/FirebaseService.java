package com.gjmarkov.greaterthen;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by gjmarkov on 15.01.2018.
 */

public class FirebaseService {

  public static final String CREATOR = "creator";
  public static final String PLAYER = "player";

  private static FirebaseService instance;

  private FirebaseService(){}

  public static FirebaseService getInstance() {
    if(instance == null) {
      instance = new FirebaseService();
    }

    return instance;
  }

  FirebaseUser user = null;

  Game game = null;

  String role = null;

  public FirebaseUser getUser() {
    return user;
  }

  public void setUser(FirebaseUser user) {
    this.user = user;
  }

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
