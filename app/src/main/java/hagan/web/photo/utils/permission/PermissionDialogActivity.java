package hagan.web.photo.utils.permission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import hagan.web.photo.widget.CommonTipDialog;

/**
 * @author HaganWu
 * @description
 * @fileName PermissionDialogActivity.java
 * @data 2019/8/2-16:01
 */
public class PermissionDialogActivity extends AppCompatActivity {

    private static PermissionSystemSettingCallback mPermissionSystemSettingCallback;
    private String dialogTitle, dialogContent;
    private String permissionName;
    private CommonTipDialog commonTipDialog;
    private static final int REQUEST_SYSTEM_SETTING_PERMISSION_CODE = 0x1116;
    private boolean hadShowDialog;


    public static void setPermissionSystemSettingCallback(PermissionSystemSettingCallback permissionSystemSettingCallback) {
        PermissionDialogActivity.mPermissionSystemSettingCallback = permissionSystemSettingCallback;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (!hadShowDialog) {
                hadShowDialog = true;
                showSystemPermissionDialog();
            }else{
                dealBack();
            }
        }
    }


    public void dealBack() {
        super.onBackPressed();
        Log.e("hagan", "PermissionDialogActivity onBackPress");
        if (commonTipDialog != null && commonTipDialog.isShowing()) {
            commonTipDialog.dismiss();
        }
        if (mPermissionSystemSettingCallback != null) {
            mPermissionSystemSettingCallback.onFail();
        }
        PermissionDialogActivity.this.finish();
    }

    private void showSystemPermissionDialog() {
        if (commonTipDialog == null) {
            commonTipDialog = new CommonTipDialog(this);
        }
        commonTipDialog.setSingleButton(false);
        commonTipDialog.setTitle(dialogTitle);
        commonTipDialog.setContentText(dialogContent);
        commonTipDialog.setOnDialogButtonsClickListener(new CommonTipDialog.OnDialogButtonsClickListener() {
            @Override
            public void onCancelClick(View v) {
                mPermissionSystemSettingCallback.onFail();
                PermissionDialogActivity.this.finish();
            }

            @Override
            public void onConfirmClick(View v) {
                Intent systemSettingIntent = new Intent();
                if (Build.VERSION.SDK_INT >= 9) {
                    systemSettingIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    systemSettingIntent.setData(Uri.fromParts("package", getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    systemSettingIntent.setAction(Intent.ACTION_VIEW);
                    systemSettingIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    systemSettingIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
                }
                startActivityForResult(systemSettingIntent, REQUEST_SYSTEM_SETTING_PERMISSION_CODE);
            }
        });
        commonTipDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPermissionSystemSettingCallback = null;
        if (commonTipDialog != null && commonTipDialog.isShowing()) {
            commonTipDialog.dismiss();
        }
    }

    public void getData() {
        Intent intent = getIntent();
        dialogTitle = intent.getStringExtra("dialogTitle");
        dialogContent = intent.getStringExtra("dialogContent");
        permissionName = intent.getStringExtra("permissionName");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SYSTEM_SETTING_PERMISSION_CODE) {
            if (ContextCompat.checkSelfPermission(PermissionDialogActivity.this, permissionName)
                    == PackageManager.PERMISSION_GRANTED) {
                mPermissionSystemSettingCallback.onSuccess();
            } else {
                mPermissionSystemSettingCallback.onFail();
            }
            PermissionDialogActivity.this.finish();
        }


    }
}
