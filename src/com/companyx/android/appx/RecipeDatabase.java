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
 * @author James Chin <jameslchin@gmail.com>
 */
public final class RecipeDatabase {
	// STATE VARIABLES
	private static Map<String, Set<Integer>> indexMap; // maps search word to Set of recipeId's
	private static Map<Integer, Recipe> idMap; // maps unique ID to corresponding recipe
	private static Set<Integer> favorites; // set containing unique ID's of favorite recipes
	private static int recipeCounter; // gives each recipe a unique number, current value represents the next available ID

	// SINGLETON
	private static RecipeDatabase holder;

	private RecipeDatabase() {
		resetDatabase();
	}
	
	/**
	 * Resets the database.
	 */
	@SuppressLint("UseSparseArrays")
	public void resetDatabase() {
		indexMap = new HashMap<String, Set<Integer>>();
		idMap = new HashMap<Integer, Recipe>();
		favorites = new HashSet<Integer>();
		recipeCounter = 0;
	}
	
	/**
	 * Returns the singleton instance of the Recipe database.
	 * @return the singleton instance of the Recipe database.
	 */
	public static RecipeDatabase getInstance() {
		if (holder == null)
			holder = new RecipeDatabase();
		return holder;
	}
	
	/**
	 * Class representing a recipe.
	 * TODO int index, List<RecipeCategory>, short timeRequiredInMin, byte difficultyLevel
	 */
	static class Recipe {
		String name;
		int recipeId;
		List<RecipeIngredient> ingredients;
		List<RecipeDirection> directions;
		
		Recipe (String name, List<RecipeIngredient> ingredients, List<RecipeDirection> directions) {
			this.name = name;
			this.ingredients = ingredients;
			this.directions = directions;
		}
	}
	
	/**
	 * Class representing one itemized ingredient consisting of the number amount, unit of measurement, and ingredient.
	 * Example: 1-1/4 Tablespoon Sugar
	 */
	static class RecipeIngredient {
		String amount;
		String measurement;
		String ingredientName;

		RecipeIngredient (String amount, String measurement, String ingredientName) {
			this.amount = amount;
			this.measurement = measurement;
			this.ingredientName = ingredientName;
		}
	}
	
	/**
	 * Class representing one instruction or action of the recipe.
	 */
	static class RecipeDirection {
		String direction;
		
		RecipeDirection (String direction) {
			this.direction = direction;
		}
	}
	
	/**
	 * Load favorites into database from a serialized String containing the recipe indexes.
	 * @param serializedFavorites serialized String containing the recipe indexes.
	 */
	public void loadFavorites(String serializedFavorites) {
		if (serializedFavorites == null)
			return;
		
		String[] deserialized = serializedFavorites.split(" ");
		
		for (String s : deserialized)
			favorites.add(Integer.valueOf(s));
	}
	
	/**
	 * Returns a List of favorite Recipes, sorted by name.
	 * @return a List of favorite Recipes, sorted by name.
	 */
	public List<Recipe> getFavorites() {
		List<Recipe> result = new ArrayList<Recipe>();
		Map<String, Set<Recipe>> resultMap = new TreeMap<String, Set<Recipe>>();
		
		for (int i : favorites) {
			Recipe recipe = idMap.get(i);
			String recipeName = recipe.name;
			
			if (!resultMap.containsKey(recipeName))
				resultMap.put(recipeName, new HashSet<Recipe>());
			resultMap.get(recipeName).add(recipe);
		}
		
		for (Map.Entry<String, Set<Recipe>> entry : resultMap.entrySet()) {
			for (Recipe r : entry.getValue())
				result.add(r);
		}
		
		return result;
	}
	
	/**
	 * Returns true if the recipeId corresponds to a Recipe currently marked as a favorite, false otherwise.
	 * @param recipeId unique identifier for the Recipe being queried.
	 * @return true if the recipeId corresponds to a Recipe currently marked as a favorite, false otherwise.
	 */
	public boolean isFavorite(int recipeId) {
		if (favorites.contains(recipeId))
			return true;
		
		return false;
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
		newRecipe.recipeId = recipeCounter++;
		
		// INDEX ID
		idMap.put(newRecipe.recipeId, newRecipe);
		
		// INDEX RECIPE NAME
		index(newRecipe.name, newRecipe);
		
		// INDEX INGREDIENT NAMES
		for (RecipeIngredient ri : newRecipe.ingredients)
			index(ri.ingredientName, newRecipe);
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

			// add recipeId to search index
			indexMap.get(word).add(recipe.recipeId);
		}
	}
	
	/**
	 * Returns a list of all recipes, sorted by name.
	 * @return a list of all recipes, sorted by name.
	 */
	public List<Recipe> allRecipes() {
		List<Recipe> result = new ArrayList<Recipe>();
		
		// maps Recipe name to Set of Recipe Id's, used to sort recipes by name
		Map<String, Set<Integer>> sortedMap = new TreeMap<String, Set<Integer>>();
		
		for (Map.Entry<Integer, Recipe> entry : idMap.entrySet()) {
			String recipeName = entry.getValue().name;
			
			if (!sortedMap.containsKey(recipeName))
				sortedMap.put(recipeName, new HashSet<Integer>());
			sortedMap.get(recipeName).add(entry.getKey());
		}
		
		// now that recipes are sorted, return as a List
		for (Map.Entry<String, Set<Integer>> entry : sortedMap.entrySet()) {
			for (Integer i : entry.getValue())
				result.add(idMap.get(i));
		}
		
		return result;
	}
	
	/**
	 * Returns a list of all recipes matching the specified search String, sorted by name.
	 * All search terms must match to return a recipe.
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
		int numMatchesRequired = searchWordSet.size();
		
		// create structure to hold unique recipes and count the number of hits for each
		Map<Integer, Integer> hitTable = new HashMap<Integer, Integer>();
		
		for (String s : searchWordSet) {
			// get all recipes containing the current word in the name or ingredient list
			Set<Integer> set = indexMap.get(s);
			
			if (set != null) {
				for (int i : set) {
					Recipe r = idMap.get(i); // retrieve recipe
					
					if (!hitTable.containsKey(r.recipeId))
						hitTable.put(r.recipeId, 1);
					else
						hitTable.put(r.recipeId, hitTable.get(r.recipeId) + 1);
				}
			}
		}
		
		// get results and sort by recipe name
		Map<String, List<Recipe>> resultTree = new TreeMap<String, List<Recipe>>();
		for (Map.Entry<Integer, Integer> entry : hitTable.entrySet()) {
			// if all terms matched, add the recipe to the result
			if (entry.getValue() == numMatchesRequired) {
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
	
	/**
	 * Returns the Recipe corresponding to the unique Id, null if non-existent or invalid Id.
	 * @param recipeId the unique Id to retrieve the Recipe for.
	 * @return the Recipe corresponding to the unique Id, null if non-existent or invalid Id.
	 */
	public Recipe findRecipeById (int recipeId) {
		if (recipeId < 0)
			return null;
		
		return idMap.get(recipeId);
	}
}
