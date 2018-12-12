package com.application.tardaniel.smartroom.AudioVisuals;

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;

import com.application.tardaniel.smartroom.R;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


/**
 * {@link VisualizerView} is responsible for setting up and drawing the visualization of the music.
 */
public class VisualizerView extends View {

    // These constants determine how much of a percentage of the audio frequencies each shape
    // represents. For example, the bass circle represents the bottom 10% of the frequencies.
    private static final float SEGMENT_SIZE = 100.f;
    private static final float BASS_SEGMENT_SIZE = 10.f / SEGMENT_SIZE;
    private static final float MID_SEGMENT_SIZE = 30.f / SEGMENT_SIZE;
    private static final float TREBLE_SEGMENT_SIZE = 60.f / SEGMENT_SIZE;

    // The minimum size of the shape, by default, before scaling
    private static final float MIN_SIZE_DEFAULT = 50;

    // This multiplier is used to make the frequency jumps a little more visually pronounced
    private static final float BASS_MULTIPLIER = 1.5f;
    private static final float MID_MULTIPLIER = 3;
    private static final float TREBLE_MULTIPLIER = 5;

    private static final float REVOLUTIONS_PER_SECOND = .3f;

    // Controls the Size of the circle each shape makes
    private static final float RADIUS_BASS = 20 / 100.f;
    private static final float RADIUS_MID = 60 / 100.f;
    private static final float RADIUS_TREBLE = 90 / 100.f;

    // The shapes
    private final TrailedShape mBassCircle;
    private final TrailedShape mMidSquare;
    private final TrailedShape mTrebleTriangle;

    // The array which keeps the current fft bytes
    private byte[] mBytes;

    // The time when the animation started
    private long mStartTime;

    // Numbers representing the current average of all the values in the bass, mid and treble range
    // in the fft
    private float bass;
    private float mid;
    private float treble;

    private float mHsv[] = new float[3];

    private float[] windowValue;
    private int nValue, insertValue;
    private float sumValue;

    private float[] windowHue;
    private int nHue, insertHue;
    private float sumHue;

    private float hue;

    // Determines whether each of these should be shown
    private boolean showBass;
    private boolean showMid;
    private boolean showTreble;

    @ColorInt
    private int backgroundColor;

    OnBackgroundColorChangedListener mCallback;

    public interface OnBackgroundColorChangedListener {
        public void onBackgroundColorChanged(int color);
    }


    float map(float x, float in_min, float in_max, float out_min, float out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public float nextValue(float val) {
        if (nValue < windowValue.length) nValue++;
        sumValue -= windowValue[insertValue];
        sumValue += val;
        windowValue[insertValue] = val;
        insertValue = (insertValue + 1) % windowValue.length;
        return sumValue / nValue;
    }

    public float nextHue(float val) {
        if (nHue < windowHue.length) nHue++;
        sumHue -= windowHue[insertHue];
        sumHue += val;
        windowHue[insertHue] = val;
        insertHue = (insertHue + 1) % windowHue.length;
        return sumHue / nHue;
    }


    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // TODO: check if it is good
        // This makes sure that the container fragment has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnBackgroundColorChangedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

//        windowValue = new float[4];
        windowValue = new float[4];
        insertValue = 0;
        sumValue = 0;

        windowHue = new float[20];
        insertHue = 0;
        sumHue = 0;

        mBytes = null;
        TrailedShape.setMinSize(MIN_SIZE_DEFAULT);

        // Create each of the shapes and define how they are drawn on screen
        // Make bass circle
        mBassCircle = new TrailedShape(BASS_MULTIPLIER) {
            @Override
            protected void drawThisShape(float shapeCenterX, float shapeCenterY, float currentSize, Canvas canvas, Paint paint) {
                canvas.drawCircle(shapeCenterX, shapeCenterY, currentSize, paint);
            }
        };

        // Make midrange square
        mMidSquare = new TrailedShape(MID_MULTIPLIER) {
            @Override
            protected void drawThisShape(float shapeCenterX, float shapeCenterY, float currentSize, Canvas canvas, Paint paint) {
                canvas.drawRect(shapeCenterX - currentSize,
                        shapeCenterY - currentSize,
                        shapeCenterX + currentSize,
                        shapeCenterY + currentSize,
                        paint);
            }
        };

        // Make treble triangle
        mTrebleTriangle = new TrailedShape(TREBLE_MULTIPLIER) {
            @Override
            protected void drawThisShape(float shapeCenterX, float shapeCenterY, float currentSize, Canvas canvas, Paint paint) {
                Path trianglePath = new Path();
                trianglePath.moveTo(shapeCenterX, shapeCenterY - currentSize);
                trianglePath.lineTo(shapeCenterX + currentSize, shapeCenterY + currentSize / 2);
                trianglePath.lineTo(shapeCenterX - currentSize, shapeCenterY + currentSize / 2);
                trianglePath.lineTo(shapeCenterX, shapeCenterY - currentSize);
                canvas.drawPath(trianglePath, paint);
            }
        };
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Setup all the view measurement code after the view is laid out. If this is done any
        // earlier the height and width are not yet determined
        mStartTime = SystemClock.uptimeMillis();

        float viewCenterX = getWidth() / 2.f;
        float viewCenterY = getHeight() / 2.f;
        float shortSide = viewCenterX < viewCenterY ? viewCenterX : viewCenterY;
        TrailedShape.setViewCenterX(viewCenterX);
        TrailedShape.setViewCenterY(viewCenterY);

        mBassCircle.setShapeRadiusFromCenter(shortSide * RADIUS_BASS);
        mMidSquare.setShapeRadiusFromCenter(shortSide * RADIUS_MID);
        mTrebleTriangle.setShapeRadiusFromCenter(shortSide * RADIUS_TREBLE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBytes == null) {
            return;
        }

        // Get the current angle all of the shapes are at
        double currentAngleRadians = calcCurrentAngle();

        // Draw the background
        canvas.drawColor(Color.TRANSPARENT);

        // Draw each shape
        if (showBass) {
            mBassCircle.draw(canvas, bass, currentAngleRadians);
        }
        if (showMid) {
            mMidSquare.draw(canvas, mid, currentAngleRadians);
        }
        if (showTreble) {
            mTrebleTriangle.draw(canvas, treble, currentAngleRadians);
        }

        // Invalidate the view to immediately redraw
        invalidate();
    }

    /**
     * Calculates, based on the current time, the angle all of the shapes should be at
     *
     * @return The current angle, in radians, that all shapes should be at
     */
    private double calcCurrentAngle() {
        long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
        float revolutions = elapsedTime * REVOLUTIONS_PER_SECOND / 1000;
        return revolutions * 2 * Math.PI;
    }

    /**
     * This method is called by the {@link AudioInputReader} class to pass in the current fast
     * Fourier transform bytes. The array is then taken, divided up into segments, and each segment
     * is averaged to determine how big of a visual spike to display.
     * <p>
     * For more information on fast fourier transforms, check out this website:
     * http://cmc.music.columbia.edu/musicandcomputers/chapter3/03_04.php
     *
     * @param bytes
     */
    public void updateFFT(byte[] bytes) {
        mBytes = bytes;
        int divider = 20;
        // Calculate average for bass segment
        float bassTotal = 0;
        for (int i = 0; i < bytes.length * BASS_SEGMENT_SIZE; i++) {
            bassTotal += Math.abs(bytes[i]);
        }
        bass = bassTotal / (bytes.length * BASS_SEGMENT_SIZE);

        // Calculate average for mid segment
        float midTotal = 0;
        for (int i = (int) (bytes.length * BASS_SEGMENT_SIZE); i < bytes.length * MID_SEGMENT_SIZE; i++) {
            midTotal += Math.abs(bytes[i]);
        }
        mid = midTotal / (bytes.length * MID_SEGMENT_SIZE);

        // Calculate average for terble segment
        float trebleTotal = 0;
        for (int i = (int) (bytes.length * MID_SEGMENT_SIZE); i < bytes.length; i++) {
            trebleTotal += Math.abs(bytes[i]);
        }
        treble = trebleTotal / (bytes.length * TREBLE_SEGMENT_SIZE);


        //saturation
        mHsv[1] = (float) 1;
        //value
        //mHsv[2] = (float)1;
        //mHsv[2] = nextValue(map(bass+treble,0.01f,100,0.4f,1.0f));
        mHsv[2] = nextValue(map(bass , 0.01f, 30, 0.5f, 1.0f));
        //mHsv[2] = nextValue(map(bass + treble, 0.01f, 35, 0.5f, 1.0f));

        hue += 0.5;
        if (mid > 15) {
            hue += 1;
        }
        if (bass * 4 + treble > 140) {
            hue += 15;
        } else if (bass * 4 + treble > 100) {
            hue += 5;
        }
        hue = nextHue(hue);
        //hue = hue % 360;

        mHsv[0] = (hue % 360);
        Log.i("bass+treble, value", hue + "\t" + mHsv[2]);
//        Log.i("", mid + " " + midTotal);
//        Log.i("bass", bass + " " + bassTotal + " " + bytes.length);

        mCallback.onBackgroundColorChanged(Color.HSVToColor(mHsv));
        invalidate();
    }


    public void updateFFT_backup(byte[] bytes) {
        mBytes = bytes;
        int divider = 20;
        // Calculate average for bass segment
        float bassTotal = 0;
        for (int i = 0; i < bytes.length * BASS_SEGMENT_SIZE; i++) {
            bassTotal += Math.abs(bytes[i]);
        }
        bass = bassTotal / divider * 5; // / (bytes.length * BASS_SEGMENT_SIZE);

        // Calculate average for mid segment
        float midTotal = 0;
        for (int i = (int) (bytes.length * BASS_SEGMENT_SIZE); i < bytes.length * MID_SEGMENT_SIZE; i++) {
            midTotal += Math.abs(bytes[i]);
        }
        mid = midTotal / divider; // / (bytes.length * MID_SEGMENT_SIZE);

        // Calculate average for terble segment
        float trebleTotal = 0;
        for (int i = (int) (bytes.length * MID_SEGMENT_SIZE); i < bytes.length; i++) {
            trebleTotal += Math.abs(bytes[i]);
        }
        treble = trebleTotal / divider; // / (bytes.length * TREBLE_SEGMENT_SIZE);


        //saturation
        mHsv[1] = (float) 1;
        //value


        mHsv[2] = nextValue(map(bass + treble, 0.01f, 140, 0.3f, 1.0f));

        hue += 0.5;
        if (mid > 15) {
            hue += 1;
        }
        if (bass + treble > 140) {
            hue += 20;
        } else if (bass + treble > 100) {
            hue += 8;
        }
//        mHsv[2] =(float)( bass>20?(mHsv[2]+0.05):(mHsv[2]-0.1));
//        if( mHsv[2] > 1){
//            mHsv[2] = 1;
//        }
        hue = hue % 360;
        mHsv[0] = nextHue(hue);
        Log.i("treble,mid,bass,value", treble + " " + mid + " " + bass + "\t\t" + mHsv[2]);
//        Log.i("", mid + " " + midTotal);
//        Log.i("bass", bass + " " + bassTotal + " " + bytes.length);

        mCallback.onBackgroundColorChanged(Color.HSVToColor(mHsv));
        invalidate();
    }

    /**
     * Restarts the visualization
     */
    public void restart() {
        mBassCircle.restartTrail();
        mMidSquare.restartTrail();
        mTrebleTriangle.restartTrail();
    }

    /** The methods below can be called to change the visualization **/

    /**
     * Sets the visibility of the bass circle
     *
     * @param showBass boolean determining if bass circle should be shown
     */
    public void setShowBass(boolean showBass) {
        this.showBass = showBass;
    }

    /**
     * Sets the visibility of the mid-range square
     *
     * @param showMid boolean determining if mid-range square should be shown
     */
    public void setShowMid(boolean showMid) {
        this.showMid = showMid;
    }

    /**
     * Sets the visibility of the treble triangle
     *
     * @param showTreble boolean determining if treble triangle should be shown
     */
    public void setShowTreble(boolean showTreble) {
        this.showTreble = showTreble;
    }

    /**
     * Sets the scale for the minimum size of the shape
     *
     * @param scale the scale for the size of the shape
     */
    public void setMinSizeScale(float scale) {
        TrailedShape.setMinSize(MIN_SIZE_DEFAULT * scale);
    }

    /**
     * Sets the color of the visualization. This should be one of the preference color values
     *
     * @param newColorKey
     */
    public void setColor(String newColorKey) {

        @ColorInt
        int shapeColor;

        @ColorInt
        int trailColor;

        if (newColorKey.equals(getContext().getString(R.string.pref_color_blue_value))) {
            shapeColor = ContextCompat.getColor(getContext(), R.color.shapeBlue);
            trailColor = ContextCompat.getColor(getContext(), R.color.trailBlue);
            backgroundColor = ContextCompat.getColor(getContext(), R.color.backgroundBlue);
        } else if (newColorKey.equals(getContext().getString(R.string.pref_color_green_value))) {
            shapeColor = ContextCompat.getColor(getContext(), R.color.shapeGreen);
            trailColor = ContextCompat.getColor(getContext(), R.color.trailGreen);
            backgroundColor = ContextCompat.getColor(getContext(), R.color.backgroundGreen);
        } else {
            shapeColor = ContextCompat.getColor(getContext(), R.color.shapeRed);
            trailColor = ContextCompat.getColor(getContext(), R.color.trailRed);
            backgroundColor = ContextCompat.getColor(getContext(), R.color.backgroundRed);
        }

        mBassCircle.setShapeColor(shapeColor);
        mMidSquare.setShapeColor(shapeColor);
        mTrebleTriangle.setShapeColor(shapeColor);

        mBassCircle.setTrailColor(trailColor);
        mMidSquare.setTrailColor(trailColor);
        mTrebleTriangle.setTrailColor(trailColor);
    }
}
