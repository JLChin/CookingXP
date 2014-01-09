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
		// STATE VARIABLES
		String name;
		
		// VIEW RESOURCES
		int lockedImgRes;
		int unlockedImgRes;
		int activatedImgRes;
		
		Box (String name, int lockedImgRes, int unlockedImgRes, int activatedImgRes) {
			this.name = name;
			this.lockedImgRes = lockedImgRes;
			this.unlockedImgRes = unlockedImgRes;
			this.activatedImgRes = activatedImgRes;
		}
	}
	
	/**
	 * Class representing a game tree.
	 */
	static class Tree {
		String name;
		List<List<BoxHolder>> boxHolderMatrix; // two dimensional matrix of BoxHolders
		
		Tree(String name) {
			this.name = name;
			
			boxHolderMatrix = new ArrayList<List<BoxHolder>>();
			for (int i = 0; i < DEFAULT_TREE_HEIGHT; i++)
				boxHolderMatrix.add(new ArrayList<BoxHolder>());
		}
		
		/**
		 * Container class managing a Box's relation within this tree instance.
		 * The Box contained in this Tree may have a different status in another Tree.
		 */
		class BoxHolder {
			// STATE VARIABLES
			Box box;
			byte tier; // 0 <= tier < DEFAULT_TREE_HEIGHT
			boolean unlocked;
			boolean activated;
			List<BoxHolder> incomingEdges;
			
			// VIEW RESOURCES
			ImageView imageView;
			
			BoxHolder(Box box, byte tier, ImageView imageView) {
				this.box = box;
				this.tier = tier;
				this.imageView = imageView;
				
			}
			
			boolean isUnlocked() {
				if (tier == 0)
					return true;
				return false;
			}
			
			boolean isActivated() {
				// TODO check conditions
				return true;
			}
		}
	}
	
	public void addBox(short boxId, String name, int lockedImgRes, int unlockedImgRes, int activatedImgRes) {
		boxMap.put(boxId, new Box(name, lockedImgRes, unlockedImgRes, activatedImgRes));
	}
	
	public void addTree(short treeId, Tree newTree) {
		treeMap.put(treeId, newTree);
	}
}
