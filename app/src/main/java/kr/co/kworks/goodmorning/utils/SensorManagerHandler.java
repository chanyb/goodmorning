package kr.co.kworks.goodmorning.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorManagerHandler {
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, magneticFieldSensor;
    private SensorEventListener accelerometerListener, magneticFieldListener;
    private float azimuthInDegrees, pitchInDegrees, rollInDegrees;
    private AzimuthInDegreeListener azimuthInDegreeListener;
    private PitchInDegreeListener pitchInDegreeListener;
    private RollInDegreeListener rollInDegreeListener;
    private AccuracyListener accuracyListener;

    public interface AccuracyListener {
         void onAccuracyChanged(Sensor sensor, int i);
    }

    public interface AzimuthInDegreeListener {
        void onSensorChanged(float degree);
    }

    public interface PitchInDegreeListener {
        void onSensorChanged(float degree);
    }

    public interface RollInDegreeListener {
        void onSensorChanged(float degree);
    }

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    public SensorManagerHandler(Context mContext) {
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
    }

    public Sensor getAccelerometerSensor() {
        if(accelerometerSensor == null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        return accelerometerSensor;
    }

    public SensorEventListener getAccelerometerListener() {
        if(accelerometerListener == null) {
            accelerometerListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    System.arraycopy(event.values, 0, accelerometerReading,
                            0, accelerometerReading.length);

                    updateOrientationAngles();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    if(accuracyListener != null) {
                        accuracyListener.onAccuracyChanged(sensor, i);
                    }
                }
            };
        }
        return accelerometerListener;
    }

    public Sensor getMagneticFieldSensor() {
        if(magneticFieldSensor == null) {
            magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        return magneticFieldSensor;
    }

    public SensorEventListener getMagneticFieldListener() {
        if(magneticFieldListener == null) {
            magneticFieldListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    System.arraycopy(event.values, 0, magnetometerReading,
                            0, magnetometerReading.length);

                    updateOrientationAngles();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    if(accuracyListener != null) {
                        accuracyListener.onAccuracyChanged(sensor, i);
                    }
                }
            };
        }
        return magneticFieldListener;
    }

    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "mRotationMatrix" now has up-to-date information.
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        float azimuthInRadians = orientationAngles[0];
        float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

        float pitchInRadians = orientationAngles[1];
        float pitchInDegrees = (float) (Math.toDegrees(pitchInRadians) + 360) % 360;

        float rollInRadians = orientationAngles[2];
        float rollInDegrees = (float) (Math.toDegrees(rollInRadians) + 360) % 360;

        this.azimuthInDegrees = azimuthInDegrees;
        this.pitchInDegrees = pitchInDegrees;
        this.rollInDegrees = rollInDegrees;

        if(azimuthInDegreeListener != null) {
            azimuthInDegreeListener.onSensorChanged(azimuthInDegrees);
        }

        if(pitchInDegreeListener != null) {
            pitchInDegreeListener.onSensorChanged(pitchInDegrees);
        }

        if(rollInDegreeListener != null) {
            rollInDegreeListener.onSensorChanged(rollInDegrees);
        }


        // quaternion
        float w, x, y, z; // Quaternion components

        float trace = rotationMatrix[0] + rotationMatrix[4] + rotationMatrix[8];
        if (trace > 0) {
            float s = 0.5f / (float) Math.sqrt(trace + 1.0f);
            w = 0.25f / s;
            x = (rotationMatrix[7] - rotationMatrix[5]) * s;
            y = (rotationMatrix[2] - rotationMatrix[6]) * s;
            z = (rotationMatrix[3] - rotationMatrix[1]) * s;
        } else {
            if (rotationMatrix[0] > rotationMatrix[4] && rotationMatrix[0] > rotationMatrix[8]) {
                float s = 2.0f * (float) Math.sqrt(1.0f + rotationMatrix[0] - rotationMatrix[4] - rotationMatrix[8]);
                w = (rotationMatrix[7] - rotationMatrix[5]) / s;
                x = 0.25f * s;
                y = (rotationMatrix[1] + rotationMatrix[3]) / s;
                z = (rotationMatrix[2] + rotationMatrix[6]) / s;
            } else if (rotationMatrix[4] > rotationMatrix[8]) {
                float s = 2.0f * (float) Math.sqrt(1.0f + rotationMatrix[4] - rotationMatrix[0] - rotationMatrix[8]);
                w = (rotationMatrix[2] - rotationMatrix[6]) / s;
                x = (rotationMatrix[1] + rotationMatrix[3]) / s;
                y = 0.25f * s;
                z = (rotationMatrix[5] + rotationMatrix[7]) / s;
            } else {
                float s = 2.0f * (float) Math.sqrt(1.0f + rotationMatrix[8] - rotationMatrix[0] - rotationMatrix[4]);
                w = (rotationMatrix[3] - rotationMatrix[1]) / s;
                x = (rotationMatrix[2] + rotationMatrix[6]) / s;
                y = (rotationMatrix[5] + rotationMatrix[7]) / s;
                z = 0.25f * s;
            }
        }

        double theta = 2*Math.acos(w);
        double thetaX = 180*x*theta/Math.PI*Math.sin(theta/2);
        double thetaY = 180*y*theta/Math.PI*Math.sin(theta/2);
        double thetaZ = 180*z*theta/Math.PI*Math.sin(theta/2);
    }

    public void addAzimuthListener(AzimuthInDegreeListener listener) {
        sensorManager.registerListener(getAccelerometerListener(), getAccelerometerSensor(), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(getMagneticFieldListener(), getMagneticFieldSensor(), SensorManager.SENSOR_DELAY_GAME);
        this.azimuthInDegreeListener = listener;
    }

    public void removeAzimuthListener() {
        this.azimuthInDegreeListener = null;
    }

    public void addPitchListener(PitchInDegreeListener listener) {
        sensorManager.registerListener(getAccelerometerListener(), getAccelerometerSensor(), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(getMagneticFieldListener(), getMagneticFieldSensor(), SensorManager.SENSOR_DELAY_GAME);
        this.pitchInDegreeListener = listener;
    }

    public void removePitchListener() {
        this.pitchInDegreeListener = null;
    }

    public void addRollListener(RollInDegreeListener listener) {
        sensorManager.registerListener(getAccelerometerListener(), getAccelerometerSensor(), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(getMagneticFieldListener(), getMagneticFieldSensor(), SensorManager.SENSOR_DELAY_GAME);
        this.rollInDegreeListener = listener;
    }

    public void addAccuracyListener(AccuracyListener accuracyListener) {
        this.accuracyListener = accuracyListener;
    }

    public void removeRollListener() {
        this.rollInDegreeListener = null;
    }

    public void unregisterListeners() {
        sensorManager.unregisterListener(getAccelerometerListener());
        sensorManager.unregisterListener(getMagneticFieldListener());
    }
}
