package net.bible.android.control.page;

import java.util.List;

import net.bible.android.BibleApplication;
import net.bible.service.common.CommonUtils;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

/** The WebView component that shows teh main bible and commentary text
 * 
 * @author Martin Denham [mjdenham at gmail dot com]
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's author.
 */
public class PageTiltScrollControl {

	// must be null initially
	private Boolean mIsOrientationSensor = null;
	private boolean mIsTiltScrollEnabled = false;

	// the pitch at which a user views the text stationary
	// this changes dynamically when the screen is touched
	// both angles are degrees
	private int mNoScrollViewingPitch = -38;
	private boolean mNoScrollViewingPitchCalculated = false;
	
	private static final int NO_SCROLL_VIEWING_TOLERANCE = 3;
	private static final int NO_SPEED_INCREASE_VIEWING_TOLERANCE = 6;
	
	// this is decreased (subtracted from) to speed up scrolling
	private static int BASE_TIME_BETWEEN_SCROLLS = 40;
	
	// current pitch of phone - varies dynamically
	private float[] mOrientationValues;
	private int mRotation = Surface.ROTATION_0;

	// needed to find if screen switches to landscape and must different sensor value
	private Display mDisplay;
	
	@SuppressWarnings("unused")
	private static final String TAG = "TiltScrollControl";
	
	public static class TiltScrollInfo {
		public int scrollPixels;
		public boolean forward;
		public int delayToNextScroll;

		public static int TIME_TO_POLL_WHEN_NOT_SCROLLING = 500;
		
		private TiltScrollInfo reset() {
			scrollPixels = 0;
			forward = true;
			delayToNextScroll = TIME_TO_POLL_WHEN_NOT_SCROLLING;
			return this;
		}
	}
	// should not need more than one because teh request come in one at a time
	private TiltScrollInfo tiltScrollInfoSingleton = new TiltScrollInfo();
	
	public TiltScrollInfo getTiltScrollInfo() {
		TiltScrollInfo tiltScrollInfo = tiltScrollInfoSingleton.reset();
		int speedUp = 0;
		if (mOrientationValues!=null) {
			int normalisedPitch = getPitch(mRotation, mOrientationValues);
			int devianceFromViewingAngle = getDevianceFromStaticViewingAngle(normalisedPitch);
			
			if (devianceFromViewingAngle > NO_SCROLL_VIEWING_TOLERANCE) {
				tiltScrollInfo.forward = normalisedPitch < mNoScrollViewingPitch;

				// speedUp if tilt screen beyond a certain amount
				if (tiltScrollInfo.forward) {
					speedUp = Math.max(0, devianceFromViewingAngle-NO_SCROLL_VIEWING_TOLERANCE-NO_SPEED_INCREASE_VIEWING_TOLERANCE);
				} else {
					// speed up faster if going back because you don't read backwards but just want to move quickly
					speedUp = Math.max(0, devianceFromViewingAngle-NO_SCROLL_VIEWING_TOLERANCE);
				}

				// speedup could be done by increasing scroll amount but that leads to a jumpy screen
				tiltScrollInfo.scrollPixels = 1;
			}
		}
		if (mIsTiltScrollEnabled) {
			tiltScrollInfo.delayToNextScroll = Math.max(0,BASE_TIME_BETWEEN_SCROLLS-(3*speedUp));
		}
		return tiltScrollInfo;
	}
	
	/** start or stop tilt to scroll functionality
	 */
	public boolean enableTiltScroll(boolean enable) {
		if (!CommonUtils.getSharedPreferences().getBoolean("tilt_to_scroll_pref", true) || !isOrientationSensor()) {
			return false;
		} else if (mIsTiltScrollEnabled != enable) {
			mIsTiltScrollEnabled = enable;
			if (enable) {
				connectListeners();
			} else {
				disconnectListeners();
			}
			return true;
		} else {
			return false;
		}
	}

	/** called when user touches screen to reset home position
	 */
	public void recalculateViewingPosition() {
		//TODO save to settings
		mNoScrollViewingPitchCalculated = false;
	}

	/** if screen rotates must switch between different values returned by orientation sensor
	 */
	private int getPitch(int rotation, float[] orientationValues) {
		float pitch = 0;
		switch (rotation) {
		//Portrait for Nexus
		case Surface.ROTATION_0:
			pitch = orientationValues[1];
			break;
		//Landscape for Nexus
		case Surface.ROTATION_90:
			pitch = -orientationValues[2];
			break;
		case Surface.ROTATION_270:
			pitch = orientationValues[2];
			break;
		case Surface.ROTATION_180:
			pitch = -orientationValues[1];
			break;
		}
		return Math.round(pitch);
	}
	
	/** find angle between no-scroll-angle and current pitch
	 */
	private int getDevianceFromStaticViewingAngle(int normalisedPitch) {
	
		if (!mNoScrollViewingPitchCalculated) {
			// assume user's viewing pitch is the current one
			mNoScrollViewingPitch = normalisedPitch;
			mNoScrollViewingPitchCalculated = true;
		}
		
		return Math.abs(normalisedPitch-mNoScrollViewingPitch);
	}
	
	/**
	 * Orientation monitor (see Professional Android 2 App Dev Meier pg 469)
	 */
	
	private void connectListeners() {
		mDisplay = ((WindowManager) BibleApplication.getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		SensorManager sm = (SensorManager) BibleApplication.getApplication().getSystemService(Context.SENSOR_SERVICE);
		Sensor oSensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		sm.registerListener(myOrientationListener, oSensor, SensorManager.SENSOR_DELAY_UI);
	}
    private void disconnectListeners() {
		SensorManager sm = (SensorManager) BibleApplication.getApplication().getSystemService(Context.SENSOR_SERVICE);
    	sm.unregisterListener(myOrientationListener);
    }

	final SensorEventListener myOrientationListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				mOrientationValues = sensorEvent.values;
				mRotation = mDisplay.getRotation();
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	
    /**
     * Returns true if at least one Orientation sensor is available
     */
    public boolean isOrientationSensor() {
        if (mIsOrientationSensor == null) {
       		SensorManager sm = (SensorManager) BibleApplication.getApplication().getSystemService(Context.SENSOR_SERVICE);
            if (sm != null) {
                List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ORIENTATION);
                mIsOrientationSensor = new Boolean(sensors.size() > 0);
            } else {
                mIsOrientationSensor = Boolean.FALSE;
            }
        }
        return mIsOrientationSensor;
    }

	public boolean isTiltScrollEnabled() {
		return mIsTiltScrollEnabled;
	}
}
