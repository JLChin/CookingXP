package com.companyx.android.appx;

import com.companyx.android.appx.RecipeDatabase.Recipe;
import com.companyx.android.appx.RecipeDatabase.RecipeDirection;
import com.companyx.android.appx.RecipeDatabase.RecipeIngredient;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

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
	
	// STATE VARIABLES
	private Recipe recipe;
	
	// SYSTEM
	private RecipeDatabase recipeDatabase;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		initialize();
	}
	
	private void initialize() {
		recipeDatabase = RecipeDatabase.getInstance();
		
		int recipeId = getIntent().getIntExtra("recipeId", -1);
		recipe = recipeDatabase.findRecipeById(recipeId);
		
		layoutBody = (LinearLayout) findViewById(R.id.layout_recipe_body);
		textViewName = (TextView) findViewById(R.id.textview_recipe_name);
		
		textViewName.setText(recipe.name);
		
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
