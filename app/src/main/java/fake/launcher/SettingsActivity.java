package fake.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends Activity {

    private Set<String> hiddenApps;
    private List<ResolveInfo> allApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        hiddenApps = new HashSet<>(prefs.getStringSet("hidden", new HashSet<>()));

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        allApps = getPackageManager().queryIntentActivities(mainIntent, 0);

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

    private class SettingsAdapter extends ArrayAdapter<ResolveInfo> {
        public SettingsAdapter() {
            super(SettingsActivity.this, android.R.layout.simple_list_item_multiple_choice, allApps);
        }

        @Override
        public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
            CheckBox checkBox = new CheckBox(SettingsActivity.this);
            ResolveInfo info = getItem(position);
            String pkg = info.activityInfo.packageName;

            checkBox.setText(info.loadLabel(getPackageManager()));
            checkBox.setChecked(hiddenApps.contains(pkg));
            checkBox.setOnClickListener(v -> {
                if (checkBox.isChecked()) hiddenApps.add(pkg);
                else hiddenApps.remove(pkg);
            });
            return checkBox;
        }
    }
}
