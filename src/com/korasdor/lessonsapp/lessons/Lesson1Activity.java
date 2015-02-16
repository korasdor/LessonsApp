package com.korasdor.lessonsapp.lessons;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;

public class Lesson1Activity extends Activity implements CvCameraViewListener {
	
	private CameraBridgeViewBase mOpenCvCameraView;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
				mOpenCvCameraView.enableView();
				break;

			default:
				super.onManagerConnected(status);
				break;
			}
		};
	};
	
	protected void onResume() {
		super.onResume();
		
		if (!OpenCVLoader.initDebug()) {
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		} else {
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(com.korasdor.lessonsapp.R.layout.lesson1layout);
		
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(com.korasdor.lessonsapp.R.id.HelloOpenCvView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mOpenCvCameraView != null){
			mOpenCvCameraView.disableView();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mOpenCvCameraView != null){
			mOpenCvCameraView.disableView();
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
	}

	@Override
	public void onCameraViewStopped() {
	}

	@Override
	public Mat onCameraFrame(Mat inputFrame) {
		return inputFrame;
	}
}
