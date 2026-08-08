package fake.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.Intent;
import android.provider.Settings;

public class EntryActivity extends Activity {
    
    private AlertDialog dialog;

    @Override
    protected void onResume() {
        super.onResume();
        if (isDefault()) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            startActivity(new Intent(this, oLauncherActivity.class));
            finish();
        } else {
            if (dialog == null || !dialog.isShowing()) {
                dialog = new AlertDialog.Builder(this)
                        .setMessage("Please set launcher as default")
                        .setPositiveButton("Open Settings", (d, w) -> {
                            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));                            
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private boolean isDefault() {
        RoleManager rm = getSystemService(RoleManager.class);
        return rm != null && rm.isRoleHeld(RoleManager.ROLE_HOME);
    }
}
