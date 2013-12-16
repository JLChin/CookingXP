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

                while (scanner.hasNext()) {

                        if (INPUT.matches(bV)) {

                                String initVal = scanner.next(); // Starting Point Holder
                                String regex = "\\b  +";
                                String blank = "";
                                String title = "";
                                INPUT = initVal;

                                // Storing Recipe title
                                while (scanner.hasNext()) {
                                        
                                        if (INPUT.equals(bV)) {
                                                INPUT = scanner.next();
                                        } 
                                        else {
                                                title = INPUT;
                                                INPUT = scanner.next();
                                                break;
                                        }
                                }

                                ArrayList<String> ingredListArray = new ArrayList<String>();
                                List<String> directList = new ArrayList<String>();

                                while (scanner.hasNext()) {
                                        ingredListArray.add(INPUT);
                                        INPUT = scanner.next();
                                        if (INPUT.matches("\\b_.*")) {
                                                break;
                                        }
                                }                                                

                                directList.add(INPUT.substring(1, INPUT.length()));
                                while (scanner.hasNext()) {
                                        if (INPUT.matches(bV)){
                                                break;
                                        }
                                        else{
                                        INPUT = scanner.next();
                                        }
                                }

                                List<RecipeIngredient> riList = new ArrayList<RecipeIngredient>();
                                for (String z : ingredListArray) {
                                        regex = ":";
                                        Pattern p = Pattern.compile(regex);
                                        String[] ami = p.split(z);
                                        RecipeIngredient newIngredient = new RecipeIngredient(ami[0], ami[1], ami[2]);
                                        riList.add(newIngredient);
                                }

                                
                List<RecipeDirection> dirList = new ArrayList<RecipeDirection>();
                for (String s : directList){
                        dirList.add(new RecipeDirection(s));
                }

                Recipe newRecipe = new Recipe(title, riList, dirList);
                recipeDatabase.addRecipe(newRecipe);


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