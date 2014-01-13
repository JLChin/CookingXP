package com.companyx.android.cookingxp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;

/**
 * Game Database
 * 
 * Manages all game data.
 * 
 * RULES:
 * One Recipe can be a member of multiple Boxes.
 * One Box can be a member of multiple Trees.
 * Each Box is in one of three states: locked, unlocked or activated.
 * The same Box can be in different states on different Trees.
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public final class GameData {
	// CONSTANTS
	private static final int DEFAULT_TREE_HEIGHT = 4;
	private static final int NUM_OF_BOXES = 11;
	
	// STATE VARIABLES
	private static Map<Integer, Box> boxMap; // maps unique boxId to Box
	private static Map<Integer, Tree> treeMap; // maps unique treeId to Tree
	
	// SINGLETON
	private static GameData holder;
	
	// SYSTEM
	private static Context context;
	
	/**
	 * Class representing a box on the game Tree
	 */
	static class Box {
		// TEXT RESOURCES
		int titleStrRes;
		int descStrRes;
		
		// IMAGE RESOURCES
		int lockedImgRes;
		int unlockedImgRes;
		int activatedImgRes;
		
		Box (int titleStrRes, int descStrRes, int lockedImgRes, int unlockedImgRes, int activatedImgRes) {
			this.titleStrRes = titleStrRes;
			this.descStrRes = descStrRes;
			this.lockedImgRes = lockedImgRes;
			this.unlockedImgRes = unlockedImgRes;
			this.activatedImgRes = activatedImgRes;
		}
	}
	
	/**
	 * Class representing a game tree.
	 */
	static class Tree {
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
		 * This method is called when the Tree is ready to be validated.
		 */
		private void validateTree() {
			// update activated status of each BoxHolder
			for (List<BoxHolder> tier : boxHolderMatrix) {
				boolean rowHasActivated = false;
				
				for (BoxHolder bh : tier) {
					bh.updateActivatedStatus();
					if (bh.activated)
						rowHasActivated = true;
				}
				
				if (rowHasActivated)
					unlockedTier++;
			}
			
			// update unlocked status of each BoxHolder
			for (int tier = 0; tier < boxHolderMatrix.size(); tier++) {
				for (BoxHolder bh : boxHolderMatrix.get(tier))
					bh.updateUnlockedStatus(tier, unlockedTier);
			}
		}
	}
	
	/**
	 * Container class managing a Box's relation within a Tree instance.
	 * BoxHolder allows the Box contained in one Tree to have a different relation in another Tree.
	 */
	class BoxHolder {
		// STATE VARIABLES
		int boxId;
		private boolean unlocked;
		private boolean activated;
		private List<BoxHolder> incomingEdges;
		
		BoxHolder(int boxId) {
			this.boxId = boxId;
			incomingEdges = new ArrayList<BoxHolder>();
		}
		
		BoxHolder addEdge(BoxHolder incomingBH) {
			incomingEdges.add(incomingBH);
			return this;
		}
		
		/**
		 * Returns whether this box is activated.
		 * @return whether this box is activated.
		 */
		boolean isActivated() {
			return activated;
		}
		
		/**
		 * Returns whether this box is unlocked.
		 * @return whether this box is unlocked.
		 */
		boolean isUnlocked() {
			return unlocked;
		}
		
		/**
		 * Checks conditions and updates activated status.
		 */
		private void updateActivatedStatus() {
			// TODO
			//activated = true;
		}
		
		/**
		 * Checks conditions and updates unlocked status.
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
	
	private GameData() {
		resetData();
	}
	
	public void addBox(int boxId, int nameStrRes, int descStrRes, int lockedImgRes, int unlockedImgRes, int activatedImgRes) {
		boxMap.put(boxId, new Box(nameStrRes, descStrRes, lockedImgRes, unlockedImgRes, activatedImgRes));
	}
	
	public void addTree(int treeId, Tree newTree) {
		treeMap.put(treeId, newTree);
		newTree.validateTree(); // Tree is ready to be validated, set unlocked & activated statuses
	}
	
	/**
	 * Resets the database.
	 */
	@SuppressLint("UseSparseArrays")
	private void resetData() {
		boxMap = new HashMap<Integer, Box>();
		treeMap = new HashMap<Integer, Tree>();
		
		loadBoxes();
		loadTrees();
	}
	
	/**
	 * Loads Boxes into the game data.
	 */
	private void loadBoxes() {
		Resources r = context.getResources();
		String p = context.getPackageName();
		
		// get resource Id's and load
		for (int i = 0; i < NUM_OF_BOXES; i++)
			addBox(i, r.getIdentifier("game_box_title" + i, "string", p), r.getIdentifier("game_box_description" + i, "string", p), r.getIdentifier("ic_box_locked", "drawable", p), r.getIdentifier("ic_box_unlocked" + i, "drawable", p), r.getIdentifier("ic_box_activated" + i, "drawable", p));
	}
	
	/**
	 * Loads Trees into the game data.
	 */
	private void loadTrees() {
		// TEST TREE
		Tree newTree = new Tree(R.string.game_tree0);
		
		List<BoxHolder> tier1 = newTree.boxHolderMatrix.get(0);
		tier1.add(new BoxHolder(0));
		tier1.add(new BoxHolder(1));
		tier1.add(new BoxHolder(2));
		
		List<BoxHolder> tier2 = newTree.boxHolderMatrix.get(1);
		tier2.add(new BoxHolder(3));
		tier2.add(new BoxHolder(4));
		tier2.add(new BoxHolder(5));
		
		List<BoxHolder> tier3 = newTree.boxHolderMatrix.get(2);
		tier3.add(new BoxHolder(6));
		tier3.add(new BoxHolder(7));
		tier3.add(new BoxHolder(8));
		
		List<BoxHolder> tier4 = newTree.boxHolderMatrix.get(3);
		tier4.add(new BoxHolder(9));
		tier4.add(new BoxHolder(10));
		
		// add edges
		tier2.get(0).incomingEdges.add(tier1.get(0));
		tier2.get(1).incomingEdges.add(tier1.get(0));
		tier2.get(1).incomingEdges.add(tier1.get(1));
		tier2.get(2).incomingEdges.add(tier1.get(2));
		tier3.get(2).incomingEdges.add(tier2.get(2));
		
		addTree(0, newTree);
	}
	
	/**
	 * Returns a List of game Trees.
	 * @return a List of game Trees.
	 */
	public List<Tree> getTrees() {
		List<Tree> result = new ArrayList<Tree>();
		
		for (Map.Entry<Integer, Tree> entry : treeMap.entrySet())
			result.add(entry.getValue());
		
		return result;
	}
	
	/**
	 * Returns the Box indexed by the unique Id.
	 * @param boxId the unique Box identifier.
	 * @return the Box indexed by the unique Id.
	 */
	public Box findBoxById(int boxId) {
		return boxMap.get(boxId);
	}
}
