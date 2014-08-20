package com.latticelabs.universalscorecard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class UniversalScorecard extends Activity implements OnClickListener {
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	View newScorecardButton = findViewById(R.id.new_scorecard_button);
	newScorecardButton.setOnClickListener(this);
	View continueButton = findViewById(R.id.continue_button);
	continueButton.setOnClickListener(this);
    }

    @Override
	public void onClick(View v) {
	switch (v.getId()) {
	case R.id.new_scorecard_button:
	    startActivity(new Intent(this, NewScorecard.class));
	    break;
	case R.id.continue_button:
	    Object addends = null;
	    try {
		FileInputStream fis = openFileInput(Scorecard.FILENAME);
		ObjectInputStream ois = new ObjectInputStream(fis);
		int startingScore = ois.readInt();
		Object players = ois.readObject();
		addends = ois.readObject();
		ois.close();
	    } catch(IOException ioe) {
	    } catch(ClassNotFoundException cnfe) {}

	    if (addends == null) {
		Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_continue_toast), Toast.LENGTH_SHORT).show();
	    }
	    else {
		startActivity(new Intent(this, Scorecard.class));
	    }
	    break;
	}
    }
}
