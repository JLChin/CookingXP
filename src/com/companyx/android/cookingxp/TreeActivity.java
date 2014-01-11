package com.companyx.android.cookingxp;

import java.util.List;

import com.companyx.android.cookingxp.GameData.Tree;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TableLayout;

/**
 * TreeActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class TreeActivity extends BaseActivity {
	GameData gameData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trees);
		
		constructTree();
	}
	
	private void constructTree() {
		gameData = GameData.getInstance(this);
		List<Tree> treeList = gameData.getTrees();
		
		TableLayout tableTree = (TableLayout) findViewById(R.id.table_tree);
		ImageView imgView = new ImageView(this);
		imgView.setImageResource(R.drawable.ic_box_locked);
		
		// TODO replace with live version
		Tree tree = treeList.get(0);
		tableTree.addView(imgView);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
}
