package com.companyx.android.appx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.annotation.SuppressLint;

/**
 * Recipe Database
 * 
 * Manages all recipe data.
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public final class RecipeDatabase {
	// FOOD TYPES
	public static final String[] MEAT = {"bacon", "beef",  "chicken", "duck", "eel", "ham", "pork", "steak", "turkey"};
	public static final String[] SEAFOOD = {"carp", "clam", "crab", "fish", "herring", "lobster", "oyster", "salmon", "tilapia", "tuna"};
	public static final String[] PRODUCE = {"apples", "avocado", "bananas", "cabbage", "carrots", "celery", "cucumbers", "eggplants", "eggs", "lettuce", "onions", "peas", "potatoes", "spinach"};
	public static final byte TYPE_MEAT = 0;
	public static final byte TYPE_SEAFOOD = 1;
	public static final byte TYPE_PRODUCE = 2;
	private static Map<String, Byte> foodTypeMap; // maps ingredient keywords to their type category
	
	// MEASUREMENT ALIASES
	public static final String[] POUNDS_ALIASES = {"lb", "lbs", "pound", "pounds"};
	public static final String POUNDS = "pounds";
	private static Map<String, String> measurementAliases; // maps measurement alias to the preferred measurement name, i.e. "lbs" to "pounds"
	
	// STATE VARIABLES
	private static Map<String, Set<Integer>> indexMap; // maps search word to Set of recipeId's
	private static Map<Integer, Recipe> idMap; // maps recipeId to corresponding recipe
	private static Set<Integer> favoriteRecipes; // set containing recipeId's of favorite recipes
	private static Map<Integer, Byte> shoppingListRecipes; // maps recipeId to shopping list quantity

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
		
		measurementAliases = new HashMap<String, String>();
		
		foodTypeMap = new HashMap<String, Byte>();
		
		loadMeasurementAliases();
		loadFoodTypes();
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
	 * TODO List<RecipeCategory>
	 */
	static class Recipe {
		int recipeId;
		String name;
		List<RecipeIngredient> ingredients;
		List<RecipeDirection> directions;
		short timeRequiredInMin;
		byte difficultyLevel;
		
		Recipe(int recipeId, String name, List<RecipeIngredient> ingredients, List<RecipeDirection> directions, short timeRequiredInMin, byte difficultyLevel) {
			this.recipeId = recipeId;
			this.name = name;
			this.ingredients = ingredients;
			this.directions = directions;
			this.timeRequiredInMin = timeRequiredInMin;
			this.difficultyLevel = difficultyLevel;
		}
	}
	
	/**
	 * Class representing one itemized ingredient consisting of the number amount, unit of measurement, and ingredient.
	 * Example: 1 1/4 tablespoon sugar, slightly charred
	 */
	static class RecipeIngredient {
		String amount;
		String measurement;
		String ingredientName;
		String notes;

		RecipeIngredient(String amount, String measurement, String ingredientName, String notes) {
			this.amount = amount;
			this.measurement = measurement;
			this.ingredientName = ingredientName;
			this.notes = notes;
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
	 * Class which holds shopping list information to be displayed, separated into food types.
	 */
	static class ShoppingList {
		List<String> meat;
		List<String> seafood;
		List<String> produce;
		List<String> other;
		
		ShoppingList() {
			meat = new ArrayList<String>();
			seafood = new ArrayList<String>();
			produce = new ArrayList<String>();
			other = new ArrayList<String>();
		}
	}
	
	/**
	 * Adds a new recipe to the recipe map and index map.
	 * @param newRecipe the new recipe to be added to the database.
	 */
	public void addRecipe(Recipe newRecipe) {
		if (newRecipe == null)
			return;
		
		// GET UNIQUE ID
		int recipeId = newRecipe.recipeId;
		
		// INDEX ID
		idMap.put(recipeId, newRecipe);
		
		// INDEX RECIPE NAME
		index(newRecipe.name, recipeId);
		
		// INDEX INGREDIENT NAMES
		for (RecipeIngredient ri : newRecipe.ingredients)
			index(ri.ingredientName, recipeId);
	}
	
	/**
	 * Helper function that indexes the given recipe by the words contained in the specified String.
	 * @param string String containing the words to index the given recipe by.
	 * @param recipeId the unique identifier for the Recipe being indexed.
	 */
	private void index(String string, int recipeId) {
		String[] words = string.split(" ");

		// convert words to lowercase for indexing
		for (int i = 0; i < words.length; i++)
			words[i] = words[i].toLowerCase(Locale.US);

		for (String word : words) {
			// new word, did not exist previously
			if (!indexMap.containsKey(word))
				indexMap.put(word, new HashSet<Integer>());

			// add recipeId to search index
			indexMap.get(word).add(recipeId);
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
			recipeIds[i / 2] = Integer.valueOf(deserialized[i]);
			recipeQuantities[i / 2] = Byte.valueOf(deserialized[i + 1]);
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
	
	/**
	 * Returns a List of shopping list Recipes, sorted by name.
	 * @return a List of shopping list Recipes, sorted by name.
	 */
	public List<Recipe> getShoppingListRecipes() {
		return getRecipesById(shoppingListRecipes.keySet());
	}
	
	/**
	 * Returns a ShoppingList object containing the aggregated shopping list ingredients.
	 * @return a ShoppingList object containing the aggregated shopping list ingredients.
	 */
	public ShoppingList getShoppingList() {
		ShoppingList result = new ShoppingList();
		
		Map<String, Double> amountMap = new HashMap<String, Double>(); // maps the ingredient name to the amount
		Map<String, String> measurementMap = new HashMap<String, String>(); // maps the ingredient name to the measurement type
		Set<String> amountless = new HashSet<String>(); // set of ingredient names where the amount and measurement is ignored for the shopping list
		 
		// for each recipe on the shopping list
		for (Map.Entry<Integer, Byte> entry : shoppingListRecipes.entrySet()) {
			Recipe recipe = findRecipeById(entry.getKey());
			List<RecipeIngredient> recipeIngredients = recipe.ingredients;
			
			// for each ingredient of each recipe
			for (RecipeIngredient ri : recipeIngredients) {
				Double amount = entry.getValue() * stringToDouble(ri.amount); // recipe quantity * ingredient quantity
				String measurementAlias = measurementAliases.get(measurementCleaner(ri.measurement));
				String name = ri.ingredientName;
				
				if (measurementAlias != null) { // "4 apples" or "5-1/2 lbs. chicken"
					if (!amountMap.containsKey(name)) {
						measurementMap.put(name, measurementAlias);
						amountMap.put(name, amount);
					} else
						amountMap.put(name, amountMap.get(name) + amount);
				} else // 5 tbsps. pepper
					amountless.add(name);
			}
		}
		
		// add aggregated ingredients to result list
		for (Map.Entry<String, Double> entry : amountMap.entrySet()) {
			String ingredientName = entry.getKey();
			String s = entry.getValue() + " " + measurementMap.get(entry.getKey()) + " " + ingredientName;
			s = s.replace(".0", ""); // US
			s = s.replace(",0", ""); // Euro
			
			// determine which list this ingredient goes in (meat, seafood, other, etc)
			Byte category = null;
			String name[] = ingredientName.split("[\\s\\-]"); // whitespace or hyphen
			for (String n: name) {
				category = foodTypeMap.get(n.toLowerCase(Locale.US));
				if (category != null)
					break;
			}

			// add to appropriate list
			if (category == null)
				result.other.add(s);
			else if (category == TYPE_MEAT)
				result.meat.add(s);
			else if (category == TYPE_SEAFOOD)
				result.seafood.add(s);
			else if (category == TYPE_PRODUCE)
				result.produce.add(s);
			else
				result.other.add(s);
		}
		
		// add remaining amountless ingredients to other list
		for (String s : amountless)
			result.other.add(s);
		
		return result;
	}
	
	/**
	 * Helper function to convert a string to a double.
	 * Example: 1-1/4 --> 1.25
	 * @param string the String representing an ingredient amount.
	 * @return the double value of the String ingredient amount.
	 */
	private double stringToDouble(String string) {
		double result = 0;
		
		// split on whitespace or hyphen
		String[] terms = string.split("[\\s\\-]");
		
		for (String s : terms)
			result += (s.contains("/")) ? (Double.valueOf(s.substring(0, 1)) / Double.valueOf(s.substring(2, 3))) : Double.valueOf(s);
			
		return result;
	}
	
	/**
	 * Helper function which cleans up the measurement term.
	 * @param string the original string to clean up.
	 * @return the cleaned string.
	 */
	private String measurementCleaner(String string) {
		if (string == null || string.equals(""))
			return "";
		
		// convert to lowercase
		string = string.toLowerCase(Locale.US);
		
		// remove trailing period if any
		if (string.charAt(string.length() - 1) == '.')
			string = string.substring(0, string.length() - 1);
		
		return string;
	}
	
	/**
	 * Load aliases into the measurementAliases Map.
	 * Note: any entry in the alias table corresponds to an ingredient to be aggregated in the shopping list.
	 */
	private void loadMeasurementAliases() {
		// EMPTY
		measurementAliases.put("", "");
		
		// POUNDS
		for (String s : POUNDS_ALIASES)
			measurementAliases.put(s, POUNDS);
	}
	
	/**
	 * Loads food types into the foodType Map
	 */
	private void loadFoodTypes() {
		for (String s : MEAT)
			foodTypeMap.put(s, TYPE_MEAT);
		
		for (String s : SEAFOOD)
			foodTypeMap.put(s, TYPE_SEAFOOD);
		
		for (String s : PRODUCE)
			foodTypeMap.put(s, TYPE_PRODUCE);
	}
}
