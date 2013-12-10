package com.companyx.android.appx;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.companyx.android.appx.RecipeBase.Recipe;

public class SelectRecipeActivity extends BaseListActivity {
	RecipeBase recipeBase;
	private List<Recipe> recipes;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_recipe);

		initialize();
	}

	private void initialize() {
		initializeListView();
		loadRecipes();

		setListAdapter(new RecipeListViewAdapter(this, recipes));
	}

	private void initializeListView() {
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Recipe selected, now what?
			}
		});
	}
	
	private void loadRecipes() {
		recipeBase = new RecipeBase();
		
		recipeBase.addRecipe(new Recipe("Curry Pie", null, null));
		recipeBase.addRecipe(new Recipe("Curry Pork", null, null));
		recipeBase.addRecipe(new Recipe("Baked Salmon", null, null));
		recipeBase.addRecipe(new Recipe("Apple Pie", null, null));
		recipeBase.addRecipe(new Recipe("Pulled Pork Sandwich", null, null));
		
		//recipes = recipeBase.getRecipes();
		recipes = recipeBase.searchRecipes("Pie");
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
	 
	        // set up view
	        Recipe recipe = recipes.get(position);
	        recipeView.textViewName.setText(recipe.name);
	 
	        return view;
	    }
		
		class RecipeView {
			TextView textViewName;
		}
	}
}
