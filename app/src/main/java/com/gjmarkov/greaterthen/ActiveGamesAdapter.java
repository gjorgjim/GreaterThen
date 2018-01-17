package com.gjmarkov.greaterthen;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by gjmarkov on 15.01.2018.
 */

public class ActiveGamesAdapter extends ArrayAdapter<Game> {

  private List<Game> games;
  private JoinGame mContext;

  public ActiveGamesAdapter(@NonNull Context context, int resource) {
    super(context, resource);
  }

  public ActiveGamesAdapter(@NonNull Context context, int resource, List<Game> items) {
    super(context, resource, items);
    this.mContext = (JoinGame) context;
  }

  @NonNull
  @Override
  public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View view = convertView;

    if (view == null) {
      view = LayoutInflater.from(getContext()).inflate(R.layout.active_games_layout, null);
    }

    TextView name = (TextView) view.findViewById(R.id.name);
    Button joinGame = (Button) view.findViewById(R.id.joinGame);

    name.setText(getItem(position).getKey());

    joinGame.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mContext.joinGame(getItem(position).getKey());
      }
    });

    return view;
  }
}
