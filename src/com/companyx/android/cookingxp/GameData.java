package com.companyx.android.cookingxp;

import android.content.Context;

/**
 * Game Database
 * 
 * Manages all game data.
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public final class GameData {
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
		short boxId;
		String name;
		int imageRef;
		
		Box (short boxId, String name, int imageRef) {
			this.boxId = boxId;
			this.name = name;
			this.imageRef = imageRef;
		}
	}
	
	/**
	 * Class representing a game tree.
	 */
	static class Tree {	
	}
}
