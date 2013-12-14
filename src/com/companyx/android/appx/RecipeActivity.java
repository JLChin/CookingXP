package com.companyx.android.appx;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.companyx.android.appx.RecipeDatabase.Recipe;
import com.companyx.android.appx.RecipeDatabase.RecipeDirection;
import com.companyx.android.appx.RecipeDatabase.RecipeIngredient;

/**
 * Recipe Activity
 * 
 * TODO Add/Remove from Favorites
 * TODO Add/Remove from Shopping List
 * TODO Step-by-Step Walkthrough
 * 
 *  @author James Chin <jameslchin@gmail.com>
 */
public class RecipeActivity extends BaseActivity {
	// VIEW HOLDERS
	private LinearLayout layoutBody;
	private TextView textViewName;
	private ImageButton buttonFavorite;
	private ImageButton buttonShoppingList;
	
	// STATE VARIABLES
	private int recipeId;
	private Recipe recipe;
	
	// SYSTEM
	private RecipeDatabase recipeDatabase;
	private SharedPreferences.Editor sharedPrefEditor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		initialize();
	}
	
	private void initialize() {
		recipeDatabase = RecipeDatabase.getInstance();
		
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		sharedPrefEditor = sharedPref.edit();
		
		recipeId = getIntent().getIntExtra("recipeId", -1);
		recipe = recipeDatabase.findRecipeById(recipeId);
		
		// RECIPE NAME
		textViewName = (TextView) findViewById(R.id.textview_recipe_name);
		textViewName.setText(recipe.name);
		
		// FAVORITE BUTTON
		buttonFavorite = (ImageButton) findViewById(R.id.imagebutton_recipe_favorite);
		if (recipeDatabase.isFavorite(recipeId)) {
			// TODO set favorite background color
		} else {
			// TODO set ordinary background color
		}
		buttonFavorite.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				synchronized(recipeDatabase) {
					if (recipeDatabase.isFavorite(recipeId)) {
						recipeDatabase.removeFavorite(recipeId);
						// set ordinary background color
					} else {
						recipeDatabase.addFavorite(recipeId);
						// set favorite background color
					}
					
					sharedPrefEditor.putString("SERIALIZED_FAVORITES", recipeDatabase.getSerializedFavorites());
					sharedPrefEditor.commit();
				}
			}
		});
		
		// SHOPPING LIST BUTTON
		buttonShoppingList = (ImageButton) findViewById(R.id.imagebutton_recipe_shopping_list);
		
		// TODO
		layoutBody = (LinearLayout) findViewById(R.id.layout_recipe_body);
		
		for (RecipeIngredient ri : recipe.ingredients) {
			TextView tv = new TextView(this);
			String s = ri.amount + " " + ri.measurement + " " + ri.ingredientName;
			tv.setText(s);
			layoutBody.addView(tv);
		}
		
		if (recipe.directions != null) {
			for (RecipeDirection rd : recipe.directions) {
				TextView tv = new TextView(this);
				String s = rd.direction;
				tv.setText(s);
				layoutBody.addView(tv);
			}
		}
	}
}
