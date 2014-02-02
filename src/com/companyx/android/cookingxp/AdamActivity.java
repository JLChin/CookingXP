package com.companyx.android.cookingxp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * AdamActivity
 * 
 * Experimental area for Adam
 */
public class AdamActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adam);
		
		initialize();
	}
	
	private void initialize() {
		LinearLayout layoutInfo = (LinearLayout) findViewById(R.id.layout_adam);
		layoutInfo.setBackgroundResource(R.drawable.box_background_dark);
		
		TextView tvInfo = new TextView(this);
		tvInfo.setText("Recipes unlocked: " + recipeDatabase.allRecipes().size());
		tvInfo.setTextColor(Color.WHITE);
		tvInfo.setTextSize(16 + 0.5f);
		layoutInfo.addView(tvInfo);
		
		TextView tvRank = new TextView(this);
		tvRank.setText("Rank: " + gameData.getRank());
		tvRank.setTextColor(Color.LTGRAY);
		layoutInfo.addView(tvRank);
		
		TextView tvScore = new TextView(this);
		tvScore.setText("Current score: " + gameData.getScore());
		tvScore.setTextColor(Color.LTGRAY);
		layoutInfo.addView(tvScore);
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
}