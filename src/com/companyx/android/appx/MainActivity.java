package com.companyx.android.appx;

import java.util.ArrayList;
import java.util.List;

import com.companyx.android.appx.RecipeDatabase.Recipe;
import com.companyx.android.appx.RecipeDatabase.RecipeIngredient;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initialize();
	}
	
	/**
	 * TODO do bulk recipe data loading here, at the start of the app. Create a separate thread to manage import if it takes longer than one or two seconds.
	 */
	private void initialize() {
		RecipeDatabase db = RecipeDatabase.getInstance();
		
		// TEST, GET RID OF THIS
		RecipeIngredient ri1 = new RecipeIngredient((float) 2.5, "pounds", "Roasted Pork");
		List<RecipeIngredient> emptyList = new ArrayList<RecipeIngredient>();
		List<RecipeIngredient> list1 = new ArrayList<RecipeIngredient>();
		list1.add(ri1);
		
		db.addRecipe(new Recipe("Curry Pie", emptyList, null));
		db.addRecipe(new Recipe("Curry Pork 2", emptyList, null));
		db.addRecipe(new Recipe("Baked Salmon", emptyList, null));
		db.addRecipe(new Recipe("Apple Pie", emptyList, null));
		db.addRecipe(new Recipe("Pulled Pork Sandwich", list1, null));
		db.addRecipe(new Recipe("Curry Pork 1", emptyList, null));
		db.addRecipe(new Recipe("Curry Pork 2", emptyList, null));
		db.addRecipe(new Recipe("Mystery Sandwich", list1, null));
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
