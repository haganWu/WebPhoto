package hagan.web.photo.utils;


import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * @author HaganWu
 * @description
 * @fileName FileUtil.java
 * @data 2019/7/31-13:18
 */
public class FileUtil {

    public static final class PATH {
        private final static String PATH_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
        private final static String PATH_PHONE = UIUtil.getContext().getFilesDir().getAbsolutePath();
        private final static String PACKAGE_NAME = UIUtil.getContext().getPackageName();
        // SD卡根路径
        public final static String PATH_BASE_FILE = getRootFile() + "test/";
        // 图片路径
        public final static String ETC_PHOTO = PATH_BASE_FILE + PACKAGE_NAME + "/pic" + File.separator;

    }

    private static String getRootFile() {
        if (hasSDCard()) {
            return PATH.PATH_SDCARD + File.separator;
        } else {
            return PATH.PATH_PHONE + File.separator;
        }
    }

    public static boolean hasSDCard() {
        String SDState = Environment.getExternalStorageState();

        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(PATH.PATH_SDCARD);
            if (!file.exists()) {
                return file.mkdirs();
            }
            return true;

        }
        return false;
    }

    public static File createFile(String dirPath, String child) {
        File file = new File(FileUtil.checkDirPath(dirPath), child);
        return file;
    }

    /**
     * @description 检查文件是否存在
     * @author HaganWu
     * @data 2019/8/2-14:44
     */
    public static String checkDirPath(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return "";
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath;
    }

}
