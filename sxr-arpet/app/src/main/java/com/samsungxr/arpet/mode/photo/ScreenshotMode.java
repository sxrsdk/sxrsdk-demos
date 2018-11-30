/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsungxr.arpet.mode.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.samsungxr.SXRCameraRig;
import com.samsungxr.arpet.BuildConfig;
import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.R;
import com.samsungxr.arpet.context.ActivityResultEvent;
import com.samsungxr.arpet.context.RequestPermissionResultEvent;
import com.samsungxr.arpet.mode.BasePetMode;
import com.samsungxr.arpet.mode.OnBackToHudModeListener;
import com.samsungxr.arpet.util.EventBusUtils;
import com.samsungxr.arpet.util.StorageUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.util.AsyncExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenshotMode extends BasePetMode {

    private static final String TAG = ScreenshotMode.class.getSimpleName();

    private static final String PACK_NAME_FACEBOOK = "com.facebook.katana";
    private static final String PACK_NAME_TWITTER = "";
    private static final String PACK_NAME_INSTAGRAM = "com.instagram.android";
    private static final String PACK_NAME_WHATSAPP = "com.whatsapp";

    private static final String ACTIVITY_SHARE_PICTURE =
            "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias";

    private static final String APP_PHOTOS_DIR_NAME = "sxr-arpet";
    private static final String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    private static final int REQUEST_STORAGE_PERMISSION = 1000;
    private static final String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private OnBackToHudModeListener mBackToHudModeListener;
    private PhotoViewController mPhotoViewController;
    private File mPhotosDir;
    private OnStoragePermissionGranted mPermissionCallback;
    private File mSavedFile;
    private SoundPool mSoundPool;
    private int mClickSoundId;
    private IPhotoView mView;

    public ScreenshotMode(PetContext petContext, OnBackToHudModeListener listener) {
        super(petContext, new PhotoViewController(petContext));
        mBackToHudModeListener = listener;
        mPhotoViewController = (PhotoViewController) mModeScene;
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
        requestStoragePermission(this::takePhoto);
        loadSounds();
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
        if (mSoundPool != null) {
            mSoundPool.release();
        }
    }

    private void showPhotoView(Bitmap photo) {
        mView = mPhotoViewController.makeView(IPhotoView.class);
        mView.setOnActionsShareClickListener(this::onShareButtonClicked);
        mView.setOnCancelClickListener(view1 -> backToHudView());
        mView.setPhotoBitmap(photo);
        mView.show();
    }

    private void onShareButtonClicked(View clickedButton) {
        switch (clickedButton.getId()) {
            case R.id.button_facebook:
                openFacebook();
                break;
            case R.id.button_whatsapp:
                openWhatsApp();
                break;
            case R.id.button_instagram:
                openInstagram();
                break;
            default:
                Log.d(TAG, "invalid button");
        }
    }

    private void backToHudView() {
        mPetContext.getSXRContext()
                .runOnGlThread(() -> mBackToHudModeListener.OnBackToHud());
    }

    private void initPhotosDir() {
        if (mPhotosDir == null) {
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            mPhotosDir = new File(picturesDir, APP_PHOTOS_DIR_NAME);
            if (!mPhotosDir.exists()) {
                if (mPhotosDir.mkdirs()) {
                    Log.d(TAG, "Directory created: " + mPhotosDir);
                }
            } else {
                Log.d(TAG, "Using existing directory: " + mPhotosDir);
            }
        }
    }

    private void takePhoto() {
        try {
            mSavedFile = null;
            mPetContext.getSXRContext().captureScreenCenter(this::onPhotoCaptured);
        } catch (Throwable t) {
            Log.e(TAG, "Error taking photo", t);
        }
    }

    private void onPhotoCaptured(Bitmap capturedPhotoBitmap) {
        Log.d(TAG, "Photo captured " + capturedPhotoBitmap);
        if (capturedPhotoBitmap != null) {
            playClickSound();
            showPhotoView(capturedPhotoBitmap);
            AsyncExecutor.create().execute(() -> savePhoto(capturedPhotoBitmap));
        }
    }

    private void savePhoto(Bitmap capturedPhotoBitmap) {

        if (StorageUtils.getAvailableExternalStorageSize() <= 0) {
            Log.e(TAG, "There is no free space to save the photo on this device.");
            return;
        }

        initPhotosDir();

        final String fileName = "sxr-arpet-photo-" + System.currentTimeMillis() + ".png";
        File file = new File(mPhotosDir, fileName);

        try (FileOutputStream output = new FileOutputStream(file)) {
            capturedPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 50, output);
        } catch (IOException e) {
            file = null;
            Log.e(TAG, "Error saving photo", e);
        }

        new Handler(Looper.getMainLooper()).post(() -> mView.showToast());
        mView.enableButtons();
        mSavedFile = file;

        // Scan file to make it available on gallery immediately
        MediaScannerConnection.scanFile(mPetContext.getActivity(),
                new String[]{mSavedFile.toString()}, null,
                (path, uri) -> {
                });
    }

    @Override
    protected void onHandleOrientation(SXRCameraRig cameraRig) {
    }

    private void requestStoragePermission(OnStoragePermissionGranted callback) {
        mPermissionCallback = callback;
        mPetContext.getActivity().requestPermissions(PERMISSION_STORAGE, REQUEST_STORAGE_PERMISSION);
    }

    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(mPetContext.getActivity(), PERMISSION_STORAGE[0])
                == PackageManager.PERMISSION_GRANTED;
    }

    @Subscribe
    public void handleContextEvent(ActivityResultEvent event) {
        if (event.getRequestCode() == REQUEST_STORAGE_PERMISSION) {
            if (hasStoragePermission()) {
                mPermissionCallback.onGranted();
            } else {
                backToHudView();
                showToastPermissionDenied();
            }
        }
    }

    @Subscribe
    public void handleContextEvent(RequestPermissionResultEvent event) {
        if (event.getRequestCode() == REQUEST_STORAGE_PERMISSION) {
            if (hasStoragePermission()) {
                mPermissionCallback.onGranted();
            } else {
                if (mPetContext.getActivity().shouldShowRequestPermissionRationale(PERMISSION_STORAGE[0])) {
                    backToHudView();
                    showToastPermissionDenied();
                } else {
                    showToastPermissionDenied();
                    openAppPermissionsSettings();
                }
            }
        }
    }

    private void showToastPermissionDenied() {
        Toast.makeText(mPetContext.getActivity(),
                "Storage access not allowed", Toast.LENGTH_LONG).show();
    }

    private void openAppPermissionsSettings() {
        Activity context = mPetContext.getActivity();
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
        context.startActivityForResult(intent, REQUEST_STORAGE_PERMISSION);
    }

    private void openFacebook() {
        if (mSavedFile != null && checkAppInstalled(PACK_NAME_FACEBOOK)) {
            Intent intent = createIntent();
            intent.setClassName(PACK_NAME_FACEBOOK, ACTIVITY_SHARE_PICTURE);
            mPetContext.getActivity().startActivity(intent);
        }
    }

    private void openWhatsApp() {
        if (mSavedFile != null && checkAppInstalled(PACK_NAME_WHATSAPP)) {
            Intent intent = createIntent();
            intent.setPackage(PACK_NAME_WHATSAPP);
            mPetContext.getActivity().startActivity(intent);
        }
    }

    private void openInstagram() {
        if (mSavedFile != null && checkAppInstalled(PACK_NAME_INSTAGRAM)) {
            Intent intent = createIntent();
            intent.setPackage(PACK_NAME_INSTAGRAM);
            mPetContext.getActivity().startActivity(intent);
        }
    }

    private Intent createIntent() {
        Activity context = mPetContext.getActivity();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, mSavedFile));
        intent.setType("image/png");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private boolean checkAppInstalled(String packageName) {
        if (isAppInstalled(packageName)) {
            return true;
        } else {
            installApp(packageName);
            return false;
        }
    }

    private void installApp(String appName) {
        Activity context = mPetContext.getActivity();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName));
            intent.setPackage("com.android.vending");
            context.startActivity(intent);
        } catch (Exception exception) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appName)));
        }
    }

    private boolean isAppInstalled(String packageName) {
        Activity context = mPetContext.getActivity();
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void loadSounds() {

        mPetContext.getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        mClickSoundId = mSoundPool.load("/system/media/audio/ui/camera_click.ogg", 1);
    }


    private void playClickSound() {

        AudioManager audioManager = (AudioManager) mPetContext.getActivity().getSystemService(Context.AUDIO_SERVICE);
        float vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float leftVolume = vol / maxVol;
        float rightVolume = vol / maxVol;
        int priority = 1;
        int no_loop = 0;
        float normal_playback_rate = 1f;

        mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            mSoundPool.release();
        });

        mSoundPool.play(mClickSoundId, leftVolume, rightVolume,
                priority, no_loop, normal_playback_rate);
    }


    @FunctionalInterface
    private interface OnStoragePermissionGranted {
        void onGranted();
    }

}
