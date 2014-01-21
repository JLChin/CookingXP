package com.companyx.android.cookingxp;

import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * MainActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class MainActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		loadDatabase();
		testRandomStuff();
	}
	
	private void testRandomStuff() {
		RelativeLayout layoutMain = (RelativeLayout) findViewById(R.id.layout_main);
		
		TextView tvWelcome = new TextView(this);
		tvWelcome.setText(R.string.welcome);
		tvWelcome.setTextSize(16 + 0.5f);
		
		layoutMain.addView(tvWelcome);
	}
	
	/**
	 * Bulk recipe data loading, at the start of the app.
	 */
	private void loadDatabase() {
		RecipeDatabase recipeDatabase = RecipeDatabase.getInstance(this);
		recipeDatabase.resetDatabase(); // in case singleton RecipeDatabase was not destroyed (i.e. exit/re-enter app quickly)
		
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

		// LOAD RECIPES FROM FILE
		InputStream inputStream = getResources().openRawResource(R.raw.master_recipe_data);
		RecipeLoader loader = new RecipeLoader(inputStream, recipeDatabase);
		loader.loadData();

		// LOAD FAVORITES
		String serializedFavorites = sharedPref.getString("SERIALIZED_FAVORITES", null);
		recipeDatabase.loadFavoriteRecipes(serializedFavorites);
		
		// LOAD SHOPPING LIST
		String serializedShoppingList = sharedPref.getString("SERIALIZED_SHOPPING_LIST", null);
		recipeDatabase.loadShoppingListRecipes(serializedShoppingList);
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
