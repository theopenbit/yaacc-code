package de.yaacc.musicplayer;

import de.yaacc.R;
import de.yaacc.R.layout;
import de.yaacc.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class MusicPlayerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_player);
		// initialize buttons
		ImageButton btnPrev = (ImageButton) findViewById(R.id.musicActivityControlPrev);
		btnPrev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// FIXME: uClient.playbackPrev();

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_music_player, menu);

		return true;
	}

}
