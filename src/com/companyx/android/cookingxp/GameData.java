package com.companyx.android.cookingxp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.ImageView;

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
	
	// STATE VARIABLES
	private static Map<Short, Box> boxMap; // maps unique boxId to Box
	private static Map<Short, Tree> treeMap; // maps unique treeId to Tree
	
	// SINGLETON
	private static GameData holder;
	
	// SYSTEM
	private static Context context;
	
	private GameData() {
		resetData();
	}
	
	/**
	 * Resets the database.
	 */
	private void resetData() {
		boxMap = new HashMap<Short, Box>();
		treeMap = new HashMap<Short, Tree>();
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
	 * Class representing a box on the game Tree
	 */
	static class Box {
		// DATA
		int nameStrRes;
		
		// IMAGE RESOURCES
		int lockedImgRes;
		int unlockedImgRes;
		int activatedImgRes;
		
		Box (int nameStrRes, int lockedImgRes, int unlockedImgRes, int activatedImgRes) {
			this.nameStrRes = nameStrRes;
			this.lockedImgRes = lockedImgRes;
			this.unlockedImgRes = unlockedImgRes;
			this.activatedImgRes = activatedImgRes;
		}
	}
	
	/**
	 * Class representing a game tree.
	 */
	static class Tree {
		int nameStrRes;
		byte unlockedTier; // 0 <= tier < DEFAULT_TREE_HEIGHT
		List<List<BoxHolder>> boxHolderMatrix; // List of tiers of BoxHolders
		
		Tree(int nameStrRes) {
			this.nameStrRes = nameStrRes;
			unlockedTier = 0;
			
			boxHolderMatrix = new ArrayList<List<BoxHolder>>();
			for (int i = 0; i < DEFAULT_TREE_HEIGHT; i++)
				boxHolderMatrix.add(new ArrayList<BoxHolder>());
		}
		
		/**
		 * Container class managing a Box's relation within this tree instance.
		 * BoxHolder allows the Box contained in this Tree to have a different relation in another Tree.
		 */
		class BoxHolder {
			// STATE VARIABLES
			Box box;
			private byte tier; // 0 <= tier < DEFAULT_TREE_HEIGHT
			private boolean unlocked;
			private boolean activated;
			private List<BoxHolder> incomingEdges;
			
			// VIEW RESOURCES
			ImageView imageView;
			
			BoxHolder(Box box, byte tier, ImageView imageView) {
				this.box = box;
				this.tier = tier;
				this.imageView = imageView;
				incomingEdges = new ArrayList<BoxHolder>();
			}
			
			BoxHolder addEdge(BoxHolder incomingBH) {
				incomingEdges.add(incomingBH);
				return this;
			}
			
			/**
			 * Returns whether this box is unlocked.
			 * @return whether this box is unlocked.
			 */
			boolean isUnlocked() {
				return unlocked;
			}
			
			/**
			 * Checks conditions and updates unlocked status.
			 */
			private void updateUnlockedStatus() {
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
			
			/**
			 * Returns whether this box is activated.
			 * @return whether this box is activated.
			 */
			boolean isActivated() {
				return activated;
			}
			
			/**
			 * Checks conditions and updates activated status.
			 */
			private void updateActivatedStatus() {
				// TODO
				activated = true;
			}
		}
		
		/**
		 * Checks all conditions and updates unlocked and activated status of each BoxHolder, unlockedTier status of Tree. TODO
		 * This method is called when the Tree is ready to be validated.
		 */
		private void updateTree() {
			// update activated status of each BoxHolder
			for (List<BoxHolder> row : boxHolderMatrix) {
				for (BoxHolder bh : row)
					bh.updateActivatedStatus();
			}
			
			// update unlocked status of each BoxHolder
			for (List<BoxHolder> row : boxHolderMatrix) {
				for (BoxHolder bh : row)
					bh.updateUnlockedStatus();
			}
		}
	}
	
	public void addBox(short boxId, int nameStrRes, int lockedImgRes, int unlockedImgRes, int activatedImgRes) {
		boxMap.put(boxId, new Box(nameStrRes, lockedImgRes, unlockedImgRes, activatedImgRes));
	}
	
	public void addTree(short treeId, Tree newTree) {
		treeMap.put(treeId, newTree);
		newTree.updateTree(); // Tree is ready to be validated, set unlocked & activated statuses
	}
}
