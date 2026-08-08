package fake.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LauncherActivity extends Activity {

    private GridView gridView;
    private LauncherApps launcherApps;
    private List<LauncherActivityInfo> apps;
    private LauncherApps.Callback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
        
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        gridView = new GridView(this);
        gridView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        gridView.setNumColumns(4);
        gridView.setBackgroundColor(Color.TRANSPARENT);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        float density = getResources().getDisplayMetrics().density;
        int finalPadding = (int) (44 * density);
        gridView.setPadding(0, finalPadding, 0, finalPadding);
		gridView.setClipToPadding(false);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            LauncherActivityInfo info = apps.get(position);
            launcherApps.startMainActivity(info.getComponentName(), info.getUser(), null, null);
        });

        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            LauncherActivityInfo info = apps.get(position);
            PopupMenu popup = new PopupMenu(this, view);
            popup.getMenu().add(0, 1, 0, "App info");
            popup.getMenu().add(0, 2, 1, "Uninstall");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    launcherApps.startAppDetailsActivity(info.getComponentName(), info.getUser(), null, null);
                    return true;
                } else if (item.getItemId() == 2) {
                    String pkg = info.getApplicationInfo().packageName;
                    startActivity(new Intent(Intent.ACTION_UNINSTALL_PACKAGE, 
                            android.net.Uri.parse("package:" + pkg)));
                    return true;
                }
                return false;
            });
            popup.show();
            return true;
        });

        Button bgButton = new Button(this) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                
                int position = gridView.pointToPosition(x, y);
                if (position != AdapterView.INVALID_POSITION) {
                    return false; 
                }
                
                return super.onTouchEvent(event);
            }
        };

        bgButton.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        bgButton.setBackgroundColor(Color.TRANSPARENT);
        bgButton.setElevation(0f);
        bgButton.setTranslationZ(0f);

        bgButton.setOnClickListener(v -> {});
        bgButton.setOnLongClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        });

        root.addView(gridView);
        root.addView(bgButton);
        setContentView(root);

        callback = new LauncherApps.Callback() {
            @Override
            public void onPackageAdded(String packageName, android.os.UserHandle user) { loadApps(); }
            @Override
            public void onPackageRemoved(String packageName, android.os.UserHandle user) { loadApps(); }
            @Override
            public void onPackageChanged(String packageName, android.os.UserHandle user) { loadApps(); }
            @Override
            public void onPackagesAvailable(String[] packageNames, android.os.UserHandle user, boolean replacing) { loadApps(); }
            @Override
            public void onPackagesUnavailable(String[] packageNames, android.os.UserHandle user, boolean replacing) { loadApps(); }
        };

        launcherApps.registerCallback(callback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        launcherApps.unregisterCallback(callback);
    }

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
        loadApps();
    }

    private void loadApps() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> hidden = prefs.getStringSet("hidden", new HashSet<>());
        String myPackage = getPackageName();
        apps = new ArrayList<>();
        
        List<LauncherActivityInfo> list = launcherApps.getActivityList(null, android.os.Process.myUserHandle());
        if (list != null) {
            for (LauncherActivityInfo info : list) {
                String pkg = info.getApplicationInfo().packageName;
                if (!hidden.contains(pkg) && !pkg.equals(myPackage)) {
                    apps.add(info);
                }
            }
        }
        runOnUiThread(() -> gridView.setAdapter(new AppAdapter()));
    }

    private class AppAdapter extends BaseAdapter {
        @Override public int getCount() { return apps.size(); }
        @Override public Object getItem(int position) { return apps.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layout = new LinearLayout(LauncherActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setPadding(16, 16, 16, 16);
            
            ImageView icon = new ImageView(LauncherActivity.this);
            icon.setLayoutParams(new LinearLayout.LayoutParams(140, 140));
            icon.setImageDrawable(apps.get(position).getIcon(0));

            TextView text = new TextView(LauncherActivity.this);
            text.setText(apps.get(position).getLabel());
            text.setTextColor(Color.WHITE);
            text.setSingleLine(true);
            
            layout.addView(icon);
            layout.addView(text);
            return layout;
        }
    }
}
