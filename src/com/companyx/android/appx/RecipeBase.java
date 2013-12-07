package com.companyx.android.appx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Recipe Database
 * 
 * Manages recipe data.
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class RecipeBase {
	Map<String, Recipe> recipeDatabase;
	
	RecipeBase() {
		recipeDatabase = new TreeMap<String, Recipe>();
	}
	
	class Recipe {
		String name;
		int timeRequiredInMin;
		List<Ingredient> ingredients;
		
		Recipe (String name) {
			this.name = name;
		}
	}
	
	class Ingredient {
		String name;
		
		Ingredient (String name) {
			this.name = name;
		}
	}
	
	/**
	 * Adds a new recipe to the recipe database.
	 * @param newRecipe the new recipe to be added to the database.
	 * @return null if the recipe name was not previously contained in the database, otherwise returns the replaced recipe of the same name.
	 */
	public Recipe addRecipe(Recipe newRecipe) {
		if (newRecipe == null)
			return null;
		
		return recipeDatabase.put(newRecipe.name, newRecipe);
	}
	
	/**
	 * Returns a list of all recipes, sorted by name.
	 * @return a list of all recipes, sorted by name.
	 */
	public List<Recipe> getRecipe() {
		List<Recipe> result = new ArrayList<Recipe>();
		
		for (Map.Entry<String, Recipe> entry : recipeDatabase.entrySet())
			result.add(entry.getValue());
		
		return result;
	}
}
