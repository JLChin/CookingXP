package com.companyx.android.appx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.companyx.android.appx.RecipeDatabase.Recipe;
import com.companyx.android.appx.RecipeDatabase.RecipeDirection;
import com.companyx.android.appx.RecipeDatabase.RecipeIngredient;
import com.companyx.android.appx.RecipeDatabase.RecipeTime;

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

		String bV = "\\b\\d:\\D"; // BreakValue - New Entry DISCOVERY Text
		String INPUT = scanner.next();
		String keyLine = "";
		int recipeNumber = 0;

		while (scanner.hasNext()) {

			if (INPUT.matches(bV)) {
				keyLine = INPUT; // saving the recipe key line
				String initVal = scanner.next(); // Starting Point Holder
				String regex = "\\b  +";
				String blank = "";
				String title = "";
				INPUT = initVal;
				// Difficulty Placeholder.
				byte diff = 0;

				// Storing Recipe title
				while (scanner.hasNext()) {

					if (INPUT.equals(bV)) {
						INPUT = scanner.next();
					} else {
						title = INPUT;
						INPUT = scanner.next();
						break;
					}
				}
				// Time Split into Int - then Short into the RecipeTime
				String timeL[] = INPUT.split("\\:");
				int pr = Integer.valueOf(timeL[0].trim());
				int ipr = Integer.valueOf(timeL[1].trim());
				int c = Integer.valueOf(timeL[2].trim());
				// short pr1 = (short)pr;
				// short ipr1 = (short)ipr;
				// short c1 = (short)c;

				// UNCOMMENT THIS
				RecipeTime timeC = new RecipeTime((short)pr, (short)ipr, (short)c);

				// Serving size byte (serveSize)
				int q = Integer.valueOf(timeL[3].trim());
				byte serveSize = (byte) q;

				INPUT = scanner.next();

				ArrayList<String> ingredListArray = new ArrayList<String>();
				List<String> directList = new ArrayList<String>();

				while (scanner.hasNext()) {
					ingredListArray.add(INPUT);
					INPUT = scanner.next();
					if (INPUT.matches("\\b_.*")) {
						break;
					}
				}

				// Adding Directions Here
				String dirL[] = INPUT.substring(1, INPUT.length()).split("\\:");
				directList = Arrays.asList(dirL);

				while (scanner.hasNext()) {
					if (INPUT.matches(bV)) {
						break;
					} else {
						INPUT = scanner.next();
					}
				}

				List<RecipeIngredient> riList = new
				ArrayList<RecipeIngredient>();
				for (String z : ingredListArray) {
					regex = "\\b:"; // Small Expression Change done HERE
					Pattern p = Pattern.compile(regex);
					String[] ami = p.split(z.toLowerCase());
					RecipeIngredient newIngredient = new
					RecipeIngredient(ami[0], ami[1], ami[2], ami[3]);
					riList.add(newIngredient);
				}

				List<RecipeDirection> dirList = new
				ArrayList<RecipeDirection>();
				for (String s : directList){
					dirList.add(new RecipeDirection(s));
				}

				Recipe newRecipe = new Recipe(recipeNumber, title, riList, dirList, timeC, diff); // serveSize,
				recipeDatabase.addRecipe(newRecipe);

				recipeNumber++; // increment recipe numbering system

			} else {
				while (scanner.hasNext()) {
					if (INPUT.equals(bV)) {
						break;
					}
					INPUT = scanner.next();

				}
			}
		}

		scanner.close();
	}
}