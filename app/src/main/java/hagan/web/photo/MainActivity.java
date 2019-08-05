package hagan.web.photo;

import android.Manifest;
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
import hagan.web.photo.utils.ToastUtil;
import hagan.web.photo.utils.permission.PermissionCallback;
import hagan.web.photo.utils.permission.PermissionDialogUtil;
import hagan.web.photo.utils.permission.PermissionSystemSettingCallback;
import hagan.web.photo.utils.permission.PermissionUtil;
import hagan.web.photo.widget.PromptButton;
import hagan.web.photo.widget.PromptDialog;

public class MainActivity extends AppCompatActivity implements OpenFileChooserCallBack {

    private WebView webView;
    private static final int REQUEST_CODE_PICK_IMAGE = 0x1111;
    private static final int REQUEST_CODE_TAKE_CAMERA = 0x2222;
    private Intent mSourceIntent;
    //针对5.0以下版本
    private ValueCallback<Uri> mUploadMessage;
    //这对5.0以上版本
    private ValueCallback<Uri[]> filePathCallback;
    private File tempFile;
    //选择照片和相机弹窗
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
        mUploadMessage = uploadMsg;
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
        if (requestCode == REQUEST_CODE_PICK_IMAGE ||
                requestCode == REQUEST_CODE_TAKE_CAMERA) {
            if (null == mUploadMessage && filePathCallback == null)
                return;
            if (resultCode == RESULT_CANCELED) {
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                    mUploadMessage = null;
                }
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                    filePathCallback = null;
                }
            } else if (resultCode == RESULT_OK) {
                Uri result = null;
                if (intent != null && intent.getData() != null) {
                    result = intent.getData();
                }
                if (result == null) {
                    if (photoUri != null) {
                        result = photoUri;
                    }
                }
                Uri[] uris = new Uri[1];
                uris[0] = result;
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                }

                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(uris);
                    mUploadMessage = null;
                }
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
                        cameraPermissionCheck();
                        break;
                    case ALBUM_ID:
                        photoPermissionCheck();
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

    /**
     * @description 打开相册
     * @author HaganWu
     * @data 2019/8/2-16:27
     */
    private void gotoPhoto() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(albumIntent, "请选择图片"), REQUEST_CODE_PICK_IMAGE);
    }

    /**
     * @description 打开相机
     * @author HaganWu
     * @data 2019/8/2-16:27
     */
    private void gotoCamera() {
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
    }


    private void cancelOperation() {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (promptDialog != null && promptDialog.isShowing()) {
            cancelOperation();
            promptDialog.dismiss();
            return;
        }
        super.onBackPressed();
    }


    /**
     * @description 相机权限判断
     * @author HaganWu
     * @data 2019/8/2-16:13
     */
    private void cameraPermissionCheck() {
        PermissionUtil.create(MainActivity.this).checkCameraePermission(new PermissionCallback() {
            @Override
            public void onClose() {
                Log.e("hagan", "onClose onClose");
                cancelOperation();
            }

            @Override
            public void onFinish() {
                Log.e("hagan", "相机权限判断 onFinish onFinish");
            }

            @Override
            public void onDeny(String permission, int position) {
                Log.e("hagan", "相机权限判断 onDeny!!!!!!!!!!!!!!!!!!!!!!");
                //拒绝权限
                PermissionDialogUtil.create(MainActivity.this)
                        .startSystemPermissionSetting(getString(R.string.permission_apply), getString(R.string.open_camera_permission_tip),
                                Manifest.permission.CAMERA, new PermissionSystemSettingCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.e("hagan", "相机权限判断 onSuccess onSuccess");
                                        gotoCamera();
                                    }

                                    @Override
                                    public void onFail() {
                                        Log.e("hagan", "相机权限判断 onFail onFail");
                                        cancelOperation();
                                        ToastUtil.showShort(getString(R.string.not_get_permission));
                                    }
                                });
            }

            @Override
            public void onGuarantee(String permission, int position, boolean lastOne) {
                //允许权限 跳转到调用系统相机
                Log.e("hagan", "onGuarantee onGuarantee lastOne:" + lastOne);
                if (lastOne) {
                    gotoCamera();
                }
            }
        });
    }

    /**
     * @description 相册权限判断
     * @author HaganWu
     * @data 2019/8/2-16:27
     */
    private void photoPermissionCheck() {
        PermissionUtil.create(MainActivity.this).checkSinglePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
            @Override
            public void onClose() {
            }

            @Override
            public void onFinish() {
                gotoPhoto();
            }

            @Override
            public void onDeny(String permission, int position) {
                PermissionDialogUtil.create(MainActivity.this)
                        .startSystemPermissionSetting(getString(R.string.permission_apply), getString(R.string.open_photo_permission_tip),
                                Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionSystemSettingCallback() {
                                    @Override
                                    public void onSuccess() {
                                        gotoPhoto();
                                    }

                                    @Override
                                    public void onFail() {
                                        cancelOperation();
                                        ToastUtil.showShort(getString(R.string.not_get_permission));
                                    }
                                });
            }

            @Override
            public void onGuarantee(String permission, int position, boolean lastOne) {
                //允许相册权限 跳转到相册
                if (lastOne) {
                    gotoPhoto();
                }
            }
        });
    }

}
