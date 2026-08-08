package fake.launcher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends Activity {

    private Set<String> hiddenApps;
    private List<LauncherActivityInfo> allApps;
    private SharedPreferences prefs;
    private int finalPadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        hiddenApps = new HashSet<>(prefs.getStringSet("hidden", new HashSet<>()));

        LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
        UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);

        allApps = new ArrayList<>();
        for (UserHandle user : userManager.getUserProfiles()) {
            allApps.addAll(launcherApps.getActivityList(null, user));
        }

        float density = getResources().getDisplayMetrics().density;
        finalPadding = (int) (44 * density);

        showMainMenu();
    }

    private void showMainMenu() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.MATCH_PARENT));
        root.setPadding(0, finalPadding, 0, finalPadding);
        root.setClipToPadding(false);

        Button btnHideApps = new Button(this);
        btnHideApps.setText("Hide apps");
        btnHideApps.setOnClickListener(v -> showHideAppsMenu());

        Button btnWallpaper = new Button(this);
        btnWallpaper.setText("Select wallpaper");
        btnWallpaper.setOnClickListener(v -> showWallpaperMenu());

        Button btnBack = new Button(this);
        btnBack.setText("Back");
        btnBack.setOnClickListener(v -> finish());

        root.addView(btnHideApps);
        root.addView(btnWallpaper);
        root.addView(btnBack);

        setContentView(root);
    }

    private void showHideAppsMenu() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, finalPadding, 0, finalPadding);
        root.setClipToPadding(false);

        ListView listView = new ListView(this);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
        listView.setAdapter(new SettingsAdapter());

        Button saveButton = new Button(this);
        saveButton.setText("Hide selected");
        saveButton.setOnClickListener(v -> prefs.edit().putStringSet("hidden", hiddenApps).apply());

        Button backButton = new Button(this);
        backButton.setText("Back");
        backButton.setOnClickListener(v -> showMainMenu());

        root.addView(listView);
        root.addView(saveButton);
        root.addView(backButton);

        setContentView(root);
    }

    private void showWallpaperMenu() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, finalPadding, 0, finalPadding);
        root.setClipToPadding(false);

        Button btnChoose = new Button(this);
        btnChoose.setText("Choose from gallery");
        btnChoose.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, 1001);
        });

        Button backButton = new Button(this);
        backButton.setText("Back");
        backButton.setOnClickListener(v -> showMainMenu());

        root.addView(btnChoose);
        root.addView(backButton);

        setContentView(root);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        if (bitmap != null) {
                            wallpaperManager.setBitmap(bitmap, null, true, 
                                    WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                        }
                    }
                } catch (Exception e) {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        }
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
