package com.companyx.android.appx;

import com.companyx.android.appx.RecipeDatabase.Recipe;

import android.os.Bundle;
import android.view.WindowManager;

public class RecipeActivity extends BaseActivity {
	// STATE VARIABLES
	Recipe recipe;
	
	// SYSTEM
	RecipeDatabase recipeDatabase;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		initialize();
	}
	
	private void initialize() {
		int recipeID = getIntent().getIntExtra("recipeID", -1);
		//recipe = 
	}
}
