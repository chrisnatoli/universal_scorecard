package com.latticelabs.universalscorecard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.method.DigitsKeyListener;
import android.os.Bundle;
import android.R.drawable;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Scorecard extends Activity implements OnClickListener, OnLongClickListener {
    public static final String FILENAME = "tmp";
    private final String TAG = "Scorecard";

    private int startingScore;
    private int round;
    private int numPlayers;
    private int digitsInNumPlayers;
    private int playersComplete;
    private String[] players;
    private int[] scores;
    private ArrayList<Integer>[] addends;

    private TableLayout nameTable;
    private TableRow nameTableScoreRow;
    private TableLayout scoreTable;
    private ArrayList<TableRow> scoreRows;
    private ArrayList<TextView>[] subtotals;
    private LinearLayout roundsColumn;
    private TextView roundsColumnDummy;
    private ScrollView vertScroller;

    private String storedAddendText;
    private int storedRow;
    private int storedCol;

    @Override
	@SuppressWarnings({"unchecked"})
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scorecard);

	// register Views and ViewGroups	
	nameTable = (TableLayout) findViewById(R.id.name_table);
	scoreTable = (TableLayout) findViewById(R.id.score_table);
	vertScroller = (ScrollView) findViewById(R.id.vert_scroller);
	roundsColumn = (LinearLayout) findViewById(R.id.rounds_column);
	roundsColumnDummy = (TextView) findViewById(R.id.rounds_column_dummy);
	nameTableScoreRow = (TableRow) findViewById(R.id.name_table_score_row);
    }
    @Override
	public void onResume() {
	super.onResume();

	// return state-recording ints to 0
	round = 0;
	playersComplete = 0;

	// read data from storage file
	try {
	    FileInputStream fis = openFileInput(FILENAME);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    startingScore = ois.readInt();
	    players = (String[]) ois.readObject();
	    addends = (ArrayList<Integer>[]) ois.readObject();
	    ois.close();
	} catch(IOException ioe) {
	} catch(ClassNotFoundException cnfe) {}

	// determine whether we are creating a new scorecard
	boolean isNewScorecard = false;
	if (addends == null)
	    isNewScorecard = true;

	// pull information from data
	numPlayers = players.length;
	digitsInNumPlayers = 0;
	int k = numPlayers;
	while (k >= 1) {
	    k /= 10;
	    digitsInNumPlayers++;
	}

	// set up arrays
	scores = new int[numPlayers];
	subtotals = new ArrayList[numPlayers];
	for (int i=0; i<numPlayers; i++) {
	    scores[i] = startingScore;
	    subtotals[i] = new ArrayList<TextView>();
	}
	scoreRows = new ArrayList<TableRow>();

	// register some stuff temporarily
	TableRow nameTableRow = (TableRow) findViewById(R.id.name_table_row);
	TableRow scoreTableNameRow = (TableRow) findViewById(R.id.score_table_name_row);

	if (isNewScorecard) {
	    // make a new addends array
	    addends = new ArrayList[numPlayers];
	    for (int i=0; i<numPlayers; i++)
		addends[i] = new ArrayList<Integer>();

	    roundsColumnDummy.setText("("+round+")");
	} else {
	    // clear all contents of rows and all the scores stored in scoreTable
	    nameTableRow.removeAllViews();
	    nameTableScoreRow.removeAllViews();
	    scoreTableNameRow.removeAllViews();
	    scoreTable.removeViews(1, scoreTable.getChildCount()-1);
	    roundsColumn.removeAllViews();
	}
	    
	// add names to nameTableRow
	for (String name : players) {
	    TextView tv = new TextView(this);
	    tv.setText(name);
	    tv.setTextSize(18);
	    tv.setPadding(7,0,7,10);
	    tv.setGravity(Gravity.CENTER);
	    nameTableRow.addView(tv);
	}
	
	// fill nameTableScoreRow with starting scores so that it spaces itself correctly, but set height to 0 so they aren't seen
	for (int i=0; i<numPlayers; i++) {
	    TextView tv = new TextView(this);
	    tv.setText(""+startingScore);
	    tv.setTextSize(18);
	    tv.setPadding(7,0,7,0);
	    tv.setHeight(0);
	    tv.setGravity(Gravity.CENTER);
	    nameTableScoreRow.addView(tv);
	}

	// similarly, add names to scoreTable so that it spaces itself correctly, but set their heights to 0 so that they aren't seen
	for (String name : players) {
	    TextView tv = new TextView(this);
	    tv.setText(name);
	    tv.setTextSize(18);
	    tv.setPadding(7,0,7,0);
	    tv.setHeight(0);
	    tv.setGravity(Gravity.CENTER);
	    scoreTableNameRow.addView(tv);
	}

	// add starting scores to scoreTable
	TableRow subtotals0 = new TableRow(this);
	for (int i=0; i<numPlayers; i++) {
	    TextView tv = new TextView(this);
	    tv.setText(""+startingScore);
	    tv.setTextSize(18);
	    tv.setPadding(7,0,7,5);
	    tv.setGravity(Gravity.CENTER);
	    subtotals0.addView(tv);
	    subtotals[i].add(tv);
	}
	scoreTable.addView(subtotals0);
	scoreRows.add(subtotals0);

	// make first addend row and its addButtons
	TableRow addends0 = new TableRow(this);
	for (int i=0; i<numPlayers; i++) {
	    Button addButton = new Button(this);
	    addButton.setText(getString(R.string.add_button));
	    addButton.setOnClickListener(this);
	    addends0.addView(addButton);
	}
	scoreTable.addView(addends0);
	scoreRows.add(addends0);

	// update roundCount
	TextView roundCount = new TextView(this);
	roundCount.setText("("+round+")");
	roundCount.setTextSize(12);
	roundCount.setPadding(0,4,5,0);
	roundCount.setGravity(Gravity.LEFT);
	roundsColumn.addView(roundCount);

	round++;

	// if it's not a new game, use addends array from file to update the scoreTable
	if (!isNewScorecard) {
	    // find maximum column height
	    int maxHeight = 0;
	    for (ArrayList<Integer> arr : addends)
		if (arr.size() > maxHeight)
		    maxHeight = arr.size();

	    // use addends array from file to update the scoreTable
	    // remove addends as you go along; otherwise you end up with duplicates, since updateScore() adds score to addends array
	    for (int r=0; r<maxHeight; r++)
		for (int c=0; c<numPlayers; c++)
		    if (addends[c].size() > r)
			updateScore(c, addends[c].remove(0));
	}

	// scroll down
	vertScroller.post(new Runnable() {
		@Override
		    public void run() {
		    vertScroller.fullScroll(ScrollView.FOCUS_DOWN);
		}
	    });
    }
    @Override
	public void onPause() {
	super.onPause();

	try {
	    deleteFile(Scorecard.FILENAME);

	    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeInt(startingScore);
	    oos.writeObject(players);
	    oos.writeObject(addends);
	    oos.close();
	} catch(IOException ex) {}
    }

    private void updateScore(int playerIndex, int addend) {
	int row = round*2-1;
	
	// update score internally
	scores[playerIndex] += addend;
	addends[playerIndex].add(addend);
	
	////////////////////////////////////////////////////////////////////
	////////////////////////// addend stuff ////////////////////////////
	////////////////////////////////////////////////////////////////////
	
	// make a TextView for the addend
	String addendText;
	if (addend > 0)
	    addendText = "+"+addend;
	else if (addend < 0)
	    addendText = ""+addend;
	else
	    addendText = ""+'\u2014'; // m-dash
	TextView addendTV = new TextView(this);
	addendTV.setText(addendText);
	addendTV.setTextSize(18);
	addendTV.setPadding(7,5,7,10);
	addendTV.setGravity(Gravity.CENTER);
	addendTV.setOnLongClickListener(this);

	// remove the child occupying the addend's place, and place the addend
	TableRow addendTR = scoreRows.get(row);
	addendTR.removeViewAt(playerIndex);
	addendTR.addView(addendTV, playerIndex);

	// if addendText has more characters than the string in the appropriate cell in nameTableScoreRow, then replace the latter with the former
	TextView nameTableScoreCell = (TextView) nameTableScoreRow.getVirtualChildAt(playerIndex);
	if (nameTableScoreCell.getText().length() < addendText.length())
	    nameTableScoreCell.setText(addendText);


	////////////////////////////////////////////////////////////////////
	////////////////////////// subtotal stuff //////////////////////////
	////////////////////////////////////////////////////////////////////

	// if a row for subtotals doesn't yet exist...
	if (scoreRows.size()<=row+1) {
	    // add a horizontal rule 
	    View hr = new View(this);
	    hr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 2));
	    hr.setBackgroundColor(0xFF909090); // color of text
	    scoreTable.addView(hr);

	    // make a new row for subtotals, add it to table and ArrayList, and fill it with dummy views
	    TableRow subtotalTR = new TableRow(this);
	    scoreTable.addView(subtotalTR);
	    scoreRows.add(subtotalTR);
	    for (int i=0; i<numPlayers; i++) {
		View v = new View(this);
		subtotalTR.addView(v);
	    }

	    // add round to roundsColumn
	    TextView roundCount = new TextView(this);
	    roundCount.setText("("+round+")");
	    roundCount.setTextSize(12);
	    roundCount.setPadding(0,72,5,0);
	    roundCount.setGravity(Gravity.LEFT);
	    roundsColumn.addView(roundCount);

	    roundsColumnDummy.setText("("+round+")");
	}

	TableRow subtotalTR = scoreRows.get(row+1);

	// make a TextView for the new subtotal
	TextView subtotalTV = new TextView(this);
	subtotalTV.setText(""+scores[playerIndex]);
	subtotalTV.setTextSize(18);
	subtotalTV.setPadding(7,10,7,5);
	subtotalTV.setGravity(Gravity.CENTER);
	subtotals[playerIndex].add(subtotalTV);

	// remove the child occupying the new subtotal's place, and place the subtotal
	subtotalTR.removeViewAt(playerIndex);
	subtotalTR.addView(subtotalTV, playerIndex);

	// if player's score has more characters than the string in the appropriate cell in nameTableScoreRow, then replace the latter with the former
	if (nameTableScoreCell.getText().length() < (""+scores[playerIndex]).length())
	    nameTableScoreCell.setText(""+scores[playerIndex]);


	////////////////////////////////////////////////////////////////////
	////////////////////////// round completion stuff //////////////////
	////////////////////////////////////////////////////////////////////

	playersComplete++;
	if (playersComplete == numPlayers) {
	    playersComplete = 0;
	    round++;

	    // create a new addend row, add it to table and list, and fill with Add buttons
	    TableRow addendRow = new TableRow(this);
	    scoreTable.addView(addendRow);
	    scoreRows.add(addendRow);
	    for (int i=0; i<numPlayers; i++) {
		Button addButton = new Button(this);
		addButton.setText(getString(R.string.add_button));
		addButton.setOnClickListener(this);
		addendRow.addView(addButton);
	    }

	    // fix spacing of rounds column
	    roundsColumn.getChildAt(roundsColumn.getChildCount()-1).setPadding(0,63,5,0);
	}
    }

    @Override
	public void onClick(View v) {
	int row = ((TableLayout) v.getParent().getParent()).indexOfChild((TableRow) v.getParent());
	int col = ((TableRow) v.getParent()).indexOfChild(v);

	Dialog dialog = onCreateDialog((col+1)*(-1));
	showDialog((col+1)*(-1));
    }

    @Override
	public boolean onLongClick(View v) {
	// combine the row and col of the view into one int, e.g., if it's row 21 and col 5, they are combined into 215
	int row = ((TableLayout) v.getParent().getParent()).indexOfChild((TableRow) v.getParent());
	int col = ((TableRow) v.getParent()).indexOfChild(v);

	// then pass this combination as the dialog ID
	Dialog dialog = onCreateDialog(row * ((int) Math.pow(10, digitsInNumPlayers)) + col);
	showDialog(row * ((int) Math.pow(10, digitsInNumPlayers)) + col);
	return true;
    }

    @Override
	protected Dialog onCreateDialog(final int id) {
	if (id < 0) {
	    // ADD SCORE DIALOG

	    // unpack col
	    final int col = (id*-1)-1;

	    final EditText et = new EditText(this);
	    et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	    et.setInputType(InputType.TYPE_CLASS_NUMBER);
	    et.setKeyListener(new DigitsKeyListener(true, false));
	    et.setTextSize(20);

	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(getString(R.string.add_score_dialog_title));
	    builder.setPositiveButton(this.getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int which) {
			// pull score from EditText and update appropriate score
			int score;
			if (et.getText().toString().equals("")) // default to 0 if no score is given
			    score = 0;
			else
			    score = Integer.parseInt(et.getText().toString());
			updateScore(col, score);

			// scroll down
			vertScroller.post(new Runnable() {
				@Override
				    public void run() {
				    vertScroller.fullScroll(ScrollView.FOCUS_DOWN);
				}
			    });

			removeDialog(id);
		    }
		});
	    builder.setNeutralButton(this.getString(R.string.dialog_pass_button), new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int which) {
			// update appropriate score with a pass (0)
			updateScore(col, 0);

			// scroll down
			vertScroller.post(new Runnable() {
				@Override
				    public void run() {
				    vertScroller.fullScroll(ScrollView.FOCUS_DOWN);
				}
			    });

			removeDialog(id);
		    }
		});
	    builder.setNegativeButton(this.getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int which) {
			removeDialog(id);
			dialog.cancel();
		    }
		});
	    builder.setView(et);
	    AlertDialog alert = builder.create();
	    return alert;
	} else {
	    // EDIT SCORE DIALOG

	    // unpack row and col from the id
	    final int col = id % ((int) Math.pow(10, digitsInNumPlayers));
	    final int row = (int) (id / ((int) Math.pow(10, digitsInNumPlayers)));

	    // get the text from the addend cell at row,col and store it
	    // (for some reason it needs to be stored during onCreateDialog(), because when showDialog() calls the dialog,
	    //  it uses weird coordinates, namely 0,-1, which throws a NullPointerException)
	    if (row>=0 && col>=0) {
		storedAddendText = ((TextView) ((TableRow) scoreTable.getChildAt(row)).getChildAt(col)).getText().toString();
		if (storedAddendText.charAt(0)=='+')
		    storedAddendText = storedAddendText.substring(1);
		else if (storedAddendText.charAt(0)=='\u2014' || storedAddendText.equals("")) // m-dash
		    storedAddendText = "0";
		storedRow = row;
		storedCol = col;
	    }

	    final EditText et = new EditText(this);
	    et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	    et.setBackgroundResource(drawable.editbox_background);
	    et.setInputType(InputType.TYPE_CLASS_NUMBER);
	    et.setKeyListener(new DigitsKeyListener(true, false));
	    et.setTextSize(20);
	    et.setText(storedAddendText);

	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(getString(R.string.edit_score_dialog_title));
	    builder.setPositiveButton(this.getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int which) {
			int oldAddend = Integer.parseInt(storedAddendText);
			int newAddend = Integer.parseInt(et.getText().toString());
			int difference = newAddend - oldAddend;

			// update addend internally
			addends[storedCol].set((storedRow-2)/3, newAddend);

			// update edited addendText
			String newAddendText;
			if (newAddend > 0)
			    newAddendText = "+"+newAddend;
			else if (newAddend == 0)
			    newAddendText = ""+'\u2014'; // m-dash
			else
			    newAddendText = ""+newAddend;
			((TextView) ((TableRow) scoreTable.getChildAt(storedRow)).getChildAt(storedCol)).setText(newAddendText);

			// add the difference to all subsequence subtotals
			for (int i=(storedRow+1)/3; i<subtotals[storedCol].size(); i++) {
			    int oldScore = Integer.parseInt(subtotals[storedCol].get(i).getText().toString());
			    subtotals[storedCol].get(i).setText(""+(difference+oldScore));
			}

			// if the new addend has more characters than the string in the appropriate cell in nameTableScoreRow,
			// then replace the latter with the former
			TextView nameTableScoreCell = (TextView) nameTableScoreRow.getVirtualChildAt(storedCol);
			if (nameTableScoreCell.getText().length() < newAddendText.length())
			    nameTableScoreCell.setText(newAddendText);
			// otherwise, set it to the longest addend or subtotal in the column
			else {
			    nameTableScoreCell.setText(""+startingScore);
			    for (int i=2; i<=(subtotals[storedCol].size()-1)*3+1; i+=3) {
				TextView addendTV = (TextView) ((TableRow) scoreTable.getChildAt(i)).getChildAt(storedCol);
				if (nameTableScoreCell.getText().toString().length() < addendTV.getText().toString().length())
				    nameTableScoreCell.setText(addendTV.getText());
			    }
			    for (int i=4; i<=(subtotals[storedCol].size()-1)*3+1; i+=3) {
				TextView subtotalTV = (TextView) ((TableRow) scoreTable.getChildAt(i)).getChildAt(storedCol);
				if (nameTableScoreCell.getText().toString().length() < subtotalTV.getText().toString().length())
				    nameTableScoreCell.setText(subtotalTV.getText());
			    }
			    nameTable.requestLayout();
			}

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
	    return alert;
	}
    }
}