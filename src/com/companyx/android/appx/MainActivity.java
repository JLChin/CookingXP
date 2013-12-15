package com.companyx.android.appx;

import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;

import com.companyx.android.appx.RecipeDatabase.Recipe;
import com.companyx.android.appx.RecipeDatabase.RecipeIngredient;

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
	 * TODO do bulk recipe data loading here, at the start of the app. Create a separate thread to manage import if it takes longer than one or two seconds.
	 */
	private void loadDatabase() {
		RecipeDatabase recipeDatabase = RecipeDatabase.getInstance();
		recipeDatabase.resetDatabase(); // in case singleton RecipeDatabase was not destroyed (i.e. exit/re-enter app quickly)
		
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		
		// LOAD FAVORITES
		String serializedFavorites = sharedPref.getString("SERIALIZED_FAVORITES", null);
		recipeDatabase.loadFavoriteRecipes(serializedFavorites);
		
		// LOAD SHOPPING LIST
		String serializedShoppingList = sharedPref.getString("SERIALIZED_SHOPPING_LIST", null);
		recipeDatabase.loadShoppingListRecipes(serializedShoppingList);
		
		// LOAD DATA FROM FILE
		InputStream inputStream = getResources().openRawResource(R.raw.master_recipe_data);
		RecipeLoader loader = new RecipeLoader(inputStream, recipeDatabase);
		loader.loadData();
		
		
		// TEST CASES, GET RID OF THIS
		List<RecipeIngredient> emptyList = new ArrayList<RecipeIngredient>();
		List<RecipeIngredient> list1 = new ArrayList<RecipeIngredient>();
		RecipeIngredient ri1 = new RecipeIngredient("2 1/2", "pounds", "Roasted Pork");
		RecipeIngredient ri2 = new RecipeIngredient("1 1/4", "Pounds", "Huge Duck");
		list1.add(ri1);
		list1.add(ri2);
		
		recipeDatabase.addRecipe(new Recipe("Curry Pie", emptyList, null));
		recipeDatabase.addRecipe(new Recipe("Curry Pork 2", emptyList, null));
		recipeDatabase.addRecipe(new Recipe("Baked Salmon", emptyList, null));
		recipeDatabase.addRecipe(new Recipe("Apple Pie", emptyList, null));
		recipeDatabase.addRecipe(new Recipe("Pulled Pork Sandwich", list1, null));
		recipeDatabase.addRecipe(new Recipe("Curry Pork 1", emptyList, null));
		recipeDatabase.addRecipe(new Recipe("Curry Pork 2", emptyList, null));
		recipeDatabase.addRecipe(new Recipe("Mystery Sandwich", list1, null));
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
