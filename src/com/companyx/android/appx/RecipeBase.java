package com.companyx.android.appx;

import java.util.ArrayList;
import java.util.HashMap;
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
	Map<String, Recipe> recipeMap; // recipes indexed by recipe name
	Map<String, List<Recipe>> indexMap; // recipes indexed by search word
	
	RecipeBase() {
		recipeMap = new TreeMap<String, Recipe>();
		indexMap = new HashMap<String, List<Recipe>>();
	}
	
	static class Recipe {
		String name;
		int timeRequiredInMin;
		List<RecipeIngredient> recipeIngredients;
		
		Recipe (String name, List<RecipeIngredient> recipeIngredients, String directions) {
			this.name = name;
		}
	}
	
	static class Ingredient {
		String name;
		
		Ingredient (String name) {
			this.name = name;
		}
	}
	
	static class RecipeIngredient {
		float amount;
		String measurement;
		Ingredient ingredient;

		RecipeIngredient (float amount, String measurement, Ingredient ingredient) {
			this.amount = amount;
			this.measurement = measurement;
			this.ingredient = ingredient;
		}
	}
	
	/**
	 * Adds a new recipe to the recipe map and index map.
	 * @param newRecipe the new recipe to be added to the database.
	 */
	public void addRecipe(Recipe newRecipe) {
		if (newRecipe == null)
			return;
		
		// index the recipe by the words in the recipe name
		String[] words = newRecipe.name.split(" ");
		for (String word : words) {
			// new word
			if (!indexMap.containsKey(word))
				indexMap.put(word, new ArrayList<Recipe>());
			
			// add new recipe to search index
			indexMap.get(word).add(newRecipe);
		}
		
		// TODO allow recipe name duplicates
		
		// add new recipe to master list
		recipeMap.put(newRecipe.name, newRecipe);
	}
	
	/**
	 * Returns a list of all recipes, sorted by name.
	 * @return a list of all recipes, sorted by name.
	 */
	public List<Recipe> getRecipes() {
		List<Recipe> result = new ArrayList<Recipe>();
		
		for (Map.Entry<String, Recipe> entry : recipeMap.entrySet())
			result.add(entry.getValue());
		
		return result;
	}
	
	/**
	 * Returns a list of all recipes matching the specified search String, sorted by name.
	 * TODO duplicate result names
	 * @param searchString String containing the specifed search term(s).
	 * @return a list of all recipes matching the specified search String, sorted by name.
	 */
	public List<Recipe> searchRecipes(String searchString) {
		if (searchString == null)
			return null;
		
		Map<String, Recipe> resultTree = new TreeMap<String, Recipe>();
		List<Recipe> list = indexMap.get(searchString);
		
		for (Recipe r : list)
			resultTree.put(r.name, r);
		
		List<Recipe> result = new ArrayList<Recipe>();
		for (Map.Entry<String, Recipe> entry : resultTree.entrySet())
			result.add(entry.getValue());
		
		return result;
	}
}
