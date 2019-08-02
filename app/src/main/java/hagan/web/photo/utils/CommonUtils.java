package hagan.web.photo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {
    public static String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "IMG_" + dateFormat.format(date);
    }
}
