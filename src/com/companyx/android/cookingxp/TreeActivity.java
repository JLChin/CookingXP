package com.companyx.android.cookingxp;

import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.companyx.android.cookingxp.GameData.Box;
import com.companyx.android.cookingxp.GameData.BoxHolder;
import com.companyx.android.cookingxp.GameData.Tree;

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
	
	/**
	 * Automatically constructs Tree layouts from GameData info.
	 */
	private void constructTree() {
		gameData = GameData.getInstance(this);
		List<Tree> treeList = gameData.getTrees();
		
		LinearLayout layoutTree = (LinearLayout) findViewById(R.id.layout_tree);
		
		Tree tree = treeList.get(0); // TODO live version, multiple Trees
		
		for (int tier = 0; tier < tree.boxHolderMatrix.size(); tier++) {
			// tier container
			RelativeLayout rl = new RelativeLayout(this);
			RelativeLayout.LayoutParams paramsRL = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			rl.setLayoutParams(paramsRL);
			
			// ImageView container
			LinearLayout ll = new LinearLayout(this); // default horizontal orientation
			RelativeLayout.LayoutParams paramsLL = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsLL.addRule(RelativeLayout.CENTER_IN_PARENT);
			ll.setLayoutParams(paramsLL);
			
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
				
				// attach onClick PopupWindow to ImageView
				
				ll.addView(imgView);
			}
			
			// ImageViews constructed, add to tier container
			rl.addView(ll);
			
			// tier constructed, add to layout
			layoutTree.addView(rl);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
}
