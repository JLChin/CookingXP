package com.companyx.android.cookingxp;

import java.io.InputStream;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;

/**
 * MainActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class MainActivity extends BaseActivity {
	// VIEW HOLDERS
	private LinearLayout layoutMain;
	
	// SYSTEM
	private UiLifecycleHelper uiHelper;
	
	private void initialize() {
		// LOAD RECIPES FROM FILE
		InputStream inputStream = getResources().openRawResource(R.raw.master_recipe_data);
		RecipeLoader loader = new RecipeLoader(inputStream, recipeDatabase);
		loader.loadData();
		
		// LOAD FAVORITES
		recipeDatabase.loadFavoriteRecipes();
		
		// LOAD SHOPPING LIST
		recipeDatabase.loadShoppingListRecipes();
		
		gameData.validate();
	}
	
	/**
	 * Facebook login.
	 */
	private void loginFacebook() {
		// start Facebook Login
		Session.openActiveSession(this, true, new Session.StatusCallback() {

			// callback when session changes state
			@SuppressWarnings("deprecation")
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {
					// make request to the /me API
					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

					  // callback after Graph API response with user object
					  @Override
					  public void onCompleted(GraphUser user, Response response) {
						  if (user != null) {
							  TextView tvWelcome = new TextView(MainActivity.this);
							  tvWelcome.setText("Hello " + user.getName() + "!");
							  layoutMain.addView(tvWelcome);
							}
					  }
					});
				}
			}
		});
	}
	
	/**
	 * Callback handling, invoked when dialog closes and control returns to the calling app.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		
		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	            Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	            Log.i("Activity", "Success!");
	        }
	    });
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initialize();
		//loginFacebook();
		
		uiHelper = new UiLifecycleHelper(this, null);
	    uiHelper.onCreate(savedInstanceState);
	    
	    //can only use if Facebook Android app is present
	    //FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this).setLink("https://developers.facebook.com/android").build();
	    //uiHelper.trackPendingDialogCall(shareDialog.present());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		uiHelper.onDestroy();
		
		recipeDatabase.release();
		gameData.release();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		// QUIT
		finish();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		uiHelper.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		
		layoutMain.removeAllViews();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		refreshLayout();
	}
	
	/**
	 * Refresh the screen layout.
	 * This is called onRestart() and handles any game updates since the user left the current Activity.
	 */
	private void refreshLayout() {
		layoutMain = (LinearLayout) findViewById(R.id.layout_main);
		int padding = (int) (scalingFactor * 10 + 0.5f);
		
		// USER INFO BOX
		LinearLayout layoutInfoContainer = new LinearLayout(this);
		layoutInfoContainer.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1.0f));
		
		LinearLayout layoutInfo = new LinearLayout(this);
		layoutInfo.setOrientation(LinearLayout.VERTICAL);
		layoutInfo.setPadding(padding, padding, padding, padding);
		layoutInfo.setBackgroundResource(R.drawable.box_background_dark);
		
		// RANK
		TextView tvRank = new TextView(this);
		tvRank.setTextColor(Color.LTGRAY);
		tvRank.setText(getString(R.string.rank) + ": " + gameData.getRank());
		layoutInfo.addView(tvRank, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		// SCORE
		TextView tvScore = new TextView(this);
		tvScore.setTextColor(Color.LTGRAY);
		tvScore.setText(getString(R.string.score) + ": " + gameData.getScore());
		layoutInfo.addView(tvScore, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		layoutInfoContainer.addView(layoutInfo, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layoutMain.addView(layoutInfoContainer);
		
		// WELCOME
		TextView tvWelcome = new TextView(this);
		tvWelcome.setText(R.string.welcome);
		tvWelcome.setTextSize(16 + 0.5f);
		layoutMain.addView(tvWelcome);
		
		// RESET GAME DATA BUTTON
		Button buttonReset = new Button(this);
		buttonReset.setText(R.string.reset);
		buttonReset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				gameData.clearGameData();
				
				layoutMain.removeAllViews();
				refreshLayout();
			}	
		});
		layoutMain.addView(buttonReset);
	}
}
