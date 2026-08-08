package fake.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        gridView = new GridView(this);
        gridView.setNumColumns(4);
        gridView.setBackgroundColor(Color.TRANSPARENT);

        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            LauncherActivityInfo info = apps.get(position);
            launcherApps.startMainActivity(info.getComponentName(), info.getUser(), null, null);
        });

        callback = new LauncherApps.Callback() {
            @Override
            public void onPackageAdded(String packageName, UserHandle user) { loadApps(); }
            @Override
            public void onPackageRemoved(String packageName, UserHandle user) { loadApps(); }
            @Override
            public void onPackageChanged(String packageName, UserHandle user) { loadApps(); }
            @Override
            public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) { loadApps(); }
            @Override
            public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) { loadApps(); }
        };

        launcherApps.registerCallback(callback);
        setContentView(gridView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        launcherApps.unregisterCallback(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApps();
    }

    private void loadApps() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> hidden = prefs.getStringSet("hidden", new HashSet<>());
        apps = new ArrayList<>();
        UserManager um = (UserManager) getSystemService(Context.USER_SERVICE);

        for (UserHandle user : um.getUserProfiles()) {
            List<LauncherActivityInfo> list = launcherApps.getActivityList(null, user);
            for (LauncherActivityInfo info : list) {
                if (!hidden.contains(info.getApplicationInfo().packageName)) {
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
            
            ImageView icon = new ImageView(LauncherActivity.this);
            icon.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            icon.setImageDrawable(apps.get(position).getIcon(0));

            TextView text = new TextView(LauncherActivity.this);
            text.setText(apps.get(position).getLabel());
            text.setTextColor(Color.WHITE);
            
            layout.addView(icon);
            layout.addView(text);
            return layout;
        }
    }
}
