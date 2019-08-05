package hagan.web.photo.utils.permission;

import java.io.Serializable;

/**
 * @description
 * @author HaganWu
 * @date 2018/9/5-16:02
 */
public class PermissionItem implements Serializable {
    public String PermissionName;
    public String Permission;
    public int PermissionIconRes;

    public PermissionItem(String permission, String permissionName, int permissionIconRes) {
        Permission = permission;
        PermissionName = permissionName;
        PermissionIconRes = permissionIconRes;
    }

    public PermissionItem(String permission) {
        Permission = permission;
    }
}
