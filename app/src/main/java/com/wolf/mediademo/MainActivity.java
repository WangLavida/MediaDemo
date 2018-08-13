package com.wolf.mediademo;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button start;
    private Button end;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.start);
        end = findViewById(R.id.end);
        start.setOnClickListener(this);
        end.setOnClickListener(this);
        end.setEnabled(false);
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mSurfaceHolder = surfaceHolder;
                // 初始化相机
                initCamera();

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                mSurfaceHolder = surfaceHolder;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                // 释放相机预览
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
                if (mMediaRecorder != null) {
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                }

            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                Log.i("宽高", mSurfaceView.getHeight() + ";" + mSurfaceView.getWidth());
                File file = Environment.getExternalStorageDirectory(); // SD卡根目录
                file = new File(file.getPath() + "/myvideo/");
                if (!file.exists()) {
                    file.mkdirs();
                }
                // 开始录制
                if (mMediaRecorder == null) {
                    mMediaRecorder = new MediaRecorder();
                    mMediaRecorder.setCamera(mCamera);
                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
//                    mMediaRecorder.setVideoSize(mSurfaceView.getHeight(),mSurfaceView.getWidth());
                    mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);// 设置帧频率，然后就清晰了
                    mMediaRecorder.setOrientationHint(270);
                    mMediaRecorder.setOutputFile(file.getPath() + "/video_" + System.currentTimeMillis() + ".mp4");
                    mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                }
                // Unlock相机
                mCamera.unlock();
                try {
                    mMediaRecorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 开始录制视频
                mMediaRecorder.start();

                start.setEnabled(false);
                end.setEnabled(true);
                Toast.makeText(MainActivity.this, "开始录制", Toast.LENGTH_LONG).show();
                break;
            case R.id.end:
                if (mMediaRecorder != null) {
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                }

                if (mCamera != null) {
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
                start.setEnabled(true);
                end.setEnabled(false);
                Toast.makeText(MainActivity.this, "停止录制", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void initCamera() {


        if (mCamera == null) {
            // 打开前置摄像头
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCamera.setDisplayOrientation(90); // 竖屏预览
        }
        if (mCamera != null) {
            try {
                // 连接一个SurfaceView到相机来准备一个实时预览
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 开始预览
            mCamera.startPreview();
        }
    }

    // 检查是否有前置摄像头
    private boolean checkFrontCamera() {
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (Camera.CameraInfo.CAMERA_FACING_FRONT == info.facing)
                return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}
