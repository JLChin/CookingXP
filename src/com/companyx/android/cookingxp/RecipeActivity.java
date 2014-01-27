package com.companyx.android.cookingxp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import android.widget.Button;
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
 * TODO orientation, screen size, density etc
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
	
	/**
	 * Adds a formatted header to the parent ViewGroup.
	 * @param title the String to use as the header title.
	 * @param viewGroup the parent ViewGroup to add the header to.
	 * @param context the parent Context.
	 * @param dpiScalingFactor the hardware-dependent scaling factor to use for calculating pixel dimensions to use.
	 */
	static void addHeader(String title, ViewGroup viewGroup, Context context, float dpiScalingFactor) {
		// leading space
		viewGroup.addView(new View(context), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (dpiScalingFactor * 16 + 0.5f)));
		
		// TextView
		TextView header = new TextView(context);
		header.setText(title);
		header.setTypeface(null, Typeface.BOLD);
		int padding = (int) (dpiScalingFactor * 10 + 0.5f);
		header.setPadding(padding, padding, padding, padding);
		header.setTextSize(16 + 0.5f);
		header.setTextColor(Color.WHITE);
		header.setBackgroundColor(Color.BLACK);
		viewGroup.addView(header, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		
		addHorizontalSeparator(viewGroup, Color.BLACK, 3.0f, context, dpiScalingFactor);
	}
	
	/**
	 * Adds a horizontal divider View to the parent ViewGroup.
	 * @param viewGroup the parent ViewGroup to add the separator line to.
	 * @param color the color of the separator line.
	 * @param dpThickness the thickness of the separator line to be drawn, in density-independent pixels (dip).
	 * @param context the parent Context.
	 * @param dpiScalingFactor the hardware-dependent scaling factor to use for calculating pixel dimensions to use.
	 */
	static void addHorizontalSeparator(ViewGroup viewGroup, int color, float dpThickness, Context context, float dpiScalingFactor) {
		View separator = new View(context);
		separator.setBackgroundColor(color);
		viewGroup.addView(separator, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (dpiScalingFactor * dpThickness + 0.5f)));
	}
	
	/**
	 * Adds a formatted info line to the parent ViewGroup.
	 * @param info the String to add as the info.
	 * @param viewGroup the parent ViewGroup to add the info line to.
	 * @param context the parent Context.
	 * @param dpiScalingFactor the hardware-dependent scaling factor to use for calculating pixel dimensions to use.
	 */
	static void addInfoLine(String info, ViewGroup viewGroup, Context context, float dpiScalingFactor) {
		TextView tv = new TextView(context);
		tv.setText(info);
		tv.setTextSize(16 + 0.5f);
		tv.setTextColor(Color.GRAY);
		int padding = (int) (dpiScalingFactor * 2 + 0.5f);
		tv.setPadding(0, padding, 0, padding);
		viewGroup.addView(tv);
	}
	
	/**
	 * Adds a formatted text line to the parent ViewGroup.
	 * @param text the String to add as the text.
	 * @param viewGroup the parent ViewGroup to add the text line to.
	 * @param context the parent Context.
	 * @param dpiScalingFactor the hardware-dependent scaling factor to use for calculating pixel dimensions to use.
	 */
	static void addTextLine(String text, ViewGroup viewGroup, Context context, float dpiScalingFactor) {
		TextView tv = new TextView(context);
		tv.setText(text);
		tv.setTextSize(16 + 0.5f);
		int padding = (int) (dpiScalingFactor * 6 + 0.5f);
		tv.setPadding(0, padding, 0, padding);
		viewGroup.addView(tv);
		
		addHorizontalSeparator(viewGroup, Color.LTGRAY, 1.0f, context, dpiScalingFactor);
	}
	
	/**
	 * Returns a string containing the formatted hour and minute representation of the recipe cooking time.
	 * @param recipeTime the RecipeTime object containing the time information for this Recipe.
	 * @param context the parent Context.
	 * @return a string containing the formatted hour and minute representation of the recipe cooking time.
	 */
	static String getTime(RecipeTime recipeTime, Context context) {
		// retrieve total time
		short totalTimeInMin = (short) (recipeTime.prepTimeInMin + recipeTime.inactivePrepTimeInMin + recipeTime.cookTimeInMin);
		
		if (totalTimeInMin <= 0)
			return " --- ";
		
		// construct hours string
		short hours = (short) (totalTimeInMin / 60);
		String hoursStr = "";
		if (hours != 0) {
			if (hours == 1)
				hoursStr += hours + " " + context.getString(R.string.recipe_info_hour) + " ";
			else
				hoursStr += hours + " " + context.getString(R.string.recipe_info_hours) + " ";
		}
			
		return hoursStr + (totalTimeInMin % 60) + " " + context.getString(R.string.recipe_info_min);
	}
	
	/**
	 * Set up the Activity.
	 */
	private void initialize() {
		// GET RECIPE INFO
		recipeId = getIntent().getIntExtra("recipeId", -1);
		recipe = recipeDatabase.findRecipeById(recipeId);
		
		// RECIPE NAME
		textViewName = (TextView) findViewById(R.id.textview_recipe_name);
		textViewName.setText(recipe.name);
		textViewName.setTextSize(18 + 0.5f);
		
		// RECIPE SUBTITLE
		textViewSubtitle = (TextView) findViewById(R.id.textview_recipe_subtitle);
		textViewSubtitle.setText(recipe.author);
		textViewSubtitle.setTextColor(Color.GRAY);
		textViewSubtitle.setTextSize(14 + 0.5f);
		
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
					
					sharedPrefEditor.putString("SERIALIZED_FAVORITES", recipeDatabase.getSerializedFavorites()).commit();
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
					sharedPrefEditor.putString("SERIALIZED_SHOPPING_LIST", recipeDatabase.getSerializedShoppingList()).commit();
					
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
		addHeader(getString(R.string.recipe_header_info), layoutBody, this, dpiScalingFactor);
		LinearLayout llInfoContainer = new LinearLayout(this);
		
		// left side: info
		LinearLayout llInfo = new LinearLayout(this);
		llInfo.setOrientation(LinearLayout.VERTICAL);
		
		// preparation time
		addInfoLine(getString(R.string.recipe_info_prep_time) + ": " + getTime(recipe.recipeTime, this), llInfo, this, dpiScalingFactor);
		
		// servings
		addInfoLine(getString(R.string.recipe_info_servings) + ": " + recipe.numOfServings, llInfo, this, dpiScalingFactor);
		
		// right side: boxes
		LinearLayout llBoxes = new LinearLayout(this);
		llBoxes.setOrientation(LinearLayout.VERTICAL);
		
		List<Short> boxes = recipe.boxes;
		for (short boxId : boxes) {
			ImageView iv = new ImageView(this);
			iv.setImageResource(gameData.findBoxById(boxId).unlockedImgRes);
			llBoxes.addView(iv);
		}
		
		llInfoContainer.addView(llInfo, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
		llInfoContainer.addView(llBoxes, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layoutBody.addView(llInfoContainer, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		// INGREDIENTS
		addHeader(getString(R.string.recipe_header_ingredients), layoutBody, this, dpiScalingFactor);
		for (RecipeIngredient ri : recipe.ingredients) {
			String s = ri.amount + " " + ri.measurement + " " + ri.ingredientName;
			
			// ingredient notes
			String notes = ri.notes;
			if (notes != null && notes.length() > 1)
				s += " (" + notes + ")";
			
			addTextLine(s, layoutBody, this, dpiScalingFactor);
		}
		
		// DIRECTIONS
		addHeader(getString(R.string.recipe_header_directions), layoutBody, this, dpiScalingFactor);
		if (recipe.directions != null) {
			for (RecipeDirection rd : recipe.directions)
				addTextLine(rd.direction, layoutBody, this, dpiScalingFactor);
		}
		
		// "I COOKED IT" TODO for debugging
		Button buttonICookedIt = new Button(this);
		buttonICookedIt.setText("I cooked it, Scout's honor.");
		buttonICookedIt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				for (short boxId : recipe.boxes)
					gameData.pingBox(boxId);
			}
		});
		layoutBody.addView(buttonICookedIt);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		initialize();
	}
}
