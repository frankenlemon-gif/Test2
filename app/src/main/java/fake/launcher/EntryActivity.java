package fake.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EntryActivity extends Activity {
    @Override
    protected void onResume() {
        super.onResume();
        if (isDefault()) {
            startActivity(new Intent(this, LauncherActivity.class));
            finish();
        } else if (findViewById(android.R.id.content) == null) {
            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.VERTICAL);
            l.setGravity(Gravity.CENTER);
            
            TextView t = new TextView(this);
            t.setText("Please set launcher as default");
            t.setPadding(0, 0, 0, 50);
            
            Button b = new Button(this);
            b.setText("Open Settings");
            b.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)));
            
            l.addView(t);
            l.addView(b);
            setContentView(l);
        }
    }

    private boolean isDefault() {
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo r = getPackageManager().resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY);
        return r != null && getPackageName().equals(r.activityInfo.packageName);
    }
}
