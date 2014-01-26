package com.companyx.android.cookingxp;

import java.io.InputStream;

import android.content.Intent;
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
	// SYSTEM
	RecipeDatabase recipeDatabase;
	GameData gameData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		loadDatabase();
		initialize();
	}
	
	private void initialize() {
		gameData = GameData.getInstance(this);
		gameData.validate();
		
		LinearLayout layoutMain = (LinearLayout) findViewById(R.id.layout_main);
		
		TextView tvWelcome = new TextView(this);
		tvWelcome.setText(R.string.welcome);
		tvWelcome.setTextSize(16 + 0.5f);
		layoutMain.addView(tvWelcome);
		
		Button buttonReset = new Button(this);
		buttonReset.setText("Reset Game Data");
		buttonReset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				gameData.clearGameData();
			}	
		});
		layoutMain.addView(buttonReset);
	}
	
	/**
	 * Bulk recipe data loading, at the start of the app.
	 */
	private void loadDatabase() {
		recipeDatabase = RecipeDatabase.getInstance(this);
		recipeDatabase.resetDatabase(); // in case singleton RecipeDatabase was not destroyed (i.e. exit/re-enter app quickly)

		// LOAD RECIPES FROM FILE
		InputStream inputStream = getResources().openRawResource(R.raw.master_recipe_data);
		RecipeLoader loader = new RecipeLoader(inputStream, recipeDatabase);
		loader.loadData();
		
		// LOAD FAVORITES
		recipeDatabase.loadFavoriteRecipes();
		
		// LOAD SHOPPING LIST
		recipeDatabase.loadShoppingListRecipes();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		// QUIT
		finish();
	}
}
