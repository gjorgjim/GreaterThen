package com.gjmarkov.greaterthen;

/**
 * Created by gjmarkov on 15.01.2018.
 */

public class Game {

  public static final int STATUS_WAITING = 1;
  public static final int STATUS_READY = 2;
  private String key;

  public Game(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
