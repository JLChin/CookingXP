package com.companyx.android.cookingxp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.companyx.android.cookingxp.RecipeDatabase.Recipe;
import com.companyx.android.cookingxp.RecipeDatabase.RecipeDirection;
import com.companyx.android.cookingxp.RecipeDatabase.RecipeIngredient;
import com.companyx.android.cookingxp.RecipeDatabase.RecipeTime;

/**
 * Recipe Activity
 * 
 *  @author James Chin <jameslchin@gmail.com>
 */
public class RecipeActivity extends BaseActivity {
	// DEFAULT SETTINGS
	private static final byte MAX_QUANTITY = 8;
	
	// VIEW HOLDERS
	private LinearLayout layoutBody;
	private TextView textViewName;
	private TextView textViewSubtitle;
	private ImageButton buttonFavorite;
	private Spinner spinnerShoppingList;
	
	// STATE VARIABLES
	private int recipeId;
	private Recipe recipe;
	
	// SYSTEM
	private GameData gameData;
	private RecipeDatabase recipeDatabase;
	private SharedPreferences.Editor sharedPrefEditor;
	private float dpiScalingFactor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		initialize();
	}
	
	private void initialize() {
		// LOAD SYSTEM VARIABLES
		recipeDatabase = RecipeDatabase.getInstance(this);
		gameData = GameData.getInstance(this);
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		sharedPrefEditor = sharedPref.edit();
		dpiScalingFactor = getResources().getDisplayMetrics().density;
		
		// GET RECIPE INFO
		recipeId = getIntent().getIntExtra("recipeId", -1);
		recipe = recipeDatabase.findRecipeById(recipeId);
		
		// RECIPE NAME
		textViewName = (TextView) findViewById(R.id.textview_recipe_name);
		textViewName.setText(recipe.name);
		
		// RECIPE SUBTITLE
		textViewSubtitle = (TextView) findViewById(R.id.textview_recipe_subtitle);
		textViewSubtitle.setText(recipe.author);
		textViewSubtitle.setTextColor(Color.GRAY);
		
		// FAVORITE BUTTON
		buttonFavorite = (ImageButton) findViewById(R.id.imagebutton_recipe_favorite);
		if (recipeDatabase.isFavorite(recipeId)) {
			buttonFavorite.setBackgroundResource(R.drawable.button_background_amber);
		} else {
			buttonFavorite.setBackgroundResource(R.drawable.button_background_gray);
		}
		buttonFavorite.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				synchronized(recipeDatabase) {
					if (recipeDatabase.isFavorite(recipeId)) {
						recipeDatabase.removeFavorite(recipeId);
						buttonFavorite.setBackgroundResource(R.drawable.button_background_gray);
						Toast.makeText(getApplicationContext(), getString(R.string.recipe_favorites_removed) + " " + recipe.name + " " + getString(R.string.recipe_favorites_from_favorites), Toast.LENGTH_SHORT).show();
					} else {
						recipeDatabase.addFavorite(recipeId);
						buttonFavorite.setBackgroundResource(R.drawable.button_background_amber);
						Toast.makeText(getApplicationContext(), getString(R.string.recipe_favorites_added) + " " + recipe.name + " " + getString(R.string.recipe_favorites_to_favorites), Toast.LENGTH_SHORT).show();
					}
					
					sharedPrefEditor.putString("SERIALIZED_FAVORITES", recipeDatabase.getSerializedFavorites());
					sharedPrefEditor.commit();
				}
			}
		});
		
		// SHOPPING LIST SPINNER
		spinnerShoppingList = (Spinner) findViewById(R.id.spinner_recipe_shopping_list);
		List<String> spinnerChoices = new ArrayList<String>();
		for (byte i = 0; i <= MAX_QUANTITY; i++)
			spinnerChoices.add(" " + String.valueOf(i));
		ArrayAdapter<String> shoppingListAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, spinnerChoices);
		shoppingListAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		spinnerShoppingList.setAdapter(shoppingListAdapter);
		spinnerShoppingList.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				synchronized(recipeDatabase) {
					byte currentQty = recipeDatabase.getQuantity(recipeId);
					
					recipeDatabase.updateQuantity(recipeId, (byte) position); 
					sharedPrefEditor.putString("SERIALIZED_SHOPPING_LIST", recipeDatabase.getSerializedShoppingList());
					sharedPrefEditor.commit();
					
					// QUANTITY CHANGE NOTIFICATION
					if (position > currentQty)
						Toast.makeText(getApplicationContext(), getString(R.string.recipe_shopping_list_added) + " " + (position - currentQty) + " " + recipe.name + " " + getString(R.string.recipe_shopping_list_to_shopping_list), Toast.LENGTH_SHORT).show();
					else if (position < currentQty)
						Toast.makeText(getApplicationContext(), getString(R.string.recipe_shopping_list_removed) + " " + (currentQty - position) + " " + recipe.name + " " + getString(R.string.recipe_shopping_list_from_shopping_list), Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spinnerShoppingList.setSelection(recipeDatabase.getQuantity(recipeId), true);
		
		// RECIPE BODY
		layoutBody = (LinearLayout) findViewById(R.id.layout_recipe_body);
		
		// INFORMATION
		addHeader(getString(R.string.recipe_header_info), layoutBody);
		LinearLayout llInfoContainer = new LinearLayout(this);
		llInfoContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		// left side: info
		LinearLayout llInfo = new LinearLayout(this);
		llInfo.setOrientation(LinearLayout.VERTICAL);
		llInfo.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
		
		// preparation time
		addInfoLine(getString(R.string.recipe_info_prep_time) + ": " + getTime(recipe.recipeTime), llInfo);
		
		// servings
		addInfoLine(getString(R.string.recipe_info_servings) + ": " + recipe.numOfServings, llInfo);
		
		// right side: boxes
		LinearLayout llBoxes = new LinearLayout(this);
		llBoxes.setOrientation(LinearLayout.VERTICAL);
		llBoxes.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		List<Short> boxes = recipe.boxes;
		for (short boxId : boxes) {
			ImageView iv = new ImageView(this);
			iv.setImageResource(gameData.findBoxById(boxId).unlockedImgRes);
			llBoxes.addView(iv);
		}
		
		llInfoContainer.addView(llInfo);
		llInfoContainer.addView(llBoxes);
		layoutBody.addView(llInfoContainer);
		
		// INGREDIENTS
		addHeader(getString(R.string.recipe_header_ingredients), layoutBody);
		for (RecipeIngredient ri : recipe.ingredients) {
			String s = ri.amount + " " + ri.measurement + " " + ri.ingredientName;
			
			// ingredient notes
			String notes = ri.notes;
			if (notes != null && notes.length() > 1)
				s += " (" + notes + ")";
			
			addTextLine(s, layoutBody);
		}
		
		// DIRECTIONS
		addHeader(getString(R.string.recipe_header_directions), layoutBody);
		if (recipe.directions != null) {
			for (RecipeDirection rd : recipe.directions)
				addTextLine(rd.direction, layoutBody);
		}
	}
	
	/**
	 * Helper function to add a formatted info line to the parent ViewGroup.
	 * @param text the String to add as the info.
	 * @param viewGroup the parent ViewGroup to add the text line to.
	 */
	private void addInfoLine(String info, ViewGroup viewGroup) {
		TextView tv = new TextView(this);
		tv.setText(info);
		tv.setTextSize(14 + 0.5f);
		tv.setTextColor(Color.GRAY);
		int padding = (int) (dpiScalingFactor * 2 + 0.5f);
		tv.setPadding(0, padding, 0, padding);
		viewGroup.addView(tv);
	}
	
	/**
	 * Helper function to add a formatted text line to the parent ViewGroup.
	 * @param text the String to add as the text.
	 * @param viewGroup the parent ViewGroup to add the text line to.
	 */
	private void addTextLine(String text, ViewGroup viewGroup) {
		TextView tv = new TextView(this);
		tv.setText(text);
		int padding = (int) (dpiScalingFactor * 6 + 0.5f);
		tv.setPadding(0, padding, 0, padding);
		viewGroup.addView(tv);
		
		addHorizontalSeparator(viewGroup, Color.LTGRAY, 1.0f);
	}
	
	/**
	 * Helper function to add a formatted header to the parent ViewGroup.
	 * @param title the String to use as the header title.
	 * @param viewGroup the parent ViewGroup to add the header to.
	 */
	private void addHeader(String title, ViewGroup viewGroup) {
		// leading space
		viewGroup.addView(new View(this), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (dpiScalingFactor * 16 + 0.5f)));
		
		// TextView
		TextView header = new TextView(this);
		header.setText(title);
		header.setTypeface(null, Typeface.BOLD);
		header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		int padding = (int) (dpiScalingFactor * 10 + 0.5f);
		header.setPadding(padding, padding, padding, padding);
		header.setTextSize(16 + 0.5f);
		header.setTextColor(Color.WHITE);
		header.setBackgroundColor(Color.BLACK);
		viewGroup.addView(header);
		
		addHorizontalSeparator(viewGroup, Color.BLACK, 3.0f);
	}
	
	/**
	 * Helper function to add a horizontal divider View to the parent ViewGroup.
	 * @param viewGroup the parent ViewGroup to add the separator line to.
	 * @param color the color of the separator line.
	 * @param dpThickness the thickness of the separator line to be drawn, in density-independent pixels (dip).
	 */
	private void addHorizontalSeparator(ViewGroup viewGroup, int color, float dpThickness) {
		View separator = new View(this);
		separator.setBackgroundColor(color);
		viewGroup.addView(separator, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (dpiScalingFactor * dpThickness + 0.5f)));
	}
	
	/**
	 * Helper function that generates a string containing the formatted hour and minute representation of the recipe cooking time.
	 * @param timeRequiredInMin the time required in minutes for the Recipe.
	 * @return a string containing the formatted hour and minute representation of the recipe cooking time.
	 */
	private String getTime(RecipeTime recipeTime) {
		// retrieve total time
		short totalTimeInMin = (short) (recipeTime.prepTimeInMin + recipeTime.inactivePrepTimeInMin + recipeTime.cookTimeInMin);
		
		if (totalTimeInMin <= 0)
			return " --- ";
		
		// construct hours string
		short hours = (short) (totalTimeInMin / 60);
		String hoursStr = "";
		if (hours != 0) {
			if (hours == 1)
				hoursStr += hours + " " + getString(R.string.recipe_info_hour) + " ";
			else
				hoursStr += hours + " " + getString(R.string.recipe_info_hours) + " ";
		}
			
		return hoursStr + (totalTimeInMin % 60) + " " + getString(R.string.recipe_info_min);
	}
}
