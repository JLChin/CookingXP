package com.companyx.android.cookingxp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.widget.ImageView;

/**
 * Game Database
 * 
 * Manages all game data.
 * 
 * RULES:
 * - One Recipe can be a member of multiple Boxes.
 * - One Box can be a member of multiple Trees, managed by BoxHolders.
 * - Each BoxHolder is in one of three states: locked, unlocked or activated.
 * - The same Box can be in different states on different Trees.
 * - Tier 0 Boxes get unlocked by default, whose Recipes are, in turn, also unlocked by default; the Box unlock progress dictates which Recipes are visible.
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public final class GameData {
	// CONSTANTS
	private static final short DEFAULT_TREE_HEIGHT = 4;
	private static final short NUM_OF_BOXES = 11;
	
	// STATE VARIABLES
	private static Map<Short, Box> boxMap; // maps unique boxId to Box
	private static Map<Integer, Tree> treeMap; // maps unique treeId to Tree
	
	// SINGLETON
	private static GameData holder;
	
	// SYSTEM
	private static Context context;
	private static SharedPreferences sharedPref;
	private static RecipeDatabase recipeDatabase;
	
	/**
	 * Class representing a box on the game Tree.
	 * When a Box is activated, it may unlock other BoxHolders further down the Tree(s).
	 */
	static class Box {
		// STATE VARIABLES
		short boxId;
		private boolean activated;
		
		// TEXT RESOURCES
		int titleStrRes;
		int descStrRes;
		
		// IMAGE RESOURCES
		int lockedImgRes;
		int unlockedImgRes;
		int activatedImgRes;
		
		Box (short boxId, int titleStrRes, int descStrRes, int lockedImgRes, int unlockedImgRes, int activatedImgRes) {
			this.boxId = boxId;
			this.titleStrRes = titleStrRes;
			this.descStrRes = descStrRes;
			this.lockedImgRes = lockedImgRes;
			this.unlockedImgRes = unlockedImgRes;
			this.activatedImgRes = activatedImgRes;
		}
		
		/**
		 * Returns true if this Box is activated, false otherwise.
		 * @return true if this Box is activated, false otherwise.
		 */
		boolean isActivated() {
			return activated;
		}
		
		/**
		 * Sets the activated status of this Box.
		 * @param activated the boolean value to set the activated status to.
		 */
		void setActivated(boolean activated) {
			this.activated = activated;
		}
	}
	
	/**
	 * Container class managing a Box's relation within a particular Tree instance.
	 * BoxHolder allows the Box contained in one Tree to have a different relation in another Tree.
	 * When a BoxHolder is unlocked, its Box can be seen on the Tree, and that Box's Recipes become available to the user.
	 * When a BoxHolder is activated, it may unlock other BoxHolders further down that particular Tree instance.
	 */
	class BoxHolder {
		// STATE VARIABLES
		short boxId;
		ImageView imageView;
		List<BoxHolder> incomingEdges;
		private boolean unlocked;
		private boolean activated;
		
		BoxHolder(short boxId) {
			this.boxId = boxId;
			incomingEdges = new ArrayList<BoxHolder>();
		}
		
		/**
		 * Adds a prerequisite BoxHolder whose activation is required to unlock this BoxHolder.
		 * @param incomingBH the prerequisite BoxHolder.
		 * @return this BoxHolder instance, for convenience.
		 */
		BoxHolder addEdge(BoxHolder incomingBH) {
			incomingEdges.add(incomingBH);
			return this;
		}
		
		/**
		 * Returns true if this BoxHolder is activated, false otherwise.
		 * @return true if this BoxHolder is activated, false otherwise.
		 */
		boolean isActivated() {
			return activated;
		}
		
		/**
		 * Returns true if this BoxHolder is unlocked, false otherwise.
		 * @return true if this BoxHolder is unlocked, false otherwise.
		 */
		boolean isUnlocked() {
			return unlocked;
		}
		
		/**
		 * Checks conditions and updates activated status.
		 */
		private void updateActivatedStatus() {
			if (findBoxById(boxId).isActivated())
				activated = true;
			else
				activated = false;
		}
		
		/**
		 * Checks BoxHolder unlocking rules and updates unlocked status.
		 * Note: tier 0 BoxHolders are automatically unlocked here.
		 * @param tier the tier this BoxHolder is on.
		 * @param unlockedTier the unlocked tier of the parent Tree.
		 */
		private void updateUnlockedStatus(int tier, int unlockedTier) {
			boolean result = false;
			
			if (tier <= unlockedTier) {
				if (incomingEdges.isEmpty())
					result = true;
				else {
					for (BoxHolder bh : incomingEdges) {
						if (bh.activated)
							result = true;
					}
				}
			}
			
			unlocked = result;
		}
	}
	
	/**
	 * Class representing a game tree.
	 */
	class Tree {
		private int nameStrRes;
		private int unlockedTier;
		List<List<BoxHolder>> boxHolderMatrix; // List of tiers of BoxHolders
		
		Tree(int nameStrRes) {
			this.nameStrRes = nameStrRes;
			unlockedTier = 0;
			
			boxHolderMatrix = new ArrayList<List<BoxHolder>>();
			for (int i = 0; i < DEFAULT_TREE_HEIGHT; i++)
				boxHolderMatrix.add(new ArrayList<BoxHolder>());
		}
		
		/**
		 * Returns the localized name of the Tree.
		 * @return the localized name of the Tree.
		 */
		public String getName() {
			return context.getString(nameStrRes);
		}
		
		/**
		 * Checks all conditions and updates unlocked and activated status of each BoxHolder, unlockedTier status of Tree.
		 * Releases locked Recipes according to Tree progress.
		 * This method is called before the Tree needs to be used or displayed, to reflect changes made.
		 * @return this Tree instance, for convenience.
		 */
		private Tree validateTree() {
			// re-verify tier status
			unlockedTier = 0;
			
			// UPDATE ACTIVATED STATUS OF EACH BOXHOLDER
			int currentTier = 0;
			for (List<BoxHolder> tier : boxHolderMatrix) {
				boolean rowHasActivated = false;
				
				for (BoxHolder bh : tier) {
					bh.updateActivatedStatus();
					if (bh.activated)
						rowHasActivated = true;
				}	
				
				// advance unlockedTier
				if (rowHasActivated && unlockedTier == currentTier)
					unlockedTier++;
				
				currentTier++;
			}
			
			// UPDATE UNLOCKED STATUS OF EACH BOXHOLDER AND RELEASE RECIPES
			for (int tier = 0; tier < boxHolderMatrix.size(); tier++) {
				for (BoxHolder bh : boxHolderMatrix.get(tier)) {
					bh.updateUnlockedStatus(tier, unlockedTier);
					
					if (bh.isUnlocked())
						recipeDatabase.unlockRecipesByBox(bh.boxId);
				}
			}
			
			return this;
		}
	}
	
	/**
	 * Returns the singleton instance of the game database.
	 * @param c the calling context.
	 * @return the singleton instance of the game database.
	 */
	public synchronized static GameData getInstance(Context c) {
		context = c;
		
		if (holder == null)
			holder = new GameData();
		
		return holder;
	}
	
	/**
	 * Private constructor.
	 */
	private GameData() {
		resetGameData();
		loadGameData();
	}
	
	/**
	 * Adds a new Box to the database.
	 * @param boxId the unique identifier for the new Box.
	 * @param nameStrRes the resource identifier for the Box name.
	 * @param descStrRes the resource identifier for the Box description.
	 * @param lockedImgRes the resource identifier for the Box locked state image.
	 * @param unlockedImgRes the resource identifier for the Box unlocked state image.
	 * @param activatedImgRes the resource identifier for the Box activated state image.
	 */
	public void addBox(short boxId, int nameStrRes, int descStrRes, int lockedImgRes, int unlockedImgRes, int activatedImgRes) {
		boxMap.put(boxId, new Box(boxId, nameStrRes, descStrRes, lockedImgRes, unlockedImgRes, activatedImgRes));
	}
	
	/**
	 * Adds a new Tree to the database. Unlock tier 0 Recipes by default.
	 * Note: Tree should be fully formed before adding.
	 * @param treeId the unique identifier for the new Tree.
	 * @param newTree the new Tree to be added to the database.
	 */
	public void addTree(int treeId, Tree newTree) {
		treeMap.put(treeId, newTree);
	}
	
	/**
	 * Clears the live and saved Box unlock progress.
	 */
	void clearGameData() {
		resetGameData();
		saveGameData();
	}
	
	/**
	 * Returns the Box indexed by the unique Id.
	 * @param boxId the unique Box identifier.
	 * @return the Box indexed by the unique Id.
	 */
	public Box findBoxById(short boxId) {
		return boxMap.get(boxId);
	}
	
	/**
	 * Returns a List of updated game Trees.
	 * @return a List of updated game Trees.
	 */
	public List<Tree> getTrees() {
		List<Tree> result = new ArrayList<Tree>();
		
		for (Map.Entry<Integer, Tree> entry : treeMap.entrySet())
			result.add(entry.getValue().validateTree());
		
		return result;
	}
	
	/**
	 * Loads Boxes into the game data.
	 */
	private void loadBoxes() {
		Resources r = context.getResources();
		String p = context.getPackageName();
		
		// get resource Id's and load
		for (short i = 0; i < NUM_OF_BOXES; i++)
			addBox(i, r.getIdentifier("game_box_title" + i, "string", p), r.getIdentifier("game_box_description" + i, "string", p), r.getIdentifier("ic_box_locked", "drawable", p), r.getIdentifier("ic_box_unlocked" + i, "drawable", p), r.getIdentifier("ic_box_activated" + i, "drawable", p));
	}
	
	/**
	 * Loads game data from preferences file.
	 */
	private void loadGameData() {
		String serialized = sharedPref.getString("SERIALIZED_GAME_DATA", null);
		
		if (serialized != null) {
			String[] boxIds = serialized.split(" ");
			
			for (String boxId : boxIds)
				boxMap.get(Short.valueOf(boxId)).setActivated(true);
		}
	}
	
	/**
	 * Loads Trees into the game data.
	 */
	private void loadTrees() {
		// TEST TREE
		Tree newTree = new Tree(R.string.game_tree0);
		
		List<BoxHolder> tier1 = newTree.boxHolderMatrix.get(0);
		tier1.add(new BoxHolder((short) 0));
		tier1.add(new BoxHolder((short) 1));
		tier1.add(new BoxHolder((short) 2));
		
		List<BoxHolder> tier2 = newTree.boxHolderMatrix.get(1);
		tier2.add(new BoxHolder((short) 3));
		tier2.add(new BoxHolder((short) 4));
		tier2.add(new BoxHolder((short) 5));
		
		List<BoxHolder> tier3 = newTree.boxHolderMatrix.get(2);
		tier3.add(new BoxHolder((short) 6));
		tier3.add(new BoxHolder((short) 7));
		tier3.add(new BoxHolder((short) 8));
		
		List<BoxHolder> tier4 = newTree.boxHolderMatrix.get(3);
		tier4.add(new BoxHolder((short) 9));
		tier4.add(new BoxHolder((short) 10));
		
		// add edges
		tier2.get(0).incomingEdges.add(tier1.get(0));
		tier2.get(1).incomingEdges.add(tier1.get(0));
		tier2.get(1).incomingEdges.add(tier1.get(1));
		tier2.get(2).incomingEdges.add(tier1.get(2));
		tier3.get(2).incomingEdges.add(tier2.get(2));
		
		addTree(0, newTree);
	}
	
	/**
	 * Updates the box after one of its recipes has been completed.
	 * TODO make more interesting rules or leveling system, for now simply one ping --> activated
	 * @param boxId the unique identifier for the Box.
	 */
	void pingBox(short boxId) {
		findBoxById(boxId).setActivated(true);
		
		saveGameData();
	}
	
	/**
	 * Loads game into default state with default unlocks and progress.
	 */
	@SuppressLint("UseSparseArrays")
	void resetGameData() {
		boxMap = new HashMap<Short, Box>();
		treeMap = new HashMap<Integer, Tree>();
		
		sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		
		recipeDatabase = RecipeDatabase.getInstance(context);
		
		loadBoxes();
		loadTrees();
	}
	
	/**
	 * Save game data as a serialized String to preferences file.
	 * Currently Box unlock progress is saved.
	 */
	private void saveGameData() {
		String serialized = "";
		
		for (Box box : boxMap.values()) {
			if (box.isActivated())
				serialized += box.boxId + " ";
		}
		
		// remove trailing space and save to preferences file
		if (serialized.length() > 0) {
			SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
			sharedPrefEditor.putString("SERIALIZED_GAME_DATA", serialized.substring(0, serialized.length() - 1));
			sharedPrefEditor.commit();
		}
	}
}
