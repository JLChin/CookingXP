package com.companyx.android.appx;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Recipe Database
 * 
 * Manages recipe data.
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class RecipeBase {
	private Map<String, List<Recipe>> recipeMap; // maps recipe name to List of recipes that match that name
	private Map<String, Set<Integer>> indexMap; // maps search word to Set of recipeID's
	private Map<Integer, Recipe> idMap; // maps unique integer ID to corresponding recipe
	private int recipeCounter; // gives each recipe a unique number, current value represents the next available ID
	
	@SuppressLint("UseSparseArrays")
	RecipeBase() {
		recipeMap = new TreeMap<String, List<Recipe>>();
		indexMap = new HashMap<String, Set<Integer>>();
		idMap = new HashMap<Integer, Recipe>();
		recipeCounter = 0;
	}
	
	/**
	 * Class representing a recipe.
	 */
	static class Recipe {
		String name;
		int recipeID;
		int timeRequiredInMin; // TODO
		List<RecipeIngredient> recipeIngredients;
		List<Direction> directions;
		
		Recipe (String name, List<RecipeIngredient> recipeIngredients, List<Direction> directions) {
			this.name = name;
			this.recipeIngredients = recipeIngredients;
			this.directions = directions;
		}
	}
	
	/**
	 * Class representing one itemized ingredient consisting of the number amount, unit of measurement, and ingredient.
	 * Example: 1-1/4 Tablespoon Sugar
	 */
	static class RecipeIngredient {
		float amount;
		String measurement;
		String ingredientName;

		RecipeIngredient (float amount, String measurement, String ingredientName) {
			this.amount = amount;
			this.measurement = measurement;
			this.ingredientName = ingredientName;
		}
	}
	
	/**
	 * Class representing one instruction or action of the recipe.
	 */
	static class Direction {
		String direction;
		
		Direction (String direction) {
			this.direction = direction;
		}
	}
	
	/**
	 * Adds a new recipe to the recipe map and index map.
	 * Assumes recipe is well-formed.
	 * @param newRecipe the new recipe to be added to the database.
	 */
	public void addRecipe(Recipe newRecipe) {
		if (newRecipe == null)
			return;
		
		// ASSIGN UNIQUE ID
		newRecipe.recipeID = recipeCounter++;
		
		// INDEX ID
		idMap.put(newRecipe.recipeID, newRecipe);
		
		// INDEX RECIPE NAME
		index(newRecipe.name, newRecipe);
		
		// INDEX INGREDIENT NAMES
		for (RecipeIngredient ri : newRecipe.recipeIngredients)
			index(ri.ingredientName, newRecipe);
		
		// ADD TO MASTER RECIPE MAP
		if (!recipeMap.containsKey(newRecipe.name))
			recipeMap.put(newRecipe.name, new ArrayList<Recipe>());
		recipeMap.get(newRecipe.name).add(newRecipe);
	}
	
	/**
	 * Helper function that indexes the given recipe by the words contained in the specified String.
	 * @param string String containing the words to index the given recipe by.
	 * @param recipe the given recipe.
	 */
	private void index(String string, Recipe recipe) {
		String[] words = string.split(" ");

		// convert words to lowercase for indexing
		for (int i = 0; i < words.length; i++)
			words[i] = words[i].toLowerCase(Locale.US);

		for (String word : words) {
			// new word, did not exist previously
			if (!indexMap.containsKey(word))
				indexMap.put(word, new HashSet<Integer>());

			// add recipeID to search index
			indexMap.get(word).add(recipe.recipeID);
		}
	}
	
	/**
	 * Returns a list of all recipes, sorted by name.
	 * @return a list of all recipes, sorted by name.
	 */
	public List<Recipe> getRecipes() {
		List<Recipe> result = new ArrayList<Recipe>();
		
		for (Map.Entry<String, List<Recipe>> entry : recipeMap.entrySet()) {
			for (Recipe r : entry.getValue())
				result.add(r);
		}
		
		return result;
	}
	
	/**
	 * Returns a list of all recipes matching the specified search String, sorted by name.
	 * @param searchString String containing the specified search term(s).
	 * @return a list of all recipes matching the specified search String, sorted by name.
	 */
	@SuppressLint("UseSparseArrays")
	public List<Recipe> searchRecipes(String searchString) {
		if (searchString == null)
			return null;
		
		// convert searchString to lowercase, to find both lower and uppercase results in the database
		searchString = searchString.toLowerCase(Locale.US);
		
		// parse search terms
		String[] searchWords = searchString.split(" ");
		
		// eliminate duplicate search terms
		Set<String> searchWordSet = new HashSet<String>();
		for (String s : searchWords)
			searchWordSet.add(s);
		
		// number of matches required
		int numTerms = searchWordSet.size();
		
		// create structure to hold unique recipes and count the number of hits for each
		Map<Integer, Integer> hitTable = new HashMap<Integer, Integer>();
		
		for (String s : searchWordSet) {
			// get all recipes containing the current word in the name or ingredient list
			Set<Integer> set = indexMap.get(s);
			
			if (set != null) {
				for (int i : set) {
					Recipe r = idMap.get(i); // retrieve recipe
					
					if (!hitTable.containsKey(r.recipeID))
						hitTable.put(r.recipeID, 1);
					else
						hitTable.put(r.recipeID, hitTable.get(r.recipeID) + 1);
				}
			}
		}
		
		// get results and sort by recipe name
		Map<String, List<Recipe>> resultTree = new TreeMap<String, List<Recipe>>();
		for (Map.Entry<Integer, Integer> entry : hitTable.entrySet()) {
			// if all terms matched, add the recipe to the result
			if (entry.getValue() == numTerms) {
				Recipe recipe = idMap.get(entry.getKey());
				
				// new recipe name
				if (!resultTree.containsKey(recipe.name))
					resultTree.put(recipe.name, new ArrayList<Recipe>());
				
				resultTree.get(recipe.name).add(recipe);
			}
		}
		
		// return results as a List
		List<Recipe> result = new ArrayList<Recipe>();
		for (Map.Entry<String, List<Recipe>> entry : resultTree.entrySet()) {
			for (Recipe r : entry.getValue())
				result.add(r);
		}
		
		return result;
	}
}
