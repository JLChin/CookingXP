package com.companyx.android.cookingxp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.companyx.android.cookingxp.RecipeDatabase.Recipe;
import com.companyx.android.cookingxp.RecipeDatabase.RecipeDirection;
import com.companyx.android.cookingxp.RecipeDatabase.RecipeIngredient;
import com.companyx.android.cookingxp.RecipeDatabase.RecipeTime;

@SuppressWarnings("unused")
// Recipe Reader
// @author Adam Hackbarth <adam.hackbarth@gmail.com>
public class RecipeLoader {
	InputStream inputStream;
	RecipeDatabase recipeDatabase;

	// constructor
	RecipeLoader(InputStream inputStream, RecipeDatabase recipeDatabase) {
		this.inputStream = inputStream;
		this.recipeDatabase = recipeDatabase;
	}

	public void loadData() {
		
		/* Loading Raw Data from File Location into Scanner */
		Scanner scanner = new Scanner(inputStream);
		scanner.useDelimiter(System.getProperty("line.separator"));
		String INPUT = scanner.next();
		String keyLine = "";
		int recipeNumber = 0;
		
		while (scanner.hasNext()) {
			if ("0:E".matches(INPUT.trim())) {
				ArrayList<String> ingredListArray = new ArrayList<String>();
				List<Short> boxAssignment = new ArrayList<Short>();
				List<String> directList = new ArrayList<String>();
				List<Integer> linkedRecipe = new ArrayList<Integer>();
				keyLine = INPUT; // saving the recipe key line
				String initVal = scanner.next(); // Starting Point Holder
				String regex = "\\b  +";
				String blank = "";
				String title = "";
				String auth = "";
				INPUT = initVal;
				
				// Recipe Linking Placeholder - Currently just linking to itself
				linkedRecipe.add(recipeNumber);

				// Storing Recipe title
				while (scanner.hasNext()) {
					if ("0:E".equals(INPUT.trim())) {
						INPUT = scanner.next();
					} else {
						title = INPUT;
						INPUT = scanner.next();
						break;
					}
				}

				// Split Title/Author/boxId line & Capitalize the first letter of each word of a recipe
				if (title.contains(":")) {
					String titleS[] = title.split("\\b:");
					String capitalizeTitleWords[] = titleS[0].trim().split("\\s");
					auth = titleS[1].trim();
					title = "";
					for (int j = 2; j < titleS.length; j++) {
						int boxId = Integer.valueOf(titleS[j].trim());
						boxAssignment.add((short) boxId);
					}
					for (int i = 0; i < capitalizeTitleWords.length; i++){
						char firstLetter = Character.toUpperCase(capitalizeTitleWords[i].charAt(0));
						title += " " + firstLetter + capitalizeTitleWords[i].substring(1, capitalizeTitleWords[i].length());
					}
					title = title.substring(1);
				}
				
				// Time Split to Int & then Short for RecipeTime
				String timeL[] = INPUT.split("\\:");
				int pr = Integer.valueOf(timeL[0].trim());
				int ipr = Integer.valueOf(timeL[1].trim());
				int c = Integer.valueOf(timeL[2].trim());

				RecipeTime timeC = new RecipeTime((short) pr, (short) ipr,
						(short) c);

				// Serving size byte (serveSize)
				int q = Integer.valueOf(timeL[3].trim());
				byte serveSize = (byte) q;
				INPUT = scanner.next();

				// Grabbing Recipe Ingredients
				while (scanner.hasNext()) {
					Log.d("RECIPE INGREDIENTS LOOP", INPUT);
					ingredListArray.add(INPUT);
					INPUT = scanner.next();
					if (INPUT.contains("_")) {
						Log.d("Looking for Directions", "FOUND THEM");
						break;
					}
				}
				// Grabbing Recipe Directions/Instructions
				String dirL[] = INPUT.substring(1, INPUT.length()).split("\\:");
				directList = Arrays.asList(dirL);

				while (scanner.hasNext()) {
					if ("0:E".matches(INPUT.trim())) {
						break;
					} else {
						INPUT = scanner.next();
					}
				}
				
				// Separating the Ingredients for Database import
				List<RecipeIngredient> riList = new ArrayList<RecipeIngredient>();
				for (String z : ingredListArray) {
					regex = "\\:";
					Pattern p = Pattern.compile(regex);
					String[] ami = p.split(z.toLowerCase());
					RecipeIngredient newIngredient = new RecipeIngredient(
							ami[0].trim(), ami[1].trim(), ami[2].trim(),
							ami[3].trim());
					riList.add(newIngredient);
				}

				List<RecipeDirection> dirList = new ArrayList<RecipeDirection>();
				for (String s : directList) {
					dirList.add(new RecipeDirection(s));
				}

				Recipe newRecipe = new Recipe(recipeNumber, title, auth,
						riList, dirList, linkedRecipe, boxAssignment, timeC,
						serveSize);
				recipeDatabase.addRecipe(newRecipe);

				recipeNumber++; // increment recipe numbering system

			} else {
				INPUT = scanner.next();
			}
		}

		scanner.close();
	}
}