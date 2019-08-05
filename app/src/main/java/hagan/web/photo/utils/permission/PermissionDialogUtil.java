package hagan.web.photo.utils.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * @author HaganWu
 * @description
 * @fileName PermissionDialogUtil.java
 * @date 2018/9/5-16:02
 */
public class PermissionDialogUtil {

    private Context context;

    public PermissionDialogUtil(Context context) {
        this.context = context;
    }

    public static PermissionDialogUtil create(Activity activity) {
        return new PermissionDialogUtil(activity);
    }

    public void startSystemPermissionSetting(String dialogTitle, String dialogContent, String permissionName,
                                             PermissionSystemSettingCallback permissionSystemSettingCallback) {
        PermissionDialogActivity.setPermissionSystemSettingCallback(permissionSystemSettingCallback);
        Intent intent = new Intent(context, PermissionDialogActivity.class);
        intent.putExtra("dialogTitle", dialogTitle);
        intent.putExtra("dialogContent", dialogContent);
        intent.putExtra("permissionName", permissionName);
        context.startActivity(intent);
    }

}
