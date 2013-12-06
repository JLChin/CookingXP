package com.companyx.android.appx;

import java.util.List;

public class RecipeBase {
	public static final String[] ingredientType = { "MEAT", "VEGETABLE", "FRUIT" };
	
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
