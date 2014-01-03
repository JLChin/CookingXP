package com.companyx.android.appx;

import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

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
	}
	
	/**
	 * Bulk recipe data loading, at the start of the app.
	 * TODO Create a separate thread to manage import if it takes longer than one or two seconds.
	 */
	private void loadDatabase() {
		RecipeDatabase recipeDatabase = RecipeDatabase.getInstance(this);
		recipeDatabase.resetDatabase(); // in case singleton RecipeDatabase was not destroyed (i.e. exit/re-enter app quickly)
		
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

		// LOAD DATA FROM FILE
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
