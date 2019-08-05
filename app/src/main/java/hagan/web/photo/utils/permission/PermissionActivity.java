package hagan.web.photo.utils.permission;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.web.photo.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import hagan.web.photo.constants.Constants;


/**
 * @author HaganWu
 * @description
 * @fileName PermissionActivity.java
 * @date 2018/9/5-16:00
 */
public class PermissionActivity extends AppCompatActivity {

    private String TAG = "hagan";
    public static int PERMISSION_TYPE_SINGLE = 1;
    public static int PERMISSION_TYPE_MUTI = 2;
    private int mPermissionType;
    private String mTitle;
    private String mMsg;
    private static PermissionCallback mCallback;
    private List<PermissionItem> mCheckPermissions;
    private Dialog mDialog;

    private static final int REQUEST_CODE_SINGLE = 1;
    private static final int REQUEST_CODE_MUTI = 2;

    private int mStyleId;
    private int mFilterColor;
    private int mAnimStyleId;
    private String titlePermissionName;

    /**
     * 重新申请权限数组的索引
     */
    public static void setCallBack(PermissionCallback callBack) {
        PermissionActivity.mCallback = callBack;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCallback = null;
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDatas();
        if (mPermissionType == PERMISSION_TYPE_SINGLE) {
            //单个权限申请
            if (mCheckPermissions == null || mCheckPermissions.size() == 0)
                return;

            requestPermission(new String[]{mCheckPermissions.get(0).Permission}, REQUEST_CODE_SINGLE);
        } else {
            //多个权限
            showPermissionDialog();
        }
    }


    private String getPermissionTitle() {
        return TextUtils.isEmpty(mTitle) ? String.format(getString(R.string.permission_dialog_title), TextUtils.isEmpty(titlePermissionName) ? "" : titlePermissionName) : mTitle;
    }

    private void showPermissionDialog() {

        String title = getPermissionTitle();
        String msg = TextUtils.isEmpty(mMsg) ? String.format(getString(R.string.permission_dialog_msg), TextUtils.isEmpty(titlePermissionName) ? "" : titlePermissionName) : mMsg;

        PermissionView contentView = new PermissionView(this);
        contentView.setGridViewColum(mCheckPermissions.size() < 3 ? mCheckPermissions.size() : 3);
        contentView.setTitle(title);
        contentView.setMsg(msg);
        //这里没有使用RecyclerView，可以少引入一个库
        contentView.setGridViewAdapter(new PermissionAdapter(mCheckPermissions));
        if (mStyleId == -1) {
            //用户没有设置，使用默认主题
            mStyleId = R.style.PermissionDefaultNormalStyle;
            mFilterColor = getResources().getColor(R.color.blue_color);
        }

        contentView.setStyleId(mStyleId);
        contentView.setFilterColor(mFilterColor);
        contentView.setBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();
                String[] strs = getPermissionStrArray();
                ActivityCompat.requestPermissions(PermissionActivity.this, strs, REQUEST_CODE_MUTI);
            }
        });
        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(contentView);
        if (mAnimStyleId != -1)
            mDialog.getWindow().setWindowAnimations(mAnimStyleId);

        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                if (mCallback != null)
                    mCallback.onClose();
                finish();
            }
        });
        mDialog.show();
    }


    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(PermissionActivity.this, permissions, requestCode);
    }

    private String[] getPermissionStrArray() {
        String[] str = new String[mCheckPermissions.size()];
        for (int i = 0; i < mCheckPermissions.size(); i++) {
            str[i] = mCheckPermissions.get(i).Permission;
        }
        return str;
    }


    private void getDatas() {
        Intent intent = getIntent();
        mPermissionType = intent.getIntExtra(Constants.DATA_PERMISSION_TYPE, PERMISSION_TYPE_SINGLE);
        titlePermissionName = intent.getStringExtra(Constants.DATA_PERMISSION_TITLE_NAME);
        mTitle = intent.getStringExtra(Constants.DATA_TITLE);
        mMsg = intent.getStringExtra(Constants.DATA_MSG);
        mFilterColor = intent.getIntExtra(Constants.DATA_FILTER_COLOR, 0);
        mStyleId = intent.getIntExtra(Constants.DATA_STYLE_ID, -1);
        mAnimStyleId = intent.getIntExtra(Constants.DATA_ANIM_STYLE, -1);
        mCheckPermissions = (List<PermissionItem>) intent.getSerializableExtra(Constants.DATA_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_SINGLE:
                String permission = getPermissionItem(permissions[0]).Permission;
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onGuarantee(permission, 0, true);
                } else {
                    onDeny(permission, 0);
                }
                finish();
                break;
            case REQUEST_CODE_MUTI:
                Log.e(TAG, "请求多个权限处理 REQUEST_CODE_MUTI");
                boolean hadDeny = false;
                for (int i = 0; i < grantResults.length; i++) {
                    //权限允许后，删除需要检查的权限
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        PermissionItem item = getPermissionItem(permissions[i]);
                        mCheckPermissions.remove(item);
                        onGuarantee(permissions[i], i, mCheckPermissions.size() == 0);
                        if (mCheckPermissions.size() == 0) {
                            Log.e(TAG, "mCheckPermissions.size() == 0   ----->   finish()");
                            finish();
                        }
                    } else {
                        //权限拒绝
                        onDeny(permissions[i], i);
                        hadDeny = true;
                        break;
                    }
                }
                if (hadDeny) {
                    Log.e(TAG, "请求多个权限当出现Deny时关闭PermissionActivity");
                    onFinish(false);
                }
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    private void onFinish(boolean doNext) {
        if (mCallback != null)
            mCallback.onFinish(doNext);
        finish();
    }


    private void onDeny(String permission, int position) {
        if (mCallback != null)
            mCallback.onDeny(permission, position);
    }

    private void onGuarantee(String permission, int position, boolean lastOne) {
        if (mCallback != null)
            mCallback.onGuarantee(permission, position, lastOne);
    }

    private PermissionItem getPermissionItem(String permission) {
        for (PermissionItem permissionItem : mCheckPermissions) {
            if (permissionItem.Permission.equals(permission))
                return permissionItem;
        }
        return null;
    }
}
