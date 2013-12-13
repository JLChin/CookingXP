package com.companyx.android.appx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.companyx.android.appx.RecipeDatabase.Recipe;
import com.companyx.android.appx.RecipeDatabase.RecipeDirection;
import com.companyx.android.appx.RecipeDatabase.RecipeIngredient;

@SuppressWarnings("unused")
// Recipe Parser
// @author Adam Hackbarth <adam.hackbarth@gmail.com>
public class RecipeParser {
	InputStream inputStream;
	RecipeDatabase recipeDatabase;

	// constructor
	RecipeParser(InputStream inputStream, RecipeDatabase recipeDatabase) {
		this.inputStream = inputStream;
		this.recipeDatabase = recipeDatabase;
	}

	public void loadData() {

		/* Loading Raw Data from File Location into Scanner */
		Scanner scanner = new Scanner(inputStream);
		scanner.useDelimiter(System.getProperty("line.separator"));

		String BV = "==============="; // BreakValue - New Entry DISCOVERY Text
										// Initial BV for first import
		int LineCount = 1; // For testing purposes

		/* Scan until new recipe block discovered */
		String C_line = scanner.next();

		while (scanner.hasNext()) {

			if (C_line.matches(BV)) {

				String InitVal = scanner.next(); // Starting Point Holder
				String REGEX = "\\b  +";
				String Blank = "";
				String INPUT = InitVal;
				String title = "";

				ArrayList<String> titleListArray = new ArrayList<String>();

				// Finding and Storing Recipe title
				while (scanner.hasNext()) {
					if (INPUT.matches(Blank)) {
						INPUT = scanner.next();
					} else {
						break;
					}
				}
				if (INPUT.equals(Blank)) {
					System.out.println("Failed");
				} else {
					while (scanner.hasNext()) {
						if (INPUT.equals(Blank)) {
							break;
						}
						if (INPUT.equals(BV)) {
							break;
						} else {
							titleListArray.add(INPUT);
							INPUT = scanner.next();
						}
					}
				}

				/*
				 * Combine titleListArray into a Single title String
				 */
				for (String z : titleListArray) {
					title = title + z;
				}

				/*
				 * Looking for the Header to indicate Ingredients List Will
				 * Follow
				 */
				while (scanner.hasNext()) {
					if (INPUT.matches(".*\\b(?i)Ingredient\\b.*")) {
						// System.out.println("ACHIEVEMENT UNLOCK - Text Found");
						break;
					} else {
						INPUT = scanner.next();
						// System.out.println("Still Looking.");
					}
				}
				INPUT = scanner.next(); // Removing Current header Text ---

				/*
				 * LISTARRAY -IngredListArray - STORE INGRED/MEASURE/AMOUNT
				 * 
				 * TO DO REMOVE FIRST LINE OF INGREDIANT LIST Via Pattern
				 */

				ArrayList<String> IngredListArray = new ArrayList<String>();

				while (scanner.hasNext()) {
					INPUT = scanner.next();
					if (INPUT.equals(Blank)) {
						break;
					} else {
						IngredListArray.add(INPUT);
					}

				}

				/*
				 * SECTION INGREDIENTS LIST
				 * 
				 * Order of Input - AMOUNT - MEASURE - INGREDIENT (AMI)
				 * 
				 * TO DO Special Cases (IE. Null Measure, Null Amount) (eg. One
				 * Egg)
				 * 
				 * Change Amount Strings to Automatically replace -
				 */

				List<RecipeIngredient> riList = new ArrayList<RecipeIngredient>();
				for (String z : IngredListArray) {
					
					
					Pattern p = Pattern.compile(REGEX);
					// Add code for special Cases Here
					String[] AMI = p.split(z);
					RecipeIngredient newIngredient = new RecipeIngredient(AMI[0], AMI[1], AMI[2]);
					riList.add(newIngredient);
					/*
					for (String s : AMI) {
						// Taking a look at the AMI outputs
						System.out.println(LineCount + " " + s.trim());
						LineCount++;
					} */
					// System.out.println(z);
				}

				/*
				 * Directions List Array - Storing Line by Line Instructions
				 * 
				 * TO DO Add locator for Direction = Instructions Instructions
				 * by Spacing Directions by complete Sentence
				 */

				List<RecipeDirection> directListArray = new ArrayList<RecipeDirection>();
				// STORE DIRECTIONS or Null to DIRECTIONS LIST

				while (scanner.hasNext()) {

					// If The next line is new Entry Text
					if (INPUT.equals(BV)) {
						// CHECK If VALID parse T/F
						/*
						 * DATABASE IMPORT String title - title String[] AMI -
						 * Amount Measure Ingredient ArrayList<String>
						 * DirectListArray - Directions
						 */
						Recipe newRecipe = new Recipe(title, riList, directListArray);
						recipeDatabase.addRecipe(newRecipe);
						break;
					} else {
						if (INPUT.equals(Blank)) {
							INPUT = scanner.next();
						} else {
							directListArray.add(new RecipeDirection(INPUT));
							INPUT = scanner.next();
						}
					}
				}

				/*
				 * Placeholder for TIME parsing addition
				 */

				// TESTING title - INGRED LIST - DIRECTIONS LIST

				// System.out.println("");
				// System.out.println(title);

				// title List Test
				/*
				 * for (String z : titleListArray) { System.err.println(z);
				 * System.out.println(""); }
				 */

				// Test the List (Amount/Measure/Ingredients) Values
				/*
				 * for (String z : IngredListArray) { System.err.println(z); }
				 * 
				 * // Test the List (Directions) Values /*for (String z :
				 * DirectListArray) { System.err.println(z); }
				 */

			} else {
				while (scanner.hasNext()) {
					if (C_line.equals(BV)) {
						break;
					}
					C_line = scanner.next();
					// System.out.println(LineCount +"        Line: " +C_line);
					// LineCount++;
				}
			}
		}

		// Testing Next 2 Output lines to see where we are in the File being
		// parsed
		// System.out.println(scanner.next());
		// System.out.println(scanner.next());

		scanner.close();
	}
}