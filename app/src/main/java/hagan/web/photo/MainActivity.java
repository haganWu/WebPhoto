package hagan.web.photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.web.photo.BuildConfig;
import com.web.photo.R;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import hagan.web.photo.constants.Constants;
import hagan.web.photo.interfaze.OpenFileChooserCallBack;
import hagan.web.photo.interfaze.PromptButtonListener;
import hagan.web.photo.utils.CommonUtils;
import hagan.web.photo.utils.FileUtil;
import hagan.web.photo.widget.PromptButton;
import hagan.web.photo.widget.PromptDialog;

public class MainActivity extends AppCompatActivity implements OpenFileChooserCallBack {

    private WebView webView;
    private static final int REQUEST_CODE_PICK_IMAGE = 0x1111;
    private static final int REQUEST_CODE_TAKE_CAMERA = 0x2222;
    private Intent mSourceIntent;
    //针对5.0以下版本
    private ValueCallback<Uri> mUploadMsg;
    //这对5.0以上版本
    private ValueCallback<Uri[]> filePathCallback;
    private File tempFile;
    private PromptDialog promptDialog;
    private final int CAMERA_ID = 0x0001;
    private final int ALBUM_ID = 0x0002;
    private final int CANCEL_ID = 0x0003;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);
        Log.e("hagan", "onCreate onCreate");
        initWebView();

    }


    public void initWebView() {
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new MyWebChromeClient(this));
        setWebViewInitialScale();
        webView.loadUrl("file:///android_asset/test.html");

    }

    private void setWebViewInitialScale() {
        WindowManager wm = (WindowManager) MainActivity.this.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        if (width > 650) {
            this.webView.setInitialScale(320);
        } else if (width > 520) {
            this.webView.setInitialScale(300);
        } else if (width > 450) {
            this.webView.setInitialScale(280);
        } else if (width > 300) {
            this.webView.setInitialScale(260);
        } else {
            this.webView.setInitialScale(240);
        }
    }


    @Override
    public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {
        Log.e("hagan", "openFileChooserCallBack");
        mUploadMsg = uploadMsg;
        showOptions();
    }

    @Override
    public void openFileChooser5CallBack(WebView webView, ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        Log.e("hagan", "openFileChooser5CallBack");
        filePathCallback = valueCallback;
        showOptions();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_CANCELED) {
            if (mUploadMsg != null) {
                mUploadMsg.onReceiveValue(null);
                mUploadMsg = null;
            }
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(null);
                filePathCallback = null;
            }
            return;
        }
        if (requestCode == REQUEST_CODE_TAKE_CAMERA || requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (null == mUploadMsg && filePathCallback == null)
                return;
            Uri[] uris = new Uri[1];
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
            uris[0] = result;
            if (mUploadMsg != null) {
                mUploadMsg.onReceiveValue(result);
                mUploadMsg = null;
            }

            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(uris);
                mUploadMsg = null;
            }

        }
    }

    public void showOptions() {

        if (promptDialog == null) {
            promptDialog = new PromptDialog(this);
        }
        PromptButtonListener promptButtonListener = new PromptButtonListener() {
            @Override
            public void onClick(PromptButton button) {
                switch (button.getId()) {
                    case CAMERA_ID:
                        //创建拍照存储的图片文件
                        String fileName = CommonUtils.getPhotoFileName() + Constants.PICTURE_NAME;
                        tempFile = FileUtil.createFile(FileUtil.PATH.ETC_PHOTO, fileName);
                        //跳转到调用系统相机
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            //设置7.0中共享文件，分享路径定义在xml/file_paths.xml
                            cameraIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            photoUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", tempFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        } else {
                            photoUri = Uri.fromFile(tempFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        }
                        startActivityForResult(Intent.createChooser(cameraIntent, "拍照"), REQUEST_CODE_TAKE_CAMERA);
                        break;
                    case ALBUM_ID:
                        Intent albumIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(Intent.createChooser(albumIntent, "请选择图片"), REQUEST_CODE_PICK_IMAGE);
                        break;
                    case CANCEL_ID:
                        cancelOperation();
                        break;
                }
            }
        };
        PromptButton cancel = new PromptButton(CANCEL_ID, "取消", promptButtonListener);
        cancel.setTextColor(Color.parseColor("#0076ff"));
        promptDialog.showAlertSheet("", true, cancel,
                new PromptButton(CAMERA_ID, "拍照", promptButtonListener),
                new PromptButton(ALBUM_ID, "从相册选择", promptButtonListener));
    }


    private void cancelOperation() {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
    }
}
