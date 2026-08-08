package fake.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
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
    private UserManager userManager;
    private List<LauncherActivityInfo> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
        userManager = (UserManager) getSystemService(Context.USER_SERVICE);

        gridView = new GridView(this);
        gridView.setNumColumns(4);
        gridView.setBackgroundColor(Color.TRANSPARENT);

        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            startActivity(new Intent(LauncherActivity.this, SettingsActivity.class));
            return true;
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            LauncherActivityInfo info = apps.get(position);
            launcherApps.startMainActivity(info.getComponentName(), info.getUser(), null, null);
        });

        setContentView(gridView);
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
        for (UserHandle user : userManager.getUserProfiles()) {
            List<LauncherActivityInfo> activityList = launcherApps.getActivityList(null, user);
            for (LauncherActivityInfo info : activityList) {
                if (!hidden.contains(info.getApplicationInfo().packageName)) {
                    apps.add(info);
                }
            }
        }
        gridView.setAdapter(new AppAdapter());
    }

    private class AppAdapter extends BaseAdapter {
        @Override
        public int getCount() { return apps.size(); }
        @Override
        public Object getItem(int position) { return apps.get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layout = new LinearLayout(LauncherActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setPadding(20, 20, 20, 20);

            LauncherActivityInfo info = apps.get(position);

            ImageView icon = new ImageView(LauncherActivity.this);
            icon.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            icon.setImageDrawable(info.getIcon(0));

            TextView text = new TextView(LauncherActivity.this);
            text.setText(info.getLabel());
            text.setTextColor(Color.WHITE);
            text.setGravity(Gravity.CENTER);

            layout.addView(icon);
            layout.addView(text);
            return layout;
        }
    }
}
