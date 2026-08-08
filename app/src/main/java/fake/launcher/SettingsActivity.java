package fake.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends Activity {

    private Set<String> hiddenApps;
    private List<LauncherActivityInfo> allApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        hiddenApps = new HashSet<>(prefs.getStringSet("hidden", new HashSet<>()));

        LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
        UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);

        allApps = new ArrayList<>();
        for (UserHandle user : userManager.getUserProfiles()) {
            allApps.addAll(launcherApps.getActivityList(null, user));
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        ListView listView = new ListView(this);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        Button saveButton = new Button(this);
        saveButton.setText("Hide selected");
        saveButton.setOnClickListener(v -> {
            prefs.edit().putStringSet("hidden", hiddenApps).apply();
            finish();
        });

        listView.setAdapter(new SettingsAdapter());

        root.addView(listView);
        root.addView(saveButton);
        setContentView(root);
    }

    private class SettingsAdapter extends ArrayAdapter<LauncherActivityInfo> {
        public SettingsAdapter() {
            super(SettingsActivity.this, android.R.layout.simple_list_item_multiple_choice, allApps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckBox checkBox = new CheckBox(SettingsActivity.this);
            LauncherActivityInfo info = getItem(position);
            String pkg = info.getApplicationInfo().packageName;

            checkBox.setText(info.getLabel());
            checkBox.setChecked(hiddenApps.contains(pkg));
            checkBox.setOnClickListener(v -> {
                if (checkBox.isChecked()) hiddenApps.add(pkg);
                else hiddenApps.remove(pkg);
            });
            return checkBox;
        }
    }
}
