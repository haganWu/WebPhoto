package hagan.web.photo.utils.permission;

import java.io.Serializable;

/**
 * @description
 * @author HaganWu
 * @date 2018/9/5-9:17
 */
public interface PermissionCallback extends Serializable {
    void onClose();

    void onFinish(boolean doNext);

    void onDeny(String permission, int position);

    void onGuarantee(String permission, int position,boolean lastOne);
}
