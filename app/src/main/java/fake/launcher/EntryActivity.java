package com.example.mylauncher;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LauncherProxyActivity extends Activity {
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
            b.setOnClickListener(v -> {
                RoleManager rm = getSystemService(RoleManager.class);
                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    startActivity(rm.createRequestRoleIntent(RoleManager.ROLE_HOME));
                } else {
                    startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
                }
            });
            
            l.addView(t);
            l.addView(b);
            setContentView(l);
        }
    }

    private boolean isDefault() {
        RoleManager rm = getSystemService(RoleManager.class);
        return rm != null && rm.isRoleHeld(RoleManager.ROLE_HOME);
    }
}
