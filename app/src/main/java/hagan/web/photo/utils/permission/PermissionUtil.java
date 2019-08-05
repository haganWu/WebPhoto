package hagan.web.photo.utils.permission;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.web.photo.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.core.content.ContextCompat;
import hagan.web.photo.constants.Constants;

/**
 * @author HaganWu
 * @description
 * @fileName PermissionUtil.java
 * @date 2018/9/5-16:02
 */
public class PermissionUtil {

    private final Context mContext;
    private String mTitle;
    private String mMsg;
    private int mStyleResId = -1;
    private PermissionCallback mCallback;
    private List<PermissionItem> mCheckPermissions;
    private int mPermissionType;
    private String titlePermissionName;
    private String[] mNormalPermissionNames;
    private String[] mNormalPermissions = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CAMERA};
    private int[] mNormalPermissionIconRes = {
            R.mipmap.permission_ic_storage, R.mipmap.permission_ic_location, R.mipmap.permission_ic_camera};


    private String[] mCameraPermissionNames;
    private String[] mCameraPermissions = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA};
    private int[] mCameraPermissionIconRes = {
            R.mipmap.permission_ic_storage, R.mipmap.permission_ic_camera};

    private int mFilterColor = 0;
    private int mAnimStyleId = -1;

    public static PermissionUtil create(Context context) {
        return new PermissionUtil(context);
    }

    public PermissionUtil(Context context) {
        mContext = context;
        mNormalPermissionNames = mContext.getResources().getStringArray(R.array.normalPermissionNames);
        mCameraPermissionNames = mContext.getResources().getStringArray(R.array.cameraPermissionNames);

    }

    public PermissionUtil title(String title) {
        mTitle = title;
        return this;
    }

    public PermissionUtil msg(String msg) {
        mMsg = msg;
        return this;
    }

    public PermissionUtil permissions(List<PermissionItem> permissionItems) {
        mCheckPermissions = permissionItems;
        return this;
    }

    public PermissionUtil filterColor(int color) {
        mFilterColor = color;
        return this;
    }

    public PermissionUtil animStyle(int styleId) {
        mAnimStyleId = styleId;
        return this;
    }

    public PermissionUtil style(int styleResIdsId) {
        mStyleResId = styleResIdsId;
        return this;
    }

    private List<PermissionItem> getNormalPermissions() {
        List<PermissionItem> permissionItems = new ArrayList<>();
        for (int i = 0; i < mNormalPermissionNames.length; i++) {
            permissionItems.add(new PermissionItem(mNormalPermissions[i], mNormalPermissionNames[i], mNormalPermissionIconRes[i]));
        }
        return permissionItems;
    }

    private List<PermissionItem> getCameraePermissions() {
        List<PermissionItem> permissionItems = new ArrayList<>();
        for (int i = 0; i < mCameraPermissionNames.length; i++) {
            permissionItems.add(new PermissionItem(mCameraPermissions[i], mCameraPermissionNames[i], mCameraPermissionIconRes[i]));
        }
        return permissionItems;
    }

    public static boolean checkPermission(Context context, String permission) {
        int checkPermission = ContextCompat.checkSelfPermission(context, permission);
        Log.e("hagan", "checkPermission 检测是否已经开启了权限 permission:" + permission + ";checkPermission:" + checkPermission);
        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    /**
     * @description 检查多个权限
     * @author HaganWu
     * @date 2018/9/5-9:22
     */
    public void checkMultiplePermission(PermissionCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (callback != null)
                callback.onFinish(true);
            return;
        }

        if (mCheckPermissions == null) {
            mCheckPermissions = new ArrayList<>();
        }
        mCheckPermissions.clear();
        mCheckPermissions.addAll(getNormalPermissions());
        //检查权限，过滤已允许的权限
        Iterator<PermissionItem> iterator = mCheckPermissions.listIterator();
        while (iterator.hasNext()) {
            if (checkPermission(mContext, iterator.next().Permission))
                iterator.remove();
        }
        mCallback = callback;
        mPermissionType = PermissionActivity.PERMISSION_TYPE_MUTI;
        titlePermissionName = "";
        if (mCheckPermissions.size() > 0) {
            startActivity();
        } else {
            if (callback != null)
                callback.onFinish(true);
        }
    }

    /**
     * @description 检测多个权限
     * @author HaganWu
     * @data 2019/8/5-10:32
     */
    public void checkCameraePermission(PermissionCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (callback != null)
                callback.onFinish(true);
            return;
        }

        if (mCheckPermissions == null) {
            mCheckPermissions = new ArrayList<>();
        }
        mCheckPermissions.clear();
        mCheckPermissions.addAll(getCameraePermissions());

        //检查权限，过滤已允许的权限
        Iterator<PermissionItem> iterator = mCheckPermissions.listIterator();
        while (iterator.hasNext()) {
            if (checkPermission(mContext, iterator.next().Permission))
                iterator.remove();
        }
        mCallback = callback;
        mPermissionType = PermissionActivity.PERMISSION_TYPE_MUTI;
        titlePermissionName = "相机";
        if (mCheckPermissions.size() > 0) {
            startActivity();
        } else {
            if (callback != null) {
                callback.onFinish(true);
            }

        }
    }


    /**
     * @description 检查单个权限
     * @author HaganWu
     * @date 2018/9/5-9:24
     */
    public void checkSinglePermission(String permission, PermissionCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermission(mContext, permission)) {
            if (callback != null)
                callback.onGuarantee(permission, 0, true);
            return;
        }
        mCallback = callback;
        mPermissionType = PermissionActivity.PERMISSION_TYPE_SINGLE;
        titlePermissionName = "相册";
        mCheckPermissions = new ArrayList<>();
        mCheckPermissions.add(new PermissionItem(permission));
        startActivity();
    }

    private void startActivity() {
        PermissionActivity.setCallBack(mCallback);
        Intent intent = new Intent(mContext, PermissionActivity.class);
        intent.putExtra(Constants.DATA_TITLE, mTitle);
        intent.putExtra(Constants.DATA_PERMISSION_TYPE, mPermissionType);
        intent.putExtra(Constants.DATA_PERMISSION_TITLE_NAME, titlePermissionName);
        intent.putExtra(Constants.DATA_MSG, mMsg);
        intent.putExtra(Constants.DATA_FILTER_COLOR, mFilterColor);
        intent.putExtra(Constants.DATA_STYLE_ID, mStyleResId);
        intent.putExtra(Constants.DATA_ANIM_STYLE, mAnimStyleId);
        intent.putExtra(Constants.DATA_PERMISSIONS, (Serializable) mCheckPermissions);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
