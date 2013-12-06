package com.companyx.android.appx;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
}
