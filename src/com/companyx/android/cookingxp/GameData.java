package com.companyx.android.cookingxp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;

/**
 * Game Database
 * 
 * Manages all game data.
 * 
 * RULES:
 * One Recipe can be a member of multiple Boxes.
 * One Box can be a member of multiple Trees.
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
		String name;
		int imageRef;
		
		Box (String name, int imageRef) {
			this.name = name;
			this.imageRef = imageRef;
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
		 */
		class BoxHolder {
			boolean unlocked;
			boolean fulfilled;
			List<BoxHolder> incomingEdges;
			
		}
	}
	
	public void addBox(short boxId, String name, int imageRef) {
		boxMap.put(boxId, new Box(name, imageRef));
	}
	
	public void addTree(short treeId, Tree newTree) {
		treeMap.put(treeId, newTree);
	}
}
