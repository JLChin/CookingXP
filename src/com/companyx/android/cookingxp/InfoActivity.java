package com.companyx.android.cookingxp;

import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * InfoActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class InfoActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		
		initialize();
	}
	
	private void initialize() {
		LinearLayout layoutInfo = (LinearLayout) findViewById(R.id.layout_info);
		
		TextView tvInfo = new TextView(this);
		tvInfo.setText(R.string.info_credits);
		tvInfo.setTextSize(16 + 0.5f);
		
		layoutInfo.addView(tvInfo);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
}