package hagan.web.photo.interfaze;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebView;


public interface OpenFileChooserCallBack {
    void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType);

    void openFileChooser5CallBack(WebView webView, ValueCallback<Uri[]> valueCallback,
                                  android.webkit.WebChromeClient.FileChooserParams fileChooserParams);
}
