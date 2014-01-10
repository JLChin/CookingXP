package com.companyx.android.cookingxp;

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
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trees);
		
		constructTree();
	}
	
	private void constructTree() {
		TableLayout tableTree = (TableLayout) findViewById(R.id.table_tree);
		ImageView imgView = new ImageView(this);
		imgView.setImageResource(R.drawable.ic_box_locked);
		
		tableTree.addView(imgView);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
}
