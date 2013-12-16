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
 * Manages all recipe data.
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public final class RecipeDatabase {
	// STATE VARIABLES
	private static Map<String, Set<Integer>> indexMap; // maps search word to Set of recipeId's
	private static Map<Integer, Recipe> idMap; // maps recipeId to corresponding recipe
	private static Set<Integer> favoriteRecipes; // set containing recipeId's of favorite recipes
	private static Map<Integer, Byte> shoppingListRecipes; // maps recipeId to shopping list quantity
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
		favoriteRecipes = new HashSet<Integer>();
		shoppingListRecipes = new HashMap<Integer, Byte>();
		recipeCounter = 0;
	}
	
	/**
	 * Returns the singleton instance of the Recipe database.
	 * @return the singleton instance of the Recipe database.
	 */
	public synchronized static RecipeDatabase getInstance() {
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
		
		Recipe(String name, List<RecipeIngredient> ingredients, List<RecipeDirection> directions) {
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

		RecipeIngredient(String amount, String measurement, String ingredientName) {
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
		
		RecipeDirection(String direction) {
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
	 * Helper function which takes a Set of recipeId's and returns the corresponding List of Recipes, sorted by name.
	 * @param recipeIdSet the Set of recipeId's to retrieve the sorted List for.
	 * @return the List of Recipes corresponding to the Set of recipeId's, sorted by name.
	 */
	private List<Recipe> getRecipesById(Set<Integer> recipeIdSet) {
		List<Recipe> result = new ArrayList<Recipe>();
		
		if (recipeIdSet != null) {
			Map<String, Set<Recipe>> resultTree = new TreeMap<String, Set<Recipe>>();
			
			for (int i : recipeIdSet) {
				Recipe recipe = idMap.get(i);
				String recipeName = recipe.name;
				
				// sort recipes by name, duplicate names OK
				if (!resultTree.containsKey(recipeName))
					resultTree.put(recipeName, new HashSet<Recipe>());
				resultTree.get(recipeName).add(recipe);
			}
			
			// transfer sorted results from Map to List
			for (Map.Entry<String, Set<Recipe>> entry : resultTree.entrySet()) {
				for (Recipe r : entry.getValue())
					result.add(r);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a list of all recipes, sorted by name.
	 * @return a list of all recipes, sorted by name.
	 */
	public List<Recipe> allRecipes() {
		return getRecipesById(idMap.keySet());
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
			
			// fill out hitTable
			if (set != null) {
				for (int i : set) {
					Integer count = hitTable.get(i);
					count = (count == null) ? 1 : (count + 1);
					
					hitTable.put(i, count);
				}
			}
		}
		
		// get Set of matches
		Set<Integer> resultSet = new HashSet<Integer>();
		for (Map.Entry<Integer, Integer> entry : hitTable.entrySet()) {
			if (entry.getValue() == numMatchesRequired)
				resultSet.add(entry.getKey());
		}
		
		// convert Set of recipeId's to List of sorted Recipes and return 
		return getRecipesById(resultSet);
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
	
	/**
	 * Load favorite Recipes into database from a serialized String containing the recipe indexes.
	 * @param serialized serialized String containing the recipe indexes.
	 */
	public void loadFavoriteRecipes(String serialized) {
		if (serialized == null || serialized.length() == 0)
			return;
		
		String[] deserialized = serialized.split(" ");
		
		for (String s : deserialized) {
			int recipeId = Integer.valueOf(s);
			
			// in case recipe no longer exists
			if (idMap.containsKey(recipeId))
				favoriteRecipes.add(recipeId);
		}
	}
	
	/**
	 * Returns a serialized string containing all the favorite recipe indexes.
	 * Used to conveniently store favoriteRecipes in the preferences file.
	 * @return a serialized string containing all the favorite recipe indexes.
	 */
	public String getSerializedFavorites() {
		String result = "";
		
		if (!favoriteRecipes.isEmpty()) {
			for (int i : favoriteRecipes)
				result += String.valueOf(i) + " ";
			
			result = result.substring(0, result.length() - 1); // remove trailing space
		}
		
		return result;
	}
	
	/**
	 * Returns a List of favorite Recipes, sorted by name.
	 * @return a List of favorite Recipes, sorted by name.
	 */
	public List<Recipe> getFavoriteRecipes() {
		return getRecipesById(favoriteRecipes);
	}
	
	/**
	 * Add Recipe to favoriteRecipes.
	 * @param recipeId the unique identifier of the Recipe to add to favoriteRecipes. 
	 */
	public void addFavorite(int recipeId) {
		favoriteRecipes.add(recipeId);
	}
	
	/**
	 * Remove Recipe from favoriteRecipes.
	 * @param recipeId the unique identifier of the Recipe to remove from favoriteRecipes.
	 */
	public void removeFavorite(int recipeId) {
		favoriteRecipes.remove(recipeId);
	}
	
	/**
	 * Returns true if the recipeId corresponds to a Recipe currently marked as a favorite, false otherwise.
	 * @param recipeId the unique identifier for the Recipe being queried.
	 * @return true if the recipeId corresponds to a Recipe currently marked as a favorite, false otherwise.
	 */
	public boolean isFavorite(int recipeId) {
		if (favoriteRecipes.contains(recipeId))
			return true;
		
		return false;
	}
	
	/**
	 * Load shopping list Recipes into database from a serialized String containing the recipe indexes and respective quantities.
	 * @param serialized serialized String containing the recipe indexes and respective quantities.
	 */
	public void loadShoppingListRecipes(String serialized) {
		if (serialized == null || serialized.length() == 0)
			return;
		
		// deserialize data
		String[] deserialized = serialized.split(" ");
		int length = deserialized.length / 2;
		Integer[] recipeIds = new Integer[length];
		Byte[] recipeQuantities = new Byte[length];
		
		for (int i = 0; i < deserialized.length; i += 2) {
			recipeIds[i] = Integer.valueOf(deserialized[i]);
			recipeQuantities[i] = Byte.valueOf(deserialized[i + 1]);
		}
		
		// load data
		for (int i = 0; i < length; i++) {
			// in case recipe no longer exists
			if (idMap.containsKey(recipeIds[i]))
				shoppingListRecipes.put(recipeIds[i], recipeQuantities[i]);
		}
	}
	
	/**
	 * Returns a serialized string containing all the shopping list recipe indexes and respective quantities.
	 * Used to conveniently store shoppingListRecipes in the preferences file.
	 * @return a serialized string containing all the shopping list recipe indexes and respective quantities.
	 */
	public String getSerializedShoppingList() {
		String result = "";
		
		if (!shoppingListRecipes.isEmpty()) {
			for (Map.Entry<Integer, Byte> entry : shoppingListRecipes.entrySet())
				result += String.valueOf(entry.getKey()) + " " + String.valueOf(entry.getValue()) + " ";
			
			result = result.substring(0, result.length() - 1); // remove trailing space
		}
		
		return result;
	}
	
	/**
	 * Returns a List of shopping list Recipes, sorted by name.
	 * @return a List of shopping list Recipes, sorted by name.
	 */
	public List<Recipe> getShoppingListRecipes() {
		return getRecipesById(shoppingListRecipes.keySet());
	}
	
	/**
	 * Updates the shopping list quantity of the specified Recipe.
	 * @param recipeId the unique identifier for the Recipe whose shopping list quantity is to be updated.
	 * @param quantity the new quantity of the Recipe to be saved to the shopping list.
	 */
	public void updateQuantity(int recipeId, byte quantity) {
		if (quantity == 0)
			shoppingListRecipes.remove(recipeId);
		else
			shoppingListRecipes.put(recipeId, quantity);
	}
	
	/**
	 * Returns the quantity of the specified Recipe stored in the shopping list.
	 * @param recipeId the unique identifier for the Recipe being queried.
	 * @return the quantity of the specified Recipe stored in the shopping list.
	 */
	public byte getQuantity(int recipeId) {
		Byte result = shoppingListRecipes.get(recipeId);
		
		return (result == null) ? 0 : result;
	}
}
