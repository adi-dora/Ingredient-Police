package com.example.snapchatcopy;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback {
    CameraManager mCameraManager;
    SurfaceView mPreviewView;
    CaptureRequest mPreviewRequest;
    SurfaceHolder mPreviewHolder;
    HandlerThread mCameraThread;
    Handler mCameraHandler, mReadyHandler;
    CameraCharacteristics mCameraInfo;
    CameraDevice mCameraDevice;
    ImageReader mImageReader;
    ImageButton mCapture, camera_fragment, search_fragment, history_fragment, helpBtn;
    File file;
    int hasPaused = 0;
    int test = 0;
    HandlerThread imageThread;
    Handler imageHandler;
    boolean buttonPressed = false;
    ArrayBlockingQueue<Image> imageQueue = new ArrayBlockingQueue<>(3);
    CameraCaptureSession mCameraSession;
    String[] perm;
    ViewPager2 pager;

    private final ConditionVariable mCloseWaiter = new ConditionVariable();


    public static CameraFragment newInstance() {
        return new CameraFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Test runs", "ViewCreated");
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        mPreviewView = view.findViewById(R.id.surfaceView);
        mCapture = view.findViewById(R.id.capture);
        camera_fragment = view.findViewById(R.id.c_camera_fragment);
        search_fragment = view.findViewById(R.id.c_search_fragment);
        history_fragment = view.findViewById(R.id.c_history_fragment);
        helpBtn = view.findViewById(R.id.helpBtn);
        pager = requireActivity().findViewById(R.id.viewPager);



        startBackgroundTasks();

        mCapture.setOnClickListener(view1 -> {
            mCapture.setEnabled(false);
            buttonPressed = true;
            takePhoto();
            mCapture.setEnabled(true);
        });

        camera_fragment.setOnClickListener(view12 -> {

            pager.setCurrentItem(1);

        });

        search_fragment.setOnClickListener(view13 -> {
            pager.setCurrentItem(2);


        });

       history_fragment.setOnClickListener(view14 -> pager.setCurrentItem(0));

        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TutorialActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }

    public void startBackgroundTasks() {


        mPreviewHolder = mPreviewView.getHolder();
        mPreviewHolder.addCallback(this);
        mReadyHandler = new Handler(Looper.getMainLooper());
        mCameraThread = new HandlerThread(this.getClass().toString());
        imageThread = new HandlerThread("Image Thread");
        imageThread.start();
        mCameraThread.start();

        mCameraHandler = new Handler(mCameraThread.getLooper());
        imageHandler = new Handler(imageThread.getLooper());
        file = new File(Environment.getExternalStorageDirectory() + "/" + UUID.randomUUID().toString() + ".jpg");
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

        if (hasPaused != 0){
            return;
        } 

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            findCamera();
        }

        hasPaused++;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("Surface Changed", "Output Sizes set");


    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        Log.d("Surface destroyed", "runs first?");

        new Thread(new Runnable() {
            @Override
            public void run() {
                mCloseWaiter.close();
                mCameraHandler.post(mCloseRunnable);
                mCloseWaiter.block(2000);


                mCameraThread.quitSafely();
                imageThread.quitSafely();
            }
        }).start();

    }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA);
        int permissionState1 = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // Check if the Camera permission is already available.
        if (permissionState != PackageManager.PERMISSION_GRANTED && permissionState1 != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.
            return false;
        } else {
            // Camera permissions are available.
            Log.i("Camera Status", "CAMERA permission has already been granted.");
            return true;
        }
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        Log.i("Camera Status", "onRequestPermissionResult");
//        if (requestCode == 1) {
//            if (grantResults.length <= 0) {
//                // If user interaction was interrupted, the permission request is cancelled and you
//                // receive empty arrays.
//                Log.i("Camera Status", "User interaction was cancelled.");
//            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission was granted.
//                findCamera();
//            } else {
//                // Permission denied.
//
//                // In this Activity we've chosen to notify the user that they
//                // have rejected a core permission for the app since it makes the Activity useless.
//                // We're communicating this message in a Snackbar since this is a sample app, but
//                // core permissions would typically be best requested during a welcome-screen flow.
//
//                // Additionally, it is important to remember that a permission might have been
//                // rejected without asking the user for permission (device policy or "Never ask
//                // again" prompts). Therefore, a user interface affordance is typically implemented
//                // when permissions are denied. Otherwise, your app could appear unresponsive to
//                // touches or interactions which have required permissions.
//                Snackbar.make(mPreviewView, "The camera permission has been denied", Snackbar
//                        .LENGTH_INDEFINITE)
//                        .setAction("Settings", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                // Build intent that displays the App settings screen.
//                                Intent intent = new Intent();
//                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
//                                intent.setData(uri);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
//                            }
//                        })
//                        .show();
//            }
//        }
//    }

    private void requestPermissions() {
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        int permissionState = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA);
        int permissionState1 = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean camPerm = permissionState == PackageManager.PERMISSION_GRANTED;
        boolean storagePerm = permissionState1 == PackageManager.PERMISSION_GRANTED;
        if (!camPerm && !storagePerm)
            perm = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        else {
            if (!camPerm)
                perm = new String[]{Manifest.permission.CAMERA};
            else {
                perm = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
            Log.i("Camera Status", "Displaying camera permission rationale to provide additional context.");
            Snackbar.make(mPreviewView, "This app requires camera access in order to function.", Snackbar
                    .LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request Camera permission
                            ActivityCompat.requestPermissions(requireActivity(), perm, 1);
                        }
                    })
                    .show();
        } else {
            Log.i("Camera Status", "Requesting camera permission");
            // Request Camera permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(requireActivity(), perm, 1);
        }
    }


    private void configureSurfaces() {
        // Find a good size for output - largest 16:9 aspect ratio that's less than 720p
        final int MAX_WIDTH = 1080;
        final float TARGET_ASPECT = 16.f / 9.f;
        final float ASPECT_TOLERANCE = 0.1f;


        StreamConfigurationMap configs =
                mCameraInfo.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (configs == null) {
            throw new RuntimeException("Cannot get available picture/preview sizes.");
        }
        Size[] outputSizes = configs.getOutputSizes(SurfaceHolder.class);

        Size outputSize = outputSizes[0];
        float outputAspect = (float) outputSize.getWidth() / outputSize.getHeight();
        for (Size candidateSize : outputSizes) {
            if (candidateSize.getWidth() > MAX_WIDTH) continue;
            float candidateAspect = (float) candidateSize.getWidth() / candidateSize.getHeight();
            boolean goodCandidateAspect =
                    Math.abs(candidateAspect - TARGET_ASPECT) < ASPECT_TOLERANCE;
            boolean goodOutputAspect =
                    Math.abs(outputAspect - TARGET_ASPECT) < ASPECT_TOLERANCE;
            if ((goodCandidateAspect && !goodOutputAspect) ||
                    candidateSize.getWidth() > outputSize.getWidth()) {
                outputSize = candidateSize;
                outputAspect = candidateAspect;
            }
        }
        Log.i("Camera Status", "Resolution chosen: " + outputSize);

        mImageReader = ImageReader.newInstance(outputSize.getWidth(), outputSize.getHeight(), ImageFormat.JPEG, 3);
        mPreviewHolder.setFixedSize(outputSize.getWidth(), outputSize.getHeight());
    }

    private void findCamera() {
        if (!checkPermissions()) {
            return;
        }
        String errorMsg = "An error occurred";

        mCameraManager = (CameraManager) requireContext().getSystemService(Context.CAMERA_SERVICE);
        boolean foundCamera = false;
        if (mCameraManager != null) {
            try {
                String[] cameraIds = mCameraManager.getCameraIdList();
                for (String id : cameraIds) {
                    CameraCharacteristics info = mCameraManager.getCameraCharacteristics(id);
                    if (Objects.equals(info.get(CameraCharacteristics.LENS_FACING), CameraCharacteristics.LENS_FACING_BACK)) {
                        mCameraInfo = info;
                        foundCamera = true;
                        configureSurfaces();
                        openCamera(id);
                        break;
                    }
                }

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            if (!foundCamera)
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    public void openCamera(String id) {
        if(!mCameraThread.isAlive()){
            startBackgroundTasks();
        }


        mCameraHandler.post(new Runnable() {

            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                try {
                    mCameraManager.openCamera(id, mCameraDeviceListener, mCameraHandler);

                } catch (CameraAccessException e) {
                    Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    CameraDevice.StateCallback mCameraDeviceListener = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {


            mCameraDevice = cameraDevice;
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    startCameraSession();
                }
            });
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;

        }
    };

    CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {

            Long resultTimeStamp = result.get(CaptureResult.SENSOR_TIMESTAMP);

            try {
                Image image = imageQueue.take();
                while(image.getTimestamp() != resultTimeStamp){

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mImageReader.setOnImageAvailableListener(null, null);

        }
    };

    private void startCameraSession() {
        // Wait until both the camera device is open and the SurfaceView is ready


        if (mCameraDevice == null) {
            return;
        }

        try {
            mCameraDevice.createCaptureSession(
                    Arrays.asList(mPreviewHolder.getSurface(), mImageReader.getSurface()),
                    mCameraSessionListener, mCameraHandler);


        } catch (CameraAccessException e) {

            mCameraDevice.close();
            mCameraDevice = null;
            e.printStackTrace();

        }

    }

    private final CameraCaptureSession.StateCallback mCameraSessionListener =
            new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCameraSession = session;

                    mReadyHandler.post(new Runnable() {
                        public void run() {
                            if (null == mCameraDevice) {
                                return;
                            }
                            try {
                                CaptureRequest.Builder previewBuilder = mCameraDevice
                                        .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON);

                                previewBuilder.addTarget(mPreviewHolder.getSurface());
                                mPreviewRequest = previewBuilder.build();
                                mCameraSession.setRepeatingRequest(mPreviewRequest, null,
                                        mCameraHandler);

                            } catch(CameraAccessException e){
                                e.printStackTrace();
                            }
                        }
                    });

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    mCameraDevice.close();
                    mCameraDevice = null;

                }
            };

    ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image;

            try {
                image = mImageReader.acquireNextImage();
                imageQueue.add(image);
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                Intent intent = new Intent(getActivity(), PictureActivity.class);
                intent.putExtra("picture", bytes);
                startActivity(intent);


            }

            finally {
                {
                }
            }
        }

    };

    @Override
    public void onResume(){
        startBackgroundTasks();
        Log.d("Resume Ran", "Line 510");
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
        Log.d("Test Pause", ""+ test);
//        mCloseWaiter.close();
//        mCameraHandler.post(mCloseRunnable);
//        mCloseWaiter.block(2000);
//
//        mCameraThread.quitSafely();
//        imageThread.quitSafely();
//        mCameraThread = null;
//        mCameraHandler = null;
//        mReadyHandler = null;
//        Log.d("On pause runs", "Line 550");
//        hasPaused++;
//

        hasPaused = 0;


    }

    private final Runnable mCloseRunnable = new Runnable() {
        @Override
        public void run() {
            if(mCameraDevice != null)
                mCameraDevice.close();
            mCameraDevice = null;
            if(mCameraSession != null)
                mCameraSession.close();
            mCameraSession = null;

        }
    };

    private void takePhoto(){
        while(mImageReader.acquireNextImage()!= null){
        }
        try{
            mImageReader.setOnImageAvailableListener(readerListener, imageHandler);
            CaptureRequest.Builder imageBuilder = mCameraSession.getDevice()
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            imageBuilder.addTarget(mImageReader.getSurface());
            mCameraSession.capture(imageBuilder.build(), mCaptureCallback, mReadyHandler);
        } catch(CameraAccessException e){
            e.printStackTrace();
        }

    }



}






