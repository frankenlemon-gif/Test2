package fake.launcher;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context,"Device Admin Enabled", Toast.LENGTH_SHORT).show();
    }

	@Override
	public void onPasswordFailed(Context context, Intent intent)
	{
		Intent i = new Intent(context, LauncherActivity000.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
	}


    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context,"Device Admin Disabled", Toast.LENGTH_SHORT).show();
    }
}
