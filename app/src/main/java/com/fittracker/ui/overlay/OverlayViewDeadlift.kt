package com.fittracker.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.fittracker.R
import com.fittracker.utilits.Utility
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import java.util.Locale

/**
 * Deadlift overlay converted from the Squat overlay you shared.
 *
 * Highlights / differences:
 * - Focuses on SIDE view primarily (LEFT_FACE / RIGHT_FACE). FRONT view only draws landmarks & counters.
 * - Rep detection is based on wrist/bar path crossing below the knee and returning to lockout (hip+knee near extension).
 * - Hinge-quality checks:
 *   • Excessive knee bend at start (you’re squatting the deadlift)
 *   • Rounded back (hip angle too closed at the bottom compared to threshold)
 *   • Bar path too far from shins (wrist–ankle X distance)
 *   • Early hip rise (hips opening much faster than knees in the first half of the pull)
 *   • No full lockout (hip and knee not extended at top)
 */
class OverlayViewDeadlift(context: Context?, attrs: AttributeSet?) :
    View(context, attrs), TextToSpeech.OnInitListener {


    // ------- Runtime / Rendering -------
    private var runningMode: RunningMode = RunningMode.LIVE_STREAM
    private var results: PoseLandmarkerResult? = null
    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    // ------- Paints -------
    private val pointPaint = Paint()
    private val linePaint = Paint()
    private val errorPointPaint = Paint()
    private val statePaint = Paint()
    private val repsPaint = Paint()
    private val incorrectRepsPaint = Paint()
    private val anglePaint = Paint()
    private val calloutPaint = Paint()

    // ------- Angles / landmarks of interest -------
    private var kneeAngle: Float = 0f      // at knee (ankle-knee-hip)
    private var hipAngle: Float = 0f       // at hip (shoulder-hip-knee)

    // Side-view pixel positions we’ll use a lot
    private var xKnee = -100f
    private var yKnee = -100f
    private var xHip = -100f
    private var yHip = -100f
    private var xAnkle = -100f
    private var yAnkle = -100f
    private var xShoulder = -100f
    private var yShoulder = -100f
    private var xWrist = -100f
    private var yWrist = -100f

    // FRONT-view helpers (used lightly here)
    private var windowWidth = 0
    private var windowHeight = 0

    // Session state
    private var userFaceType = 0
    private var cameraFacing = -1
    private var isTimerCompleted = false
    private var isPlaying = true

    // Rep counters
    private var repsTotal = 0
    private var repsIncorrect = 0
    private var minBarYThisRep = Float.MAX_VALUE
    private var seenBottom = false

    // Error gating to avoid spam
    private var lastErrorSpeakAt = mutableMapOf<Int, Long>()

    // TTS
    private var tts: TextToSpeech? = null

    init {
        initPaints()
        tts = TextToSpeech(context, this)
    }

    private fun initPaints() {
        linePaint.color = Color.RED
        linePaint.strokeWidth = DL.LANDMARK_LINE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.WHITE
        pointPaint.strokeWidth = DL.LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.STROKE

        errorPointPaint.color = Color.RED
        errorPointPaint.strokeWidth = DL.ERROR_STROKE_WIDTH
        errorPointPaint.style = Paint.Style.STROKE

        statePaint.textSize = DL.TEXT_SIZE
        statePaint.color = Color.MAGENTA

        repsPaint.textSize = DL.TEXT_SIZE
        repsPaint.color = Color.BLUE

        incorrectRepsPaint.textSize = DL.TEXT_SIZE
        incorrectRepsPaint.color = Color.RED

        anglePaint.textSize = DL.ANGLE_TEXT
        anglePaint.color = Color.GREEN

        calloutPaint.color = Color.WHITE
        calloutPaint.textSize = DL.CALLOUT_TEXT
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val pose = results ?: return

        // Counters HUD
        canvas.drawText("Reps: $repsTotal", DL.TEXT_X, DL.TEXT_TOTAL_RESP_Y, repsPaint)
        canvas.drawText("Incorrect: $repsIncorrect", DL.TEXT_X, DL.TEXT_INCORRECT_RESP_Y, incorrectRepsPaint)

        // Basic visibility check
        for (landmark in pose.landmarks()) {
            if (!Utility.isUserFullyVisible(landmark, windowWidth, windowHeight)) return
        }
        if (!isPlaying) return
        if (!isTimerCompleted) return

        // Draw skeleton
        for (landmark in pose.landmarks()) {
            for (n in landmark) {
                canvas.drawCircle(n.x() * imageWidth * scaleFactor, n.y() * imageHeight * scaleFactor, 3f, pointPaint)
            }
            PoseLandmarker.POSE_LANDMARKS.forEach {
                canvas.drawLine(
                    pose.landmarks()[0][it!!.start()].x() * imageWidth * scaleFactor,
                    pose.landmarks()[0][it.start()].y() * imageHeight * scaleFactor,
                    pose.landmarks()[0][it.end()].x() * imageWidth * scaleFactor,
                    pose.landmarks()[0][it.end()].y() * imageHeight * scaleFactor,
                    linePaint
                )
            }
        }

        // Compute and display angles for side view
        val kAngle = (kneeAngle * 10).roundToInt() / 10f
        val hAngle = (hipAngle * 10).roundToInt() / 10f
        if (userFaceType == DL.LEFT_FACE || userFaceType == DL.RIGHT_FACE) {
            canvas.drawText("Knee: $kAngle°", xKnee * imageWidth * scaleFactor, yKnee * imageHeight * scaleFactor, anglePaint)
            canvas.drawText("Hip: $hAngle°", xHip * imageWidth * scaleFactor, yHip * imageHeight * scaleFactor, anglePaint)

            handleDeadliftSide(canvas)
        } else {
            // FRONT: show angles lightly
            canvas.drawText("Knee: $kAngle°", xKnee * imageWidth * scaleFactor, yKnee * imageHeight * scaleFactor, anglePaint)
            canvas.drawText("Hip: $hAngle°", xHip * imageWidth * scaleFactor, yHip * imageHeight * scaleFactor, anglePaint)
        }
    }

    private fun handleDeadliftSide(canvas: Canvas) {
        // 1) Rep phase detection driven by wrist (bar) vertical travel vs knee
        val wristY = yWrist * imageHeight * scaleFactor
        val kneeY = yKnee * imageHeight * scaleFactor

        // Track deepest point of the bar in this rep
        if (wristY < Float.MAX_VALUE) {
            minBarYThisRep = min(minBarYThisRep, wristY)
        }

        val atBottom = wristY > (kneeY + DL.BAR_BELOW_KNEE_YPX) // bar/wrist below knee enough
        val atTopLockout = (hipAngle >= DL.HIP_LOCKOUT_MIN && kneeAngle >= DL.KNEE_LOCKOUT_MIN)

        // bottom reached?
        if (atBottom) seenBottom = true

        // top reached after bottom -> count rep
        if (seenBottom && atTopLockout) {
            repsTotal++
            // Incorrect if no full lockout or bar path too far or excessive knee bend at start
            val incorrect =
                !atTopLockout ||
                barTooFarFromShins() ||
                excessiveKneeBendAtStart() ||
                roundedBackAtBottom()
            if (incorrect) repsIncorrect++

            seenBottom = false
            minBarYThisRep = Float.MAX_VALUE
        }

        // 2) Errors & cues during motion
        if (excessiveKneeBendAtStart()) {
            cue(canvas, xKnee, yKnee, "Less knee bend — hinge at the hips", DL.CUE_EXCESS_KNEE)
        }
        if (roundedBackAtBottom()) {
            cue(canvas, xHip, yHip, "Keep your back neutral (brace)", DL.CUE_ROUNDED_BACK)
        }
        if (barTooFarFromShins()) {
            cue(canvas, xAnkle, yAnkle, "Keep bar close to shins", DL.CUE_BAR_FAR)
        }
        if (earlyHipRise()) {
            cue(canvas, xHip, yHip, "Don't let hips shoot up first", DL.CUE_HIPS_SHOOT)
        }
        if (seenBottom && !atTopLockout) {
            // approaching top but not locked out
            cue(canvas, xHip, yHip, "Squeeze glutes — stand tall (lockout)", DL.CUE_LOCKOUT)
        }
    }

    private fun barTooFarFromShins(): Boolean {
        val dx = abs((xWrist - xAnkle) * imageWidth * scaleFactor)
        return dx > DL.BAR_ANKLE_X_MAX
    }

    private fun excessiveKneeBendAtStart(): Boolean {
        // If at the start/top position the knee is very bent, you’re squatting the pull
        return kneeAngle < DL.START_KNEE_MIN
    }

    private fun roundedBackAtBottom(): Boolean {
        // Proxy: hip angle too closed at bottom implies rounding/hunching
        return seenBottom && hipAngle < DL.BOTTOM_HIP_MIN
    }

    // Simple heuristic: if hip angle opens much faster than knees in early ascent
    private var prevHipAngle: Float = 0f
    private var prevKneeAngle: Float = 0f
    private var earlyPhaseFrames = 0

    private fun earlyHipRise(): Boolean {
        val hipDelta = hipAngle - prevHipAngle
        val kneeDelta = kneeAngle - prevKneeAngle
        prevHipAngle = hipAngle
        prevKneeAngle = kneeAngle

        // Track a few frames after bottom
        if (seenBottom) {
            earlyPhaseFrames = 0
            return false
        } else {
            earlyPhaseFrames = (earlyPhaseFrames + 1).coerceAtMost(10)
        }
        // If hips open > DL.HIP_FAST_OPEN and knees barely extend in first few frames
        return earlyPhaseFrames in 1..6 && hipDelta > DL.HIP_FAST_OPEN && kneeDelta < DL.KNEE_SLOW_OPEN
    }

    private fun cue(canvas: Canvas, nx: Float, ny: Float, message: String, type: Int) {
        val now = System.currentTimeMillis()
        val last = lastErrorSpeakAt[type] ?: 0L
        val shouldSpeak = now - last > DL.SPEAK_DEBOUNCE_MS
        if (shouldSpeak) {
            speakOut(message)
            lastErrorSpeakAt[type] = now
        }
        drawCallout(canvas, nx * imageWidth * scaleFactor, ny * imageHeight * scaleFactor, message)
    }

    private fun drawCallout(canvas: Canvas, x: Float, y: Float, message: String) {
        val textHeight = abs(calloutPaint.fontMetrics.top)
        val pad = 10
        val bg = RectF(x, y, x + calloutPaint.measureText(message) + pad * 2, y + textHeight + pad * 2)
        val bgPaint = Paint().apply {
            color = ContextCompat.getColor(context!!, R.color.purple_200)
            style = Paint.Style.FILL
            alpha = 180
        }
        canvas.drawRoundRect(bg, 10f, 10f, bgPaint)
        val tx = bg.left + (bg.width() - calloutPaint.measureText(message)) / 2
        val ty = bg.centerY() - (calloutPaint.descent() + calloutPaint.ascent()) / 2
        canvas.drawText(message, tx, ty, calloutPaint)
    }

    fun setResults(
        poseResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE,
        cameraFacing: Int,
        userFaceType: Int,
        // Pixels (normalized 0..1) for relevant joints — SIDE view recommended
        xHip: Float,
        yHip: Float,
        xKnee: Float,
        yKnee: Float,
        xAnkle: Float,
        yAnkle: Float,
        xShoulder: Float,
        yShoulder: Float,
        xWrist: Float,
        yWrist: Float,
        isTimerCompleted: Boolean,
        windowWidth: Int,
        windowHeight: Int,
        isPlaying: Boolean
    ) {
        this.results = poseResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        this.userFaceType = userFaceType
        this.xHip = xHip
        this.yHip = yHip
        this.xKnee = xKnee
        this.yKnee = yKnee
        this.xAnkle = xAnkle
        this.yAnkle = yAnkle
        this.xShoulder = xShoulder
        this.yShoulder = yShoulder
        this.xWrist = xWrist
        this.yWrist = yWrist
        this.windowWidth = windowWidth
        this.windowHeight = windowHeight
        this.isPlaying = isPlaying
        this.isTimerCompleted = isTimerCompleted

        if (this.cameraFacing != cameraFacing) {
            repsTotal = 0
            repsIncorrect = 0
        }
        this.cameraFacing = cameraFacing

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> min(width * 1f / imageWidth, height * 1f / imageHeight)
            RunningMode.LIVE_STREAM -> max(width * 1f / imageWidth, height * 1f / imageHeight)
        }

        // Update angles (use your Utility.calculateAngles: angle at mid-point of triplet)
        // Knee angle at knee from (ankle-knee-hip)
        val pAnkX = xAnkle * imageWidth * scaleFactor
        val pAnkY = yAnkle * imageHeight * scaleFactor
        val pKneX = xKnee * imageWidth * scaleFactor
        val pKneY = yKnee * imageHeight * scaleFactor
        val pHipX = xHip * imageWidth * scaleFactor
        val pHipY = yHip * imageHeight * scaleFactor
        val pShX = xShoulder * imageWidth * scaleFactor
        val pShY = yShoulder * imageHeight * scaleFactor

        kneeAngle = Utility.calculateAngles(pAnkX, pAnkY, pKneX, pKneY, pHipX, pHipY)
        hipAngle = Utility.calculateAngles(pShX, pShY, pHipX, pHipY, pKneX, pKneY)

        invalidate()
    }

    // -------- TTS --------
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // no-op
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun speakOut(text: String) {
        if (tts?.isSpeaking == true) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    // --------- Deadlift constants ---------
    private object DL {
        // Faces (reuse your app’s constants values if you have them; here are safe defaults)
        const val FRONT_FACE = 0
        const val LEFT_FACE = 1
        const val RIGHT_FACE = 2

        // Drawing
        const val LANDMARK_LINE_WIDTH = 4f
        const val LANDMARK_STROKE_WIDTH = 3f
        const val ERROR_STROKE_WIDTH = 5f
        const val TEXT_SIZE = 42f
        const val ANGLE_TEXT = 34f
        const val CALLOUT_TEXT = 32f
        const val TEXT_X = 40f
        const val TEXT_TOTAL_RESP_Y = 80f
        const val TEXT_INCORRECT_RESP_Y = 130f

        // Rep logic
        const val BAR_BELOW_KNEE_YPX = 20f      // how much lower than knee to consider “past knees”
        const val HIP_LOCKOUT_MIN = 165f        // hip near extension at top
        const val KNEE_LOCKOUT_MIN = 165f       // knee near extension at top

        // Error thresholds
        const val START_KNEE_MIN = 105f         // too bent at start means squatting the pull
        const val BOTTOM_HIP_MIN = 55f          // <55° at hip at bottom => likely rounded/hunched
        const val BAR_ANKLE_X_MAX = 70f         // px: wrist should be within this of ankle X during pull
        const val HIP_FAST_OPEN = 4f            // deg/frame considered fast hip opening
        const val KNEE_SLOW_OPEN = 1.2f         // deg/frame considered slow knee opening

        // Debounce TTS
        const val SPEAK_DEBOUNCE_MS = 2500L

        // Cue types
        const val CUE_EXCESS_KNEE = 1
        const val CUE_ROUNDED_BACK = 2
        const val CUE_BAR_FAR = 3
        const val CUE_HIPS_SHOOT = 4
        const val CUE_LOCKOUT = 5
    }
}
