package com.latticelabs.universalscorecard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.R.drawable;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class NewScorecard extends Activity implements OnClickListener {
    private final String TAG = "NewScorecard";

    private ArrayList<String> players;
    private EditText startingScore;
    private ArrayAdapter arrayAdapter;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.new_scorecard);

	players = new ArrayList<String>();

	startingScore = (EditText) findViewById(R.id.starting_score);

	arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, players);
	ListView listView = (ListView) findViewById(R.id.player_list);
 	listView.setAdapter(arrayAdapter);
	listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		@Override
		    public void onItemClick(AdapterView parent, View view, int position, long id) {
		    // create an AlertDialog so the user can edit or remove the selected player
		    Dialog dialog = onCreateDialog(position);
		    showDialog(position);
		}
	    });

	View addPlayerButton = findViewById(R.id.add_player_button);
	addPlayerButton.setOnClickListener(this);

	View startScorecardButton = findViewById(R.id.start_scorecard_button);
	startScorecardButton.setOnClickListener(this);
    }

    @Override
	public void onClick(View v) {
	switch (v.getId()) {
	case R.id.add_player_button:
	    Dialog dialog = onCreateDialog(R.id.add_player_button);
	    showDialog(R.id.add_player_button);
	    break;
	case R.id.start_scorecard_button:
	    if (players.isEmpty())
		Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_players_toast), Toast.LENGTH_SHORT).show();
	    else {
		try {
		    deleteFile(Scorecard.FILENAME);

		    FileOutputStream fos = openFileOutput(Scorecard.FILENAME, Context.MODE_PRIVATE);
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
		    String startScoreStr = startingScore.getText().toString();
		    oos.writeInt(startScoreStr.equals("") ? 0 : Integer.parseInt(startScoreStr));
		    oos.writeObject(players.toArray(new String[players.size()]));
		    oos.writeObject(null); // no addends
		    oos.flush();
		    oos.close();
		} catch(IOException ex) {}

		startActivity(new Intent(this, Scorecard.class));
	    }
	    break;
	}
    }

    @Override
	protected Dialog onCreateDialog(final int id) {
	Dialog dialog;
	if (id == R.id.add_player_button) {
	    // ADD PLAYER DIALOG
	    // if onCreatDialog is called by the Add player button, make an AlertDialog so the user can
	    // enter the player's name and then add it to the list of players

	    final EditText et = new EditText(this);
	    et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	    et.setBackgroundResource(drawable.editbox_background);
	    et.setTextSize(20);

	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(this.getString(R.string.add_player_dialog_title));
	    builder.setPositiveButton(this.getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int which) {
			// changes any new lines into spaces
			char[] chars = new char[et.getText().length()];
			et.getText().getChars(0, et.getText().length(), chars, 0);
			for (int i=0; i<chars.length; i++)
			    if (chars[i] == '\n')
				chars[i] = ' ';

			if (String.copyValueOf(chars).trim().equals(""))
			    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.blank_name_toast),
					   Toast.LENGTH_SHORT).show();
			else {
			    players.add(String.copyValueOf(chars).trim());
			    arrayAdapter.notifyDataSetChanged();
			}
			InputMethodManager imm = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
			imm.hideSoftInputFromWindow(et.getWindowToken(),0);
			removeDialog(id);
		    }
		});
	    builder.setNegativeButton(this.getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();
			removeDialog(id);
		    }
		});
	    builder.setView(et);
	    AlertDialog alert = builder.create();
	    dialog = alert;
	} else if (id>=0 && id<players.size()) {
	    // EDIT/REMOVE DIALOG
	    // if onCreateDialog was called by the listView's OnClickListener by using the position of the
	    // clicked item, then make an AlertDialog so the user can choose between editing the player's
	    // name or removing the player from the list

	    final CharSequence[] items = {this.getString(R.string.edit_name_item), this.getString(R.string.remove_player_item)};
	    
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(players.get(id));
	    builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case 0:
			    // create another AlertDialog to edit the name after closing this one
			    dialog.cancel();
			    Dialog d = onCreateDialog((id*-1)-1);
			    showDialog((id*-1)-1);
			    removeDialog(id);
			    break;
			case 1:
			    // remove the player, update view
			    players.remove(id);
			    arrayAdapter.notifyDataSetChanged();
			    dialog.cancel();
			    removeDialog(id);
			    break;
			}
		    }
		});
	    AlertDialog alert = builder.create();
	    dialog = alert;
	} else if (id<0 && id>(players.size()*-1)-1) {
	    // EDIT NAME DIALOG
	    // if onCreateDialog was called by the edit/remove AlertDialog to edit the name of the selected
	    // player, then make an AlertDialog with an EditText displaying the current name for the user
	    // to edit

	    final EditText et = new EditText(this);
	    et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	    et.setBackgroundResource(drawable.editbox_background);
	    et.setTextSize(20);
	    et.setText(players.get((id+1)*-1));

	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(this.getString(R.string.edit_name_item));
	    builder.setPositiveButton(this.getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int which) {
			// changes any new lines into spaces
			char[] chars = new char[et.getText().length()];
			et.getText().getChars(0, et.getText().length(), chars, 0);
			for (int i=0; i<chars.length; i++)
			    if (chars[i] == '\n')
				chars[i] = ' ';

			if (String.copyValueOf(chars).trim().equals(""))
			    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.blank_name_toast),
					   Toast.LENGTH_SHORT).show();
			else {
			    players.set((id+1)*-1,String.copyValueOf(chars).trim());
			    arrayAdapter.notifyDataSetChanged();
			}
			InputMethodManager imm = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
			imm.hideSoftInputFromWindow(et.getWindowToken(),0);
			removeDialog(id);
		    }
		});
	    builder.setNegativeButton(this.getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();
			removeDialog(id);
		    }
		});
	    builder.setView(et);
	    AlertDialog alert = builder.create();
	    dialog = alert;
	} else {
	    dialog = null;
	}
	return dialog;
    }
}
