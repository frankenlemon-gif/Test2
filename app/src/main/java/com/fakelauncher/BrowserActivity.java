package fake.launcher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.*;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class BrowserActivity extends Activity {

    private WebView webView;
    private FrameLayout root;
    private boolean isRecentScreen = false;
    private int navBarHeight = 120;

	@Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);
        setContentView(root);

        navBarHeight = (int) (56 * getResources().getDisplayMetrics().density);

		webView = new WebView(this);

        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        webParams.bottomMargin = navBarHeight; 
        root.addView(webView, webParams);
		WebSettings s = webView.getSettings();
		s.setJavaScriptEnabled(true);	
		s.setDomStorageEnabled(false);          
		s.setDatabaseEnabled(false);            
		s.setSaveFormData(false);
		s.setSavePassword(false);               
		s.setCacheMode(WebSettings.LOAD_NO_CACHE);	
		webView.setWebViewClient(new WebViewClient());
		webView.setWebChromeClient(new WebChromeClient());		
		webView.loadUrl("https://www.google.com");      
        addNavigationBar();
	}

    

    private void addNavigationBar() {
        
        final LinearLayout navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setBackgroundColor(Color.parseColor("#E61A1A1A")); // Полупрозрачный темный

        final FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            navBarHeight
        );
        navParams.gravity = Gravity.BOTTOM;

        
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					root.getViewTreeObserver().removeOnGlobalLayoutListener(this);

					
					Rect visibleRect = new Rect();
					root.getGlobalVisibleRect(visibleRect);

					
					navParams.gravity = Gravity.BOTTOM;
					navBar.setLayoutParams(navParams);
				}
			});

        root.addView(navBar, navParams);

        
        Button backButton = createNavButton("<");
        backButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					try {
						Intent intent = new Intent(BrowserActivity.this, 
												   Class.forName("com.fakelauncher.LauncherActivity"));
						startActivity(intent);
					} catch (ClassNotFoundException e) {
						Toast.makeText(BrowserActivity.this, 
									   "LauncherActivity not found", 
									   Toast.LENGTH_SHORT).show();
					}
				}
			});

        
        Button homeButton = createNavButton("□");
        homeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					try {
						Intent intent = new Intent(BrowserActivity.this, 
												   Class.forName("com.fakelauncher.LauncherActivity"));
						startActivity(intent);
					} catch (ClassNotFoundException e) {
						Toast.makeText(BrowserActivity.this, 
									   "LauncherActivity not found", 
									   Toast.LENGTH_SHORT).show();
					}
				}
			});

        
        Button recentButton = createNavButton("|||");
        recentButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isRecentScreen) {
						
						showMainScreen();
					} else {
						
						showRecentAppsScreen();
					}
				}
			});

        
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT
        );
        buttonParams.weight = 1;

        navBar.addView(backButton, buttonParams);
        navBar.addView(homeButton, buttonParams);
        navBar.addView(recentButton, buttonParams);
    }

    private Button createNavButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(24);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setAllCaps(false);
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    

    private void showRecentAppsScreen() {
        isRecentScreen = true;
        root.removeAllViews();

        
        FrameLayout recentContainer = new FrameLayout(this);
        recentContainer.setBackgroundColor(Color.parseColor("#1A1A1A")); // Темный фон
        FrameLayout.LayoutParams recentParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        root.addView(recentContainer, recentParams);

        
        Button noRecentText = new Button(this);
        noRecentText.setText("Нет последних приложений");
        noRecentText.setTextSize(20);
        noRecentText.setTextColor(Color.parseColor("#CCCCCC"));
        noRecentText.setBackgroundColor(Color.TRANSPARENT);
        noRecentText.setGravity(Gravity.CENTER);
        noRecentText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showMainScreen();
				}
			});

        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.CENTER;

        recentContainer.addView(noRecentText, textParams);

        
        addNavigationBar();
    }

    

    private void showMainScreen() {
        isRecentScreen = false;
        root.removeAllViews();

        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        webParams.bottomMargin = navBarHeight; 
        root.addView(webView, webParams);

        
        addNavigationBar();
    }

	@Override
	protected void onPause()
	{
		super.onPause();
		finish();
	}


	

    @Override
    public void onBackPressed() {
        if (isRecentScreen) {
            showMainScreen();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
