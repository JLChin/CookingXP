package com.companyx.android.cookingxp;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
	
	/**
	 * Helper function to set up social media linking buttons, adding an ImageButton to a parent ViewGroup.
	 * @param viewGroup the parent ViewGroup to add the ImageButton to.
	 * @param drawableRes the Drawable resource to set as the ImageButton image.
	 * @param descriptionRes the String resource to set as the ImageButton content description, for accessibility.
	 * @param func the function to call upon clicking the ImageButton, passed in wrapped in a Callable interface. (Command Pattern)
	 */
	private void addSocialMediaLinkButton(ViewGroup viewGroup, int drawableRes, int descriptionRes, final Callable<Void> func) {
		ImageButton imageButton = new ImageButton(this);
		imageButton.setImageResource(drawableRes);
		imageButton.setBackgroundResource(0);
		imageButton.setContentDescription(getString(descriptionRes));
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					func.call();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		viewGroup.addView(imageButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
	}
	
	/**
	 * Helper function to set up social media linking buttons, adding the row of ImageButtons to a parent ViewGroup.
	 * @param viewGroup the parent ViewGroup to add the row of ImageButtons to.
	 */
	private void addSocialMediaLinksToLayout(ViewGroup viewGroup) {
		// horizontal layout container
		LinearLayout llSocial = new LinearLayout(this);
		
		// FACEBOOK SHARE
		addSocialMediaLinkButton(llSocial, R.drawable.ic_facebook, R.string.facebook_share, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				shareOnFacebook();
				return null;
			}
		});
		
		// TWITTER SHARE
		addSocialMediaLinkButton(llSocial, R.drawable.ic_twitter, R.string.twitter_share, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// TODO shareOnTwitter();
				Toast.makeText(MainActivity.this, "Check back soon for Twitter sharing!", Toast.LENGTH_SHORT).show();
				return null;
			}
		});
		
		// GOOGLE+ SHARE
		addSocialMediaLinkButton(llSocial, R.drawable.ic_google_plus, R.string.google_plus_share, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// TODO shareOnGooglePlus();
				Toast.makeText(MainActivity.this, "Check back soon for Google+ sharing!", Toast.LENGTH_SHORT).show();
				return null;
			}
		});
		
		viewGroup.addView(llSocial);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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

	    if (session != null) {
	        // check if the logged-in user has publish permissions; otherwise re-authorize to grant the missing permissions
	    	List<String> requiredPermissions = Arrays.asList("publish_actions");
	        List<String> currentPermissions = session.getPermissions();
	        if (!isSubsetOf(requiredPermissions, currentPermissions)) {
	        	Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, requiredPermissions);
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
		// TODO for debugging
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
		
		// layout filler, to push social media links to the bottom
		layoutMain.addView(new View(this), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
		
		addSocialMediaLinksToLayout(layoutMain);
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
}
