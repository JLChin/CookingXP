package com.companyx.android.cookingxp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;


/**
 * MainActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class MainActivity extends BaseActivity {
	// CONSTANTS
	static final String PENDING_REQUEST_BUNDLE_KEY = "com.companyx.android.cookingxp:PendingRequest";
	
	// VIEW HOLDERS
	private LinearLayout layoutMain;
	TextView textViewFacebookResults;
	EditText editRequests;
	
	// STATE VARIABLES
	boolean pendingRequest;
	
	// SYSTEM
	Session session;
	
	private Session createSession() {
        Session activeSession = Session.getActiveSession();
        if (activeSession == null || activeSession.getState().isClosed()) {
            activeSession = new Session.Builder(this).setApplicationId(getString(R.string.facebook_app_id)).build();
            Session.setActiveSession(activeSession);
        }
        
        return activeSession;
    }
	
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
		
		// FACEBOOK SETUP
		session = createSession();
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		
		// sharing
		if (session.onActivityResult(this, requestCode, resultCode, data) && pendingRequest && session.getState().isOpened()) {
            sendRequests();
        }
	}
	
	private void onClickRequest() {
        if (this.session.isOpened()) {
            sendRequests();
        } else {
            StatusCallback callback = new StatusCallback() {
                public void call(Session session, SessionState state, Exception exception) {
                    if (exception != null) {
                        new AlertDialog.Builder(MainActivity.this).setTitle(R.string.facebook_login_failed).setMessage(exception.getMessage()).setPositiveButton(R.string.ok, null).show();
                        MainActivity.this.session = createSession();
                    }
                }
            };
            
            pendingRequest = true;
            this.session.openForRead(new Session.OpenRequest(this).setCallback(callback));
        }
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
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		pendingRequest = savedInstanceState.getBoolean(PENDING_REQUEST_BUNDLE_KEY, pendingRequest);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean(PENDING_REQUEST_BUNDLE_KEY, pendingRequest);
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
		
		// LOGIN FACEBOOK BUTTON
		Button buttonFacebookLogin = new Button(this);
		buttonFacebookLogin.setText(R.string.facebook_login);
		buttonFacebookLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loginFacebook();
			}
		});
		layoutMain.addView(buttonFacebookLogin);
		
		// FACEBOOK SHARE BUTTON
		Button buttonFacebookShare = new Button(this);
		buttonFacebookShare.setText(R.string.facebook_share);
		buttonFacebookShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onClickRequest();
			}
		});
		layoutMain.addView(buttonFacebookShare);
		
		editRequests = new EditText(this);
		layoutMain.addView(editRequests);
		
		textViewFacebookResults = new TextView(this);
		layoutMain.addView(textViewFacebookResults);
	}
	
	private void sendRequests() {
        textViewFacebookResults.setText("");

        String requestIdsText = editRequests.getText().toString();
        String[] requestIds = requestIdsText.split(",");

        List<Request> requests = new ArrayList<Request>();
        for (final String requestId : requestIds) {
            requests.add(new Request(session, requestId, null, null, new Request.Callback() {
                public void onCompleted(Response response) {
                    GraphObject graphObject = response.getGraphObject();
                    FacebookRequestError error = response.getError();
                    String s = textViewFacebookResults.getText().toString();
                    if (graphObject != null) {
                        if (graphObject.getProperty("id") != null) {
                            s = s + String.format("%s: %s\n", graphObject.getProperty("id"), graphObject.getProperty("name"));
                        } else {
                            s = s + String.format("%s: <no such id>\n", requestId);
                        }
                    } else if (error != null) {
                        s = s + String.format("Error: %s", error.getErrorMessage());
                    }
                    textViewFacebookResults.setText(s);
                }
            }));
        }
        pendingRequest = false;
        Request.executeBatchAndWait(requests);
    }
}
