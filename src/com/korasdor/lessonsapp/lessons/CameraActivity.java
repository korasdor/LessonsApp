package com.korasdor.lessonsapp.lessons;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.NativeCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.korasdor.lessonsapp.R;
import com.korasdor.lessonsapp.lessons.common.LabActivity;
import com.nummist.secondsight.filters.Filter;
import com.nummist.secondsight.filters.NoneFilter;
import com.nummist.secondsight.filters.convolution.StrokeEdgesFilter;
import com.nummist.secondsight.filters.curve.CrossProcessCurveFilter;
import com.nummist.secondsight.filters.curve.PortraCurveFilter;
import com.nummist.secondsight.filters.curve.ProviaCurveFilter;
import com.nummist.secondsight.filters.curve.VelviaCurveFilter;
import com.nummist.secondsight.filters.mixer.RecolorCMVFilter;
import com.nummist.secondsight.filters.mixer.RecolorRCFilter;
import com.nummist.secondsight.filters.mixer.RecolorRGVFilter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class CameraActivity extends FragmentActivity implements
		CvCameraViewListener2 {

	private static final String TAG = "CameraActivity";
	private static final String STATE_CAMERA_INDEX = "cameraIndex";
	private static final String STATE_CURVE_FILTER_INDEX = "curveFilterIndex";
	private static final String STATE_MIXER_FILTER_INDEX = "mixerFilterIndex";
	private static final String STATE_CONVOLUTION_FILTER_INDEX = "convolutionFilterIndex";

	private Filter[] mCurveFilters;
	private Filter[] mMixerFilters;
	private Filter[] mConvolutionFilters;

	private CameraBridgeViewBase mCameraView;
	private Mat mBgr;

	private int mCameraIndex;
	private int mNumCameras;

	private int mCurveFilterIndex;
	private int mMixedFilterIndex;
	private int mConvolutuinFilterIndex;

	private boolean mIsCameraFrontFacing;
	private boolean mIsPhotoPending;
	private boolean mIsMenuLocked;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
				Log.d(TAG, "OpenCV loaded siccessfully");
				mCameraView.enableView();
				mBgr = new Mat();

				mCurveFilters = new Filter[] { new NoneFilter(),
						new PortraCurveFilter(), new ProviaCurveFilter(),
						new VelviaCurveFilter(), new CrossProcessCurveFilter() };

				mMixerFilters = new Filter[] { new NoneFilter(),
						new RecolorRCFilter(), new RecolorRGVFilter(),
						new RecolorCMVFilter() };

				mConvolutionFilters = new Filter[] { new NoneFilter(),
						new StrokeEdgesFilter() };

				break;

			default:
				super.onManagerConnected(status);
				break;
			}
		}
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (savedInstanceState != null) {
			mCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0);
			mCurveFilterIndex = savedInstanceState.getInt(
					STATE_CURVE_FILTER_INDEX, 0);
			mMixedFilterIndex = savedInstanceState.getInt(
					STATE_MIXER_FILTER_INDEX, 0);
			mConvolutuinFilterIndex = savedInstanceState.getInt(
					STATE_CONVOLUTION_FILTER_INDEX, 0);
		} else {
			mCameraIndex = 0;
			mCurveFilterIndex = 0;
			mMixedFilterIndex = 0;
			mConvolutuinFilterIndex = 0;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(mCameraIndex, cameraInfo);
			mIsCameraFrontFacing = (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT);
			mNumCameras = Camera.getNumberOfCameras();
		} else {
			mIsCameraFrontFacing = false;
			mNumCameras = 1;
		}

