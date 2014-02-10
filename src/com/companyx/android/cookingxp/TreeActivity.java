package com.companyx.android.cookingxp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
	public static final int DEFAULT_EDGE_STROKE_WIDTH = 10;
	
	// VIEW HOLDERS
	private RelativeLayout layoutTree;
	private Spinner spinnerTree;
	
	// STATE VARIABLES
	private List<Tree> treeList; // all Trees currently available to the user
	private Map<View, PopupWindow> openPopups;
	private List<BoxHolder> pendingEdgeBHs; // path edges to draw, waiting for layout dimensions
	
	// SYSTEM
	OnGlobalLayoutListener listenerOGL;
	
	/**
	 * Custom View class for drawing the path edges between Boxes.
	 */
	static class EdgeView extends View {
		Paint paint;
		float startX, startY, stopX, stopY;

		public EdgeView(Context context, float startX, float startY, float stopX, float stopY, int strokeWidth) {
			super(context);
			paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(strokeWidth);
			
			this.startX = startX;
			this.startY = startY;
			this.stopX = stopX;
			this.stopY = stopY;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			canvas.drawLine(startX, startY, stopX, stopY, paint);
		}
	}
	
	/**
	 * Constructs Tree layout from GameData info.
	 * @param tree the Tree object to construct the layout for.
	 * @param layout the LinearLayout to add the Tree to.
	 */
	private void constructTree(Tree tree, LinearLayout layout) {
		// calculate image spacing based on screen size and current orientation
		@SuppressWarnings("deprecation")
		float screenWidthInPixels = getWindowManager().getDefaultDisplay().getWidth();
		int imgSpacingInPixels = (int) (DEFAULT_IMAGE_SPACING_RATIO * screenWidthInPixels + 0.5f);
		
		pendingEdgeBHs = new ArrayList<BoxHolder>();
		
		for (int tier = 0; tier < tree.boxHolderMatrix.size(); tier++) {
			// horizontal break between tiers
			layout.addView(new View(this), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgSpacingInPixels));
						
			// tier container
			RelativeLayout rl = new RelativeLayout(this);
			
			// ImageView container
			LinearLayout ll = new LinearLayout(this); // default horizontal orientation
			RelativeLayout.LayoutParams paramsLL = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsLL.addRule(RelativeLayout.CENTER_IN_PARENT);
			
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
				
				// cache ImageView in BoxHolder to use dimensions for drawing edges
				bh.imageView = imgView;
				
				// add ImageView to container
				ll.addView(imgView);
				
				// vertical break between ImgViews
				ll.addView(new View(this), new LinearLayout.LayoutParams(imgSpacingInPixels, LinearLayout.LayoutParams.MATCH_PARENT));
				
				// if there are pending edges to be drawn, add to List
				if (!bh.incomingEdges.isEmpty())
					pendingEdgeBHs.add(bh);
			}
			
			// remove trailing break
			ll.removeViewAt(ll.getChildCount() - 1);
			
			// ImageViews constructed, add to tier container
			rl.addView(ll, paramsLL);
			
			// tier constructed, add to layout
			layout.addView(rl, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		}
	}
	
	/**
	 * Close all open PopupWindows.
	 */
	private void dismissPopups() {
		for (View v : openPopups.keySet())
			openPopups.remove(v).dismiss();
	}
	
	/**
	 * Draws the path edges connecting the Boxes on the Tree.
	 * NOTE: Y coordinate on screen goes top-->down.
	 * This is called only once, after the ImageViews have been given layout dimensions.
	 */
	@SuppressWarnings("deprecation")
	private void drawEdges() {
		// remove listener, no longer needed
		layoutTree.getViewTreeObserver().removeGlobalOnLayoutListener(listenerOGL);
		
		for (BoxHolder bh : pendingEdgeBHs) {
			for (BoxHolder incomingBH : bh.incomingEdges) {
				// get ImageView locations in Window
				int[] imageViewStartXY = {0, 0};
				int[] imageViewEndXY = {0, 0};
				incomingBH.imageView.getLocationInWindow(imageViewStartXY);
				bh.imageView.getLocationInWindow(imageViewEndXY);
				
				// calculate dimensions in pixels
				int imageViewWidth = incomingBH.imageView.getWidth();
				int imageViewMidWidth = imageViewWidth / 2;
				int imageViewHeight = incomingBH.imageView.getHeight();
				int edgeViewHeight = imageViewEndXY[1] - imageViewStartXY[1] - imageViewHeight;
				int edgeViewWidth = edgeViewHeight; // currently the ImageView spacing width scales 1:1 with height
				
				// absolute screen location coordinates are offset by XML padding - Android bug?
				int adjustedImageViewStartX = imageViewStartXY[0] - (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
				int adjustedImageViewStartY = imageViewStartXY[1] - (int) getResources().getDimension(R.dimen.activity_vertical_margin);
				
				// EdgeView draw parameters
				int startX = 0;
				int startY = 0;
				int endX = 0;
				int endY = 0;
				int leftMargin = 0;
				int topMargin = 0;
				int strokeWidth = DEFAULT_EDGE_STROKE_WIDTH;
				
				// if current ImageView lines up with incoming ImageView, they are on the same axis
				if (bh.imageView.getLeft() == incomingBH.imageView.getLeft()) {
					// bottom middle of incoming ImageView to top middle of current ImageView
					startX = imageViewMidWidth;
					startY = 0;
					endX = imageViewMidWidth;
					endY = edgeViewHeight;
					leftMargin = adjustedImageViewStartX;
					topMargin = adjustedImageViewStartY;
					strokeWidth *= 2; // diagonal strokes are thicker for some reason TODO
				} else if (bh.imageView.getLeft() > incomingBH.imageView.getLeft()) {
					// lower right corner of incoming ImageView to upper left corner of current ImageView
					startX = 0;
					startY = 0;
					endX = edgeViewWidth;
					endY = edgeViewHeight;
					leftMargin = adjustedImageViewStartX + imageViewWidth;
					topMargin = adjustedImageViewStartY;
				} else {
					// lower left corner of incoming ImageView to upper right corner of current ImageView
					startX = edgeViewWidth;
					startY = 0;
					endX = 0;
					endY = edgeViewHeight;
					leftMargin = adjustedImageViewStartX;
					topMargin = adjustedImageViewStartY;
				}
				
				RelativeLayout.LayoutParams paramsEV = new RelativeLayout.LayoutParams(edgeViewWidth, edgeViewHeight);
				paramsEV.leftMargin = leftMargin;
				paramsEV.topMargin = topMargin;
				
				layoutTree.addView(new EdgeView(this, startX, startY, endX, endY, strokeWidth), paramsEV);
			}
		}
	}
	
	/**
	 * Set up the Tree selection Spinner.
	 * @param layout the LinearLayout to add the Spinner to.
	 */
	private void initializeSpinner(LinearLayout layout) {
		spinnerTree = new Spinner(this);
		
		// add selections
		List<String> treeTitles = new ArrayList<String>();
		for (Tree tree : treeList)
			treeTitles.add(tree.getName());
		spinnerTree.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, treeTitles));
		spinnerTree.setSelection(sharedPref.getInt("DEFAULT_TREE_SELECTION", 0));
		
		// attach listener
		spinnerTree.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// position == treeId
				sharedPrefEditor.putInt("DEFAULT_TREE_SELECTION", position).commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		layout.addView(spinnerTree);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trees);
		
		openPopups = new HashMap<View, PopupWindow>();
		layoutTree = (RelativeLayout) findViewById(R.id.layout_tree);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		
		layoutTree.removeAllViews();
		dismissPopups();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		refreshLayout();
	}

	/**
	 * Refresh the screen layout.
	 * This is called onStart() and handles any game updates since the user left the current Activity.
	 */
	private void refreshLayout() {
		// draw edges when ImageViews are given layout dimensions
		listenerOGL = new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				drawEdges();
			}
		};
		layoutTree.getViewTreeObserver().addOnGlobalLayoutListener(listenerOGL);

		// vertical LinearLayout container
		LinearLayout llTree = new LinearLayout(this);
		llTree.setOrientation(LinearLayout.VERTICAL);
		
		treeList = gameData.getTrees();
		initializeSpinner(llTree);
		constructTree(treeList.get(sharedPref.getInt("DEFAULT_TREE_SELECTION", 0)), llTree);
		
		// volatile layout elements refreshed, add to parent RelativeLayout
		layoutTree.addView(llTree, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
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
		RelativeLayout.LayoutParams paramsMod = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsMod.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
		// container for title, modifier
		RelativeLayout rlTitle = new RelativeLayout(this);
		rlTitle.addView(tvTitle);
		rlTitle.addView(tvModifier, paramsMod);
		layoutBoxPopup.addView(rlTitle, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		
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
		layoutBoxPopup.addView(new View(this), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 12));
		
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
		layoutBoxPopup.addView(new View(this), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 12));
		
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
}
