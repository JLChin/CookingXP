package com.companyx.android.cookingxp;

import java.io.InputStream;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * MainActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class MainActivity extends BaseActivity {
	// VIEW HOLDERS
	private LinearLayout layoutMain;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initialize();
	}
	
	private void initialize() {
		// LOAD RECIPES FROM FILE
		InputStream inputStream = getResources().openRawResource(R.raw.master_recipe_data);
		RecipeLoader loader = new RecipeLoader(inputStream, recipeDatabase);
		loader.loadData();
		
		// LOAD FAVORITES
		recipeDatabase.loadFavoriteRecipes();
		
		// LOAD SHOPPING LIST
		recipeDatabase.loadShoppingListRecipes();
		
		gameData.validate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		recipeDatabase.release();
		gameData.release();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		// QUIT
		finish();
	}
	
	/**
	 * Refresh the screen layout.
	 * This is called onRestart() and handles any game updates since the user left the current Activity.
	 */
	private void refreshLayout() {
		layoutMain = (LinearLayout) findViewById(R.id.layout_main);
		int padding = (int) (dpiScalingFactor * 10 + 0.5f);
		
		// USER INFO BOX
		LinearLayout layoutInfoContainer = new LinearLayout(this);
		layoutInfoContainer.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1.0f));
		
		LinearLayout layoutInfo = new LinearLayout(this);
		layoutInfo.setOrientation(LinearLayout.VERTICAL);
		layoutInfo.setPadding(padding, padding, padding, padding);
		layoutInfo.setBackgroundResource(R.drawable.box_background_dark);
		
		// RANK
		TextView tvRank = new TextView(this);
		tvRank.setTextColor(Color.LTGRAY);
		tvRank.setText(getString(R.string.rank) + ": " + gameData.getRank());
		layoutInfo.addView(tvRank, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		// SCORE
		TextView tvScore = new TextView(this);
		tvScore.setTextColor(Color.LTGRAY);
		tvScore.setText(getString(R.string.score) + ": " + gameData.getScore());
		layoutInfo.addView(tvScore, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		layoutInfoContainer.addView(layoutInfo, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layoutMain.addView(layoutInfoContainer);
		
		// WELCOME
		TextView tvWelcome = new TextView(this);
		tvWelcome.setText(R.string.welcome);
		tvWelcome.setTextSize(16 + 0.5f);
		layoutMain.addView(tvWelcome);
		
		// RESET GAME DATA BUTTON
		Button buttonReset = new Button(this);
		buttonReset.setText(R.string.reset);
		buttonReset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				gameData.clearGameData();
				
				layoutMain.removeAllViews();
				refreshLayout();
			}	
		});
		layoutMain.addView(buttonReset);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		
		layoutMain.removeAllViews();
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		refreshLayout();
	}
}
