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
	public static final int[] TREES = {R.string.game_tree0, R.string.game_tree1, R.string.game_tree2};
	
	// VIEW HOLDERS
	private LinearLayout layoutTree;
	private RelativeLayout layoutTreeOverlay;
	private Spinner spinnerTree;
	
	// STATE VARIABLES
	private List<Tree> treeList;
	private Map<View, PopupWindow> openPopups;
	private List<BoxHolder> pendingEdgeBHs;
	
	// SYSTEM
	private GameData gameData;
	private RecipeDatabase recipeDatabase;
	OnGlobalLayoutListener listenerOGL;
	
	/**
	 * Custom View class for drawing the path edges between Boxes.
	 */
	static class EdgeView extends View {
		Paint paint;
		float startX, startY, stopX, stopY;

		public EdgeView(Context context, float startX, float startY, float stopX, float stopY) {
			super(context);
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(8);
			
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
	 */
	private void constructTree(Tree tree) {
		// calculate image spacing based on screen size and current orientation
		@SuppressWarnings("deprecation")
		float screenWidthInPixels = getWindowManager().getDefaultDisplay().getWidth();
		int imgSpacingInPixels = (int) (DEFAULT_IMAGE_SPACING_RATIO * screenWidthInPixels + 0.5f);
		
		pendingEdgeBHs = new ArrayList<BoxHolder>();
		
		for (int tier = 0; tier < tree.boxHolderMatrix.size(); tier++) {
			// horizontal break between tiers
			layoutTree.addView(new View(this), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgSpacingInPixels));
						
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
			layoutTree.addView(rl, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		}
		
		// draw edges when ImageViews are given layout dimensions
		listenerOGL = new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				drawEdges();
			}	
		};
		layoutTree.getViewTreeObserver().addOnGlobalLayoutListener(listenerOGL);
	}
	
	/**
	 * Draws the path edges connecting the Boxes on the Tree.
	 * This is called only once, after the ImageViews have been given layout dimensions.
	 */
	@SuppressWarnings("deprecation")
	private void drawEdges() {
		// remove listener, no longer needed
		layoutTree.getViewTreeObserver().removeGlobalOnLayoutListener(listenerOGL);
		
		for (BoxHolder bh : pendingEdgeBHs) {
			for (BoxHolder incomingBH : bh.incomingEdges) {
				int[] startXY = new int[2];
				int[] endXY = new int[2];
				incomingBH.imageView.getLocationInWindow(startXY);
				bh.imageView.getLocationInWindow(endXY);
				
				// if current ImageView lines up with incoming ImageView, they are on the same axis
				if (bh.imageView.getLeft() == incomingBH.imageView.getLeft()) {
					// bottom middle of incoming ImageView to top middle of current ImageView
				} else if (bh.imageView.getLeft() > incomingBH.imageView.getLeft()) {
					// lower right corner of incoming ImageView to upper left corner of current ImageView
				} else {
					// lower left corner of incoming ImageView to upper right corner of current ImageView
				}
				
				// TODO testing
				TextView tvTest = new TextView(this);
				tvTest.setText("EDGEsdgfsdfgdsfgsdgsdfgsdfgsdfgsdgfss");
				RelativeLayout.LayoutParams paramsTest = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				paramsTest.leftMargin = (startXY[0] + endXY[0]) / 2;
				paramsTest.topMargin = (startXY[1] + endXY[1]) / 2 - incomingBH.imageView.getHeight();
				layoutTreeOverlay.addView(tvTest, paramsTest);
				
				//layoutTreeOverlay.addView(new EdgeView(this, startXY[0], startXY[1], endXY[0], endXY[1]));
				layoutTreeOverlay.addView(new EdgeView(this, 0, 0, 100, 100), paramsTest);
			}
		}
	}
	
	/**
	 * Set up the Activity.
	 */
	private void initialize() {
		recipeDatabase = RecipeDatabase.getInstance(this);
		gameData = GameData.getInstance(this);
		treeList = gameData.getTrees();
		
		openPopups = new HashMap<View, PopupWindow>();
		
		layoutTree = (LinearLayout) findViewById(R.id.layout_tree);
		layoutTreeOverlay = (RelativeLayout) findViewById(R.id.layout_tree_overlay);
	}
	
	/**
	 * Set up the Tree selection Spinner.
	 */
	private void initializeSpinner() {
		spinnerTree = new Spinner(this);
		
		// add selections
		List<String> trees = new ArrayList<String>();
		for (int i : TREES)
			trees.add(getString(i));
		spinnerTree.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, trees));
		
		// attach listener
		spinnerTree.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO position = treeId
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		layoutTree.addView(spinnerTree);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trees);
		
		initialize();
		initializeSpinner();
		constructTree(treeList.get(0));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
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