//		mCameraView = new NativeCameraView(this, mCameraIndex);
		mCameraView = new JavaCameraView(this, mCameraIndex);
		mCameraView.setCvCameraViewListener(this);
		setContentView(mCameraView);
	}

	@SuppressLint("NewApi")
	@Override
	public void onSaveInstanceState(Bundle outState,
			PersistableBundle outPersistentState) {

		outState.putInt(STATE_CAMERA_INDEX, mCameraIndex);
		outState.putInt(STATE_CURVE_FILTER_INDEX, mCurveFilterIndex);
		outState.putInt(STATE_MIXER_FILTER_INDEX, mMixedFilterIndex);
		outState.putInt(STATE_CONVOLUTION_FILTER_INDEX, mConvolutuinFilterIndex);

		super.onSaveInstanceState(outState, outPersistentState);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mCameraView != null) {
			mCameraView.disableView();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!OpenCVLoader.initDebug()) {
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
					mLoaderCallback);
		} else {
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}

		mIsMenuLocked = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCameraView != null) {
			mCameraView.disableView();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_camera, menu);

		if (mNumCameras < 2) {
			menu.removeItem(R.id.menu_next_camera);
		}

		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mIsMenuLocked)
			return true;

		switch (item.getItemId()) {
		case R.id.menu_next_curve_filter:
			mCurveFilterIndex++;

			if (mCurveFilterIndex == mCurveFilters.length) {
				mCurveFilterIndex = 0;
			}
			return true;
		case R.id.menu_next_mixer_filter:
			mMixedFilterIndex++;

			if (mMixedFilterIndex == mMixerFilters.length) {
				mMixedFilterIndex = 0;
			}
			return true;
		case R.id.menu_next_convolution_filter:
			mConvolutuinFilterIndex++;

			if (mConvolutuinFilterIndex == mConvolutionFilters.length) {
				mConvolutuinFilterIndex = 0;
			}
			return true;
		case R.id.menu_next_camera:
			mIsMenuLocked = true;
			mCameraIndex++;

			if (mCameraIndex == mNumCameras) {
				mCameraIndex = 0;
			}
			recreate();

			return true;
		case R.id.menu_take_photo:
			mIsMenuLocked = true;
			mIsPhotoPending = true;
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
	}

	@Override
	public void onCameraViewStopped() {
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		final Mat rgba = inputFrame.rgba();
		
		mCurveFilters[mCurveFilterIndex].apply(rgba, rgba);
		mMixerFilters[mMixedFilterIndex].apply(rgba, rgba);
		mConvolutionFilters[mConvolutuinFilterIndex].apply(rgba, rgba);

		if (mIsPhotoPending) {
			mIsPhotoPending = false;
			takePhoto(rgba);
		}

		if (mIsCameraFrontFacing) {
			Core.flip(rgba, rgba, 1);
		}

		return rgba;
	}

	private void takePhoto(final Mat rgba) {
		final long currentTimeMillis = System.currentTimeMillis();
		final String appName = getString(R.string.second_sight_activity_label);
		final String galleryPath = Environment
				.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES).toString();
		final String albumPath = galleryPath + "/" + appName;
		final String photoPath = albumPath + "/" + currentTimeMillis + ".png";
		final ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, photoPath);
		values.put(Images.Media.MIME_TYPE, LabActivity.PHOTO_MIME_TYPE);
		values.put(Images.Media.TITLE, appName);
		values.put(Images.Media.DESCRIPTION, appName);
		values.put(Images.Media.DATE_TAKEN, currentTimeMillis);

		File album = new File(albumPath);
		if (!album.isDirectory() && !album.mkdirs()) {
			Log.e(TAG, "Failed to create album directory at " + albumPath);
			onTakePhotoFailed();
			return;
		}

		Imgproc.cvtColor(rgba, mBgr, Imgproc.COLOR_RGB2BGR, 3);
		if (!Highgui.imwrite(photoPath, mBgr)) {
			Log.e(TAG, "Failed to save photo to " + photoPath);
			onTakePhotoFailed();
		}

		Log.d(TAG, "Photo saved successfully to " + photoPath);

		Uri uri;
		try {
			uri = getContentResolver().insert(
					Images.Media.EXTERNAL_CONTENT_URI, values);
		} catch (Exception e) {
			Log.e(TAG, "Failed to insert photo into MediaStore");
			e.printStackTrace();

			File photo = new File(photoPath);
			if (!photo.delete()) {
				Log.e(TAG, "Failed to delete non-insert photo");
			}

			onTakePhotoFailed();
			return;
		}

		final Intent intent = new Intent(this, LabActivity.class);
		intent.putExtra(LabActivity.EXTRA_PHOTO_URI, uri);
		intent.putExtra(LabActivity.EXTRA_PHOTO_DATA_PATH, photoPath);
		startActivity(intent);
	}

	private void onTakePhotoFailed() {
		mIsMenuLocked = false;

		final String errorMessage = getString(R.string.photo_error_message);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(CameraActivity.this, errorMessage,
						Toast.LENGTH_LONG).show();
			}
		});
	}
}