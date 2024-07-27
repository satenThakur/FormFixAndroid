package com.fittracker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.fittracker.model.ErrorMessage
import com.fittracker.utilits.Constants
import com.fittracker.utilits.Constants.ANGLE_TEXT
import com.fittracker.utilits.Constants.HEEL_MAX_ANGLE
import com.fittracker.utilits.Constants.HEEL_MIN_ANGLE
import com.fittracker.utilits.Constants.TUCK_HIPS
import com.fittracker.utilits.Constants.KNEES_CROSSING_TOES
import com.fittracker.utilits.Constants.LANDMARK_LINE_WIDTH
import com.fittracker.utilits.Constants.LANDMARK_STROKE_WIDTH
import com.fittracker.utilits.Constants.LINE_LENGTH
import com.fittracker.utilits.Constants.MASK_TEXT
import com.fittracker.utilits.Constants.SQUAT_INCORRECT
import com.fittracker.utilits.Constants.STATE_DOWN
import com.fittracker.utilits.Constants.STATE_MOVING
import com.fittracker.utilits.Constants.STATE_UN_DECIDED
import com.fittracker.utilits.Constants.STATE_UP
import com.fittracker.utilits.Constants.TEXT_INCORRECT_RESP_Y
import com.fittracker.utilits.Constants.TEXT_SIZE
import com.fittracker.utilits.Constants.TEXT_STATE_Y
import com.fittracker.utilits.Constants.TEXT_TOTAL_RESP_Y
import com.fittracker.utilits.Constants.TEXT_X
import com.fittracker.utilits.Constants.TOE_KNEE_X_DIFFS_MIN_THRESHOLD
import com.fittracker.utilits.Utility
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class OverlayViewPushups(context: Context?, attrs: AttributeSet?) :
    View(context, attrs), TextToSpeech.OnInitListener {
    override fun onInit(p0: Int) {
        println("")
    }

}