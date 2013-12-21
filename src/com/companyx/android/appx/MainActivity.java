package com.companyx.android.appx;

import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;

import com.companyx.android.appx.RecipeDatabase.Recipe;
import com.companyx.android.appx.RecipeDatabase.RecipeIngredient;
import com.companyx.android.appx.RecipeDatabase.RecipeTime;

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

		// LOAD DATA FROM FILE
		InputStream inputStream = getResources().openRawResource(R.raw.master_recipe_data);
		RecipeLoader loader = new RecipeLoader(inputStream, recipeDatabase);
		//loader.loadData();

		loadTestData(recipeDatabase); // TODO REMOVE

		// LOAD FAVORITES
		String serializedFavorites = sharedPref.getString("SERIALIZED_FAVORITES", null);
		recipeDatabase.loadFavoriteRecipes(serializedFavorites);
		
		// LOAD SHOPPING LIST
		String serializedShoppingList = sharedPref.getString("SERIALIZED_SHOPPING_LIST", null);
		recipeDatabase.loadShoppingListRecipes(serializedShoppingList);
	}
	
	/**
	 * REMOVE
	 * @param recipeDatabase
	 */
	private void loadTestData(RecipeDatabase recipeDatabase) {
		List<RecipeIngredient> emptyList = new ArrayList<RecipeIngredient>();
		List<RecipeIngredient> list1 = new ArrayList<RecipeIngredient>();
		RecipeIngredient ri1 = new RecipeIngredient("2 1/2", "pounds", "Roasted Pork", "");
		RecipeIngredient ri2 = new RecipeIngredient("1 1/4", "Pounds", "Huge Duck", "");
		RecipeIngredient ri3 = new RecipeIngredient("2", "pinches", "salt", null);
		RecipeIngredient ri4 = new RecipeIngredient("3", "", "apples", "");
		RecipeIngredient ri5 = new RecipeIngredient("1", null, "bananas", "");
		RecipeIngredient ri6 = new RecipeIngredient("4", "bunches", "celery", "");
		RecipeIngredient ri7 = new RecipeIngredient("4", "", "flour tortillas", "grilled, cut into thin strips");
		RecipeIngredient ri8 = new RecipeIngredient("1/4", "lbs.", "tuna", "");
		list1.add(ri1);
		list1.add(ri2);
		list1.add(ri3);
		list1.add(ri4);
		list1.add(ri5);
		list1.add(ri6);
		list1.add(ri7);
		list1.add(ri8);
		RecipeTime time1 = new RecipeTime((short) 30, (short) 30, (short) 24);
		
		recipeDatabase.addRecipe(new Recipe(321001, "Curry Pie", emptyList, null, time1, (byte) 1, (byte) 2));
		recipeDatabase.addRecipe(new Recipe(321002, "Curry Pork 2", emptyList, null, time1, (byte) 4, (byte) 3));
		recipeDatabase.addRecipe(new Recipe(321003, "Baked Salmon", emptyList, null, time1, (byte) 4, (byte) 4));
		recipeDatabase.addRecipe(new Recipe(321004, "Apple Pie", emptyList, null, time1, (byte) 4, (byte) 1));
		recipeDatabase.addRecipe(new Recipe(321005, "Pulled Pork Sandwich", list1, null, time1, (byte) 4, (byte) 25));
		recipeDatabase.addRecipe(new Recipe(321006, "Curry Pork 1", emptyList, null, time1, (byte) 4, (byte) 32));
		recipeDatabase.addRecipe(new Recipe(321007, "Curry Pork 2", emptyList, null, time1, (byte) 4, (byte) 43));
		recipeDatabase.addRecipe(new Recipe(321008, "Mystery Sandwich", list1, null, time1, (byte) 4, (byte) 120));
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
