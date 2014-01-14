package com.companyx.android.cookingxp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.companyx.android.cookingxp.GameData.Box;
import com.companyx.android.cookingxp.GameData.BoxHolder;
import com.companyx.android.cookingxp.GameData.Tree;
import com.companyx.android.cookingxp.RecipeDatabase.Recipe;

/**
 * TreeActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class TreeActivity extends BaseActivity {
	// CONSTANTS
	public static final float DEFAULT_IMAGE_SPACING_RATIO = 0.1f; // ratio of image spacing to screen width
	
	// VIEW HOLDERS
	private LinearLayout layoutTree;
	
	// STATE VARIABLES
	private List<Tree> treeList;
	private Map<View, PopupWindow> openPopups;
	
	// SYSTEM
	private GameData gameData;
	private RecipeDatabase recipeDatabase;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trees);
		
		initialize();
		constructTree(treeList.get(0));
	}
	
	private void initialize() {
		recipeDatabase = RecipeDatabase.getInstance(this);
		gameData = GameData.getInstance(this);
		treeList = gameData.getTrees();
		
		openPopups = new HashMap<View, PopupWindow>();
		
		layoutTree = (LinearLayout) findViewById(R.id.layout_tree);
	}
	
	/**
	 * Constructs Tree layout from GameData info.
	 * @param tree the Tree object to construct the layout for.
	 */
	private void constructTree(Tree tree) {
		// calculate image spacing based on screen size and current orientation
		@SuppressWarnings("deprecation")
		float screenWidthInPixels = getWindowManager().getDefaultDisplay().getWidth();
		int imgSpacingInPixels = (int) (DEFAULT_IMAGE_SPACING_RATIO * screenWidthInPixels);
		
		Log.wtf("JAMES", String.valueOf(imgSpacingInPixels));
		
		for (int tier = 0; tier < tree.boxHolderMatrix.size(); tier++) {
			// tier container
			RelativeLayout rl = new RelativeLayout(this);
			RelativeLayout.LayoutParams paramsRL = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			rl.setLayoutParams(paramsRL);
			
			// ImageView container
			LinearLayout ll = new LinearLayout(this); // default horizontal orientation
			RelativeLayout.LayoutParams paramsLL = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsLL.addRule(RelativeLayout.CENTER_IN_PARENT);
			ll.setLayoutParams(paramsLL);
			
			for (BoxHolder bh : tree.boxHolderMatrix.get(tier)) {
				// retrieve Box
				Box box = gameData.findBoxById(bh.boxId);
				
				// set ImageView
				ImageView imgView = new ImageView(this);
				if (bh.isUnlocked()) {
					if (bh.isActivated())
						imgView.setImageResource(box.activatedImgRes);
					else
						imgView.setImageResource(box.unlockedImgRes);
				} else
					imgView.setImageResource(box.lockedImgRes);
				
				// cache Box in ImageView to retrieve later
				imgView.setTag(box);
				
				// attach onClick PopupWindow to ImageView
				imgView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						showPopup(view);
					}
				});
				
				ll.addView(imgView);
				
				// vertical break between ImgViews
				ll.addView(new View(this), new LinearLayout.LayoutParams(imgSpacingInPixels, LinearLayout.LayoutParams.MATCH_PARENT));
			}
			
			// remove trailing break
			ll.removeViewAt(ll.getChildCount() - 1);
			
			// ImageViews constructed, add to tier container
			rl.addView(ll);
			
			// tier constructed, add to layout
			layoutTree.addView(rl);
			
			// horizontal break between tiers
			layoutTree.addView(new View(this), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgSpacingInPixels));
		}
	}
	
	/**
	 * Construct and display PopupWindow.
	 * @param view the invoked View to display a PopupWindow for.
	 */
	private void showPopup(View view) {
		// if already open
		if (openPopups.containsKey(view)) {
			openPopups.remove(view).dismiss();
			return;
		}
		
		// manage currently open PopupWindows (close all)
		for (View v : openPopups.keySet())
			openPopups.remove(v).dismiss();
		
		// retrieve Box
		Box box = (Box) view.getTag();
		
		// construct PopupWindow
		PopupWindow popupWindow = new PopupWindow(getLayoutInflater().inflate(R.layout.box_popup, null), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		// View container
		LinearLayout layoutBoxPopup = (LinearLayout) popupWindow.getContentView().findViewById(R.id.layout_box_popup);
		layoutBoxPopup.setBackgroundColor(Color.BLACK);
		
		// BOX TITLE
		TextView tvTitle = new TextView(this);
		tvTitle.setText(getString(box.titleStrRes));
		tvTitle.setTextColor(Color.LTGRAY);
		
		// MODIFIER
		TextView tvModifier = new TextView(this);
		tvModifier.setText("+10 Awesomeness"); // TODO
		tvModifier.setTextColor(Color.GREEN);
		tvModifier.setGravity(Gravity.RIGHT);
		RelativeLayout.LayoutParams paramsTV = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsTV.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		tvModifier.setLayoutParams(paramsTV);
		
		// container for title, modifier
		RelativeLayout rlTitle = new RelativeLayout(this);
		RelativeLayout.LayoutParams paramsRL = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlTitle.setLayoutParams(paramsRL);
		rlTitle.addView(tvTitle);
		rlTitle.addView(tvModifier);
		layoutBoxPopup.addView(rlTitle);
		
		// separator
		View separator = new View(this);
		separator.setBackgroundColor(Color.LTGRAY);
		layoutBoxPopup.addView(separator, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
		
		// BOX DESCRIPTION
		TextView tvDescription = new TextView(this);
		tvDescription.setText(getString(box.descStrRes));
		tvDescription.setTextColor(Color.LTGRAY);
		layoutBoxPopup.addView(tvDescription);
		
		// break
		layoutBoxPopup.addView(new View(this), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
		
		// APPLICABLE RECIPES
		for (Recipe recipe : recipeDatabase.getRecipesByBox(box.boxId)) {
			TextView tvRecipe = new TextView(this);
			tvRecipe.setText(recipe.name);
			tvRecipe.setTextColor(Color.WHITE);
			
			// cache Recipe in TextView to retrieve later
			tvRecipe.setTag(recipe);
			
			// attach OnClickListener to go to Recipe
			tvRecipe.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// retrieve Recipe
					Recipe recipe = (Recipe) view.getTag();
					
					// go to Recipe
					Intent intent = new Intent(TreeActivity.this, RecipeActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					intent.putExtra("recipeId", recipe.recipeId);
					startActivity(intent);
				}	
			});
			
			layoutBoxPopup.addView(tvRecipe);
		}
		
		// break
		layoutBoxPopup.addView(new View(this), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
		
		// WEAPON TYPE!!!
		TextView tvWeapon = new TextView(this);
		tvWeapon.setText("Two-Handed Weapon"); // TODO
		tvWeapon.setTextColor(Color.CYAN);
		tvWeapon.setGravity(Gravity.RIGHT);
		layoutBoxPopup.addView(tvWeapon);
		
		// PopupWindow constructed, attach to view
		popupWindow.showAsDropDown(view);
		
		openPopups.put(view, popupWindow);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
}
