package fake.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
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
    private PackageManager packageManager;
    private List<ResolveInfo> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        packageManager = getPackageManager();
        gridView = new GridView(this);
        gridView.setNumColumns(4);
        gridView.setBackgroundColor(Color.TRANSPARENT);
        gridView.setOnLongClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            ResolveInfo info = apps.get(position);
            Intent intent = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName);
            if (intent != null) startActivity(intent);
        });

        setContentView(gridView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApps();
    }

    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allApps = packageManager.queryIntentActivities(mainIntent, 0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> hidden = prefs.getStringSet("hidden", new HashSet<>());

        apps = new ArrayList<>();
        for (ResolveInfo info : allApps) {
            if (!hidden.contains(info.activityInfo.packageName)) {
                apps.add(info);
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

            ImageView icon = new ImageView(LauncherActivity.this);
            icon.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            icon.setImageDrawable(apps.get(position).loadIcon(packageManager));

            TextView text = new TextView(LauncherActivity.this);
            text.setText(apps.get(position).loadLabel(packageManager));
            text.setTextColor(Color.WHITE);
            text.setGravity(Gravity.CENTER);

            layout.addView(icon);
            layout.addView(text);
            return layout;
        }
    }
}
