package com.companyx.android.appx;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.companyx.android.appx.RecipeDatabase.Recipe;
import com.companyx.android.appx.RecipeDatabase.RecipeTime;
import com.companyx.android.appx.RecipeDatabase.ShoppingList;

/**
 * Search/Select Recipe Activity
 * 
 * TODO Add Recipe, custom options menu
 * TODO http://developer.android.com/guide/topics/search/adding-recent-query-suggestions.html
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class SelectRecipeActivity extends BaseListActivity {
	// STATE VARIABLES
	private List<Recipe> recipes;
	private String operation;
	
	// VIEW HOLDERS
	private LinearLayout layoutIngredients;
	
	// SYSTEM
	private RecipeDatabase recipeDatabase;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_recipe);
		
		initialize();
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		// singleTop flag set in manifest; handle when the user searches from this Activity and sends new search Intent to itself without restarting
		setIntent(intent);
		handleIntent(intent);
	}
	
	/**
	 * Perform search if user arrived at this activity via Search, return complete recipe list otherwise.
	 * @param intent the Intent passed to this Activity.
	 */
	private void handleIntent(Intent intent) {
		// receive search action
		String action = intent.getAction();
		
		// other operations
		operation = intent.getStringExtra("operation");
		
		// reset views
		layoutIngredients.removeAllViews();
		
		if (action != null && action.equals(Intent.ACTION_SEARCH)) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			recipes = recipeDatabase.searchRecipes(query);
		} else if (operation != null) {
			if (operation.equals("Favorites"))
				loadFavoriteRecipes();
			else if (operation.equals("Shopping List"))
				loadShoppingListRecipes();
		} else
			loadAllRecipes();
		
		// post results
		setListAdapter(new RecipeListViewAdapter(this, recipes));
	}

	private void initialize() {
		layoutIngredients = (LinearLayout) findViewById(R.id.layout_select_recipe_ingredients);
		
		initializeListView();
		
		recipeDatabase = RecipeDatabase.getInstance();
	}

	private void initializeListView() {
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int recipeId = ((RecipeListViewAdapter.RecipeView) view.getTag()).recipeId;
				
				Intent intent = new Intent(SelectRecipeActivity.this, RecipeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				intent.putExtra("recipeId", recipeId);
				startActivity(intent);
			}
		});
	}
	
	/**
	 * Return all recipes in the RecipeDatabase, sorted by Recipe name.
	 */
	private void loadAllRecipes() {
		recipes = recipeDatabase.allRecipes();
		
		Toast.makeText(getApplicationContext(), getString(R.string.select_recipe_showing) + " " + recipes.size() + " " + getString(R.string.select_recipe_recipes), Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Return favorite recipes from the RecipeDatabase, sorted by Recipe name.
	 */
	private void loadFavoriteRecipes() {
		recipes = recipeDatabase.getFavoriteRecipes();
		
		if (recipes.size() == 0)
			new AlertDialog.Builder(this).setTitle(R.string.select_recipe_favorites_alert_title).setMessage(R.string.select_recipe_favorites_empty).setPositiveButton(R.string.select_recipe_favorites_empty_ok, null).show();
	}
	
	/**
	 * Return favorite recipes from the RecipeDatabase, sorted by Recipe name.
	 * Show list of aggregated recipe ingredients.
	 */
	private void loadShoppingListRecipes() {
		recipes = recipeDatabase.getShoppingListRecipes();
		
		ShoppingList list = recipeDatabase.getShoppingList();
		
		// MEAT
		if (!list.meat.isEmpty()) {
			TextView tv = new TextView(this);
			tv.setText(R.string.select_recipe_meat);
			layoutIngredients.addView(tv);
			addIngredientViews(list.meat, layoutIngredients);
		}
		
		// SEAFOOD
		if (!list.seafood.isEmpty()) {
			TextView tv = new TextView(this);
			tv.setText(R.string.select_recipe_seafood);
			layoutIngredients.addView(tv);
			addIngredientViews(list.seafood, layoutIngredients);
		}
		
		// PRODUCE
		if (!list.produce.isEmpty()) {
			TextView tv = new TextView(this);
			tv.setText(R.string.select_recipe_produce);
			layoutIngredients.addView(tv);
			addIngredientViews(list.produce, layoutIngredients);
		}
		
		// OTHER
		if (!list.other.isEmpty()) {
			TextView tv = new TextView(this);
			tv.setText(R.string.select_recipe_other);
			layoutIngredients.addView(tv);
			addIngredientViews(list.other, layoutIngredients);
		}
		
		if (recipes.size() == 0)
			new AlertDialog.Builder(this).setTitle(R.string.select_recipe_shopping_list_alert_title).setMessage(R.string.select_recipe_shopping_list_empty).setPositiveButton(R.string.select_recipe_shopping_list_empty_ok, null).show();
	}
	
	/**
	 * Helper function which adds child views displaying ingredients to a parent ViewGroup.
	 * @param ingredients the List of Strings representing ingredients.
	 * @param viewGroup the parent ViewGroup to add the child views to.
	 */
	private void addIngredientViews(List<String> ingredients, ViewGroup viewGroup) {
		for (String s : ingredients) {
			TextView tv = new TextView(this);
			tv.setText(s);
			viewGroup.addView(tv);
		}
	}
	
	/**
	 * Custom Recipe list view adapter.
	 */
	private class RecipeListViewAdapter extends ArrayAdapter<Recipe> {
		private final List<Recipe> recipes;
		private final Context activity;
		
		RecipeListViewAdapter(Context activity, List<Recipe> recipes) {
			super(activity, R.layout.recipe_list_item, recipes);
			this.activity = activity;
			this.recipes = recipes;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        RecipeView recipeView = null;
	 
	        if (view == null) {
	        	LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
	            view = inflater.inflate(R.layout.recipe_list_item, null);
	 
	            // hold the view objects in an object, so they don't need to be re-fetched
	            recipeView = new RecipeView();
	            recipeView.textViewName = (TextView) view.findViewById(R.id.recipe_list_name);
	            recipeView.textViewDescription = (TextView) view.findViewById(R.id.recipe_list_description);
	            recipeView.textViewInfoRight = (TextView) view.findViewById(R.id.recipe_list_info_right);
	 
	            // cache the view objects in the tag, so they can be re-accessed later
	            view.setTag(recipeView);
	        } else
	        	recipeView = (RecipeView) view.getTag();
	 
	        // set up view, store unique ID to retrieve recipe from database when selected
	        Recipe recipe = recipes.get(position);
	        int recipeId = recipe.recipeId;
	        
	        recipeView.recipeId = recipeId;
	        recipeView.textViewName.setText(recipe.name);
	        recipeView.textViewDescription.setText("Put something cool here.");
	        if (operation != null && operation.equals("Shopping List"))
	        	recipeView.textViewInfoRight.setText(String.valueOf(recipeDatabase.getQuantity(recipeId)));
	        else
	        	recipeView.textViewInfoRight.setText(getTime(recipe.recipeTime));
	        
	        return view;
	    }
		
		class RecipeView {
			int recipeId;
			TextView textViewName;
			TextView textViewDescription;
			TextView textViewInfoRight;
		}
		
		/**
		 * Helper function that generates a string containing the formatted hour and minute representation of the recipe cooking time.
		 * @param timeRequiredInMin the time required in minutes for the Recipe.
		 * @return a string containing the formatted hour and minute representation of the recipe cooking time.
		 */
		private String getTime(RecipeTime recipeTime) {
			// retrieve total time
			short totalTimeInMin = (short) (recipeTime.prepTimeInMin + recipeTime.inactivePrepTimeInMin + recipeTime.cookTimeInMin);
			
			// construct hours string
			short hours = (short) (totalTimeInMin / 60);
			String hoursStr = "";
			if (hours != 0) {
				if (hours == 1)
					hoursStr += hours + " " + getString(R.string.select_recipe_hour) + " ";
				else
					hoursStr += hours + " " + getString(R.string.select_recipe_hours) + " ";
			}
				
			return hoursStr + (totalTimeInMin % 60) + " " + getString(R.string.select_recipe_min);
		}
	}
}
