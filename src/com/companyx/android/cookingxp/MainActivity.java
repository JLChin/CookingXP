package com.companyx.android.cookingxp;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

/**
 * MainActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class MainActivity extends BaseActivity {
	// VIEW HOLDERS
	private LinearLayout layoutMain;
	
	// FACEBOOK
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	
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
	 * Determine whether or not the user has granted the necessary permissions to publish the story.
	 * @param subset subset of permissions to check.
	 * @param superset superset of permissions to check against.
	 * @return true if all permissions contained in the subset are also contained in the superset, false otherwise.
	 */
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	    for (String string : subset) {
	        if (!superset.contains(string)) {
	            return false;
	        }
	    }
	    
	    return true;
	}
	
	/**
	 * Login to Facebook and publish a story to the logged-in user's wall using Graph API.
	 */
	private void shareOnFacebook() {
		// start Facebook Login
		Session.openActiveSession(this, true, new Session.StatusCallback() {
			// callback when session changes state
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {
					// make request to the /me API
					Request.newMeRequest(session, new Request.GraphUserCallback() {
						// callback after Graph API response with user object
						@Override
						public void onCompleted(GraphUser user, Response response) {
							publishFacebookStory(user.getName());
						}
					}).executeAsync();
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initialize();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		recipeDatabase.release();
		gameData.release();
		
		// release Facebook Session
		Session session = Session.getActiveSession();
		if (session != null)
			session.closeAndClearTokenInformation();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		// QUIT
		finish();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		
		layoutMain.removeAllViews();
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		refreshLayout();
	}
	
	/**
	 * Publish a link (together with a name, caption, image, etc.) to the currently logged-in Facebook user's wall.
	 * @param username the currently logged-in Facebook user's name.
	 */
	private void publishFacebookStory(String username) {
	    Session session = Session.getActiveSession();

	    if (session != null){
	        // check if the logged-in user has publish permissions; otherwise re-authorize to grant the missing permissions
	        List<String> permissions = session.getPermissions();
	        if (!isSubsetOf(PERMISSIONS, permissions)) {
	        	Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
	            session.requestNewPublishPermissions(newPermissionsRequest);
	            return;
	        }
	        
	        // create a Request object that will be executed by a subclass of AsyncTask called RequestAsyncTask
	        Bundle postParams = new Bundle();
	        postParams.putString("name", getString(R.string.facebook_name));
	        postParams.putString("caption", getString(R.string.facebook_caption));
	        postParams.putString("description", gameData.getRank() + " " + username + getString(R.string.facebook_s_score) + ": " + gameData.getScore());
	        postParams.putString("link", getString(R.string.facebook_link));
	        postParams.putString("picture", getString(R.string.facebook_picture));
	        
	        Request.Callback callback= new Request.Callback() {
	            public void onCompleted(Response response) {
	                FacebookRequestError error = response.getError();
	                if (error != null) {
	                	Toast.makeText(MainActivity.this, error.getErrorMessage(), Toast.LENGTH_SHORT).show();
	                } else {
	                	Toast.makeText(MainActivity.this, getString(R.string.facebook_share_success), Toast.LENGTH_LONG).show();
	                }
	            }
	        };
	        
	        // make a POST to the Graph API, passing in the current user's session, the Graph endpoint to post to, a Bundle of POST parameters, the HTTP method (POST) and a callback to handle the response when the call completes
	        Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);
	        RequestAsyncTask task = new RequestAsyncTask(request);
	        task.execute();
	    } else
	    	 Toast.makeText(this, getString(R.string.facebook_no_active_session), Toast.LENGTH_SHORT).show();
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
		
		// FACEBOOK SHARE
		Button buttonFacebookShare = new Button(this);
		buttonFacebookShare.setText(R.string.facebook_share);
		buttonFacebookShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareOnFacebook();
			}
		});
		layoutMain.addView(buttonFacebookShare);
	}
}
