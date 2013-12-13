package com.companyx.android.appx;

import java.util.List;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.TextView;

import com.companyx.android.appx.RecipeDatabase.Recipe;

/**
 * Search/Select Recipe Activity
 * 
 * TODO Add Recipe, custom options menu
 * 
 * TODO http://developer.android.com/guide/topics/search/adding-recent-query-suggestions.html
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class SelectRecipeActivity extends BaseListActivity {
	private RecipeDatabase recipeBase;
	private List<Recipe> recipes;
	
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
		String action = intent.getAction();
		
		if (action != null && action.equals(Intent.ACTION_SEARCH)) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			recipes = recipeBase.searchRecipes(query);
		} else
			recipes = recipeBase.getRecipes();
		
		// post results
		setListAdapter(new RecipeListViewAdapter(this, recipes));
	}

	private void initialize() {
		initializeListView();
		
		recipeBase = RecipeDatabase.getInstance();
		
		// TODO
	}

	private void initializeListView() {
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int recipeID = ((RecipeListViewAdapter.RecipeView) view.getTag()).recipeID;
				
				Intent intent = new Intent(SelectRecipeActivity.this, RecipeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.putExtra("recipeID", recipeID);
				startActivity(intent);
			}
		});
	}
	
	/**
	 * Custom Recipe list view adapter.
	 */
	private class RecipeListViewAdapter extends ArrayAdapter<Recipe> {
		private final List<Recipe> recipes;
		private final Context activity;
		
		RecipeListViewAdapter(Context activity, List<Recipe> recipes) {
			super(activity, R.layout.list_item, recipes);
			this.activity = activity;
			this.recipes = recipes;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        RecipeView recipeView = null;
	 
	        if (view == null) {
	        	LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
	            view = inflater.inflate(R.layout.list_item, null);
	 
	            // hold the view objects in an object, so they don't need to be re-fetched
	            recipeView = new RecipeView();
	            recipeView.textViewName = (TextView) view.findViewById(R.id.textview_list_item);
	 
	            // cache the view objects in the tag, so they can be re-accessed later
	            view.setTag(recipeView);
	        } else
	        	recipeView = (RecipeView) view.getTag();
	 
	        // set up view, store unique ID to retrieve recipe from database when selected
	        Recipe recipe = recipes.get(position);
	        recipeView.textViewName.setText(recipe.name);
	        recipeView.recipeID = recipe.recipeID;
	 
	        return view;
	    }
		
		class RecipeView {
			TextView textViewName;
			int recipeID;
		}
	}
}
