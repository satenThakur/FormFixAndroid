package com.fittracker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.fittracker.utilits.Constants
import com.fittracker.utilits.Utility
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class OverlayViewPushUps(context: Context?, attrs: AttributeSet?) :
    View(context, attrs), TextToSpeech.OnInitListener {
    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var statePaint = Paint()
    private var repsPaint = Paint()
    private var incorrectRepsPaint = Paint()
    private var anglePaint = Paint()
    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var kneesAngle: Float = 0.0f
    private var hipsAngle: Float = 0.0f
    private var elbowsAngle: Float = 0.0f
    private var shouldersAngle: Float = 0.0f
    private var respCountTotal = 0
    private var respCountIncorrect = 0
    private var statesSet = mutableSetOf<Int>()
    private var isTimerCompleted = false
    private var cameraFacing=0;
    private var windowHeight = 0
    private var windowWidth = 0
    private var yOfToe = -100f
    private var yoFShoulder = -100f
    private var userFaceType = 0
    var xKnee = 0f
    var yKnee = 0f
    var xHip = 0f
    var yHip = 0f
    var xShoulder = 0f
    var yShoulder  = 0f
    var xElbow=0f
    var yElbow=0f
    private var isPlaying=true
    private fun initPaints() {
        linePaint.color = Color.WHITE
        linePaint.strokeWidth = Constants.LANDMARK_LINE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.RED
        pointPaint.strokeWidth = Constants.LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.STROKE

        statePaint.textSize = Constants.TEXT_SIZE
        statePaint.color = Color.MAGENTA

        repsPaint.textSize = Constants.TEXT_SIZE
        repsPaint.color = Color.BLUE

        incorrectRepsPaint.textSize = Constants.TEXT_SIZE
        incorrectRepsPaint.color = Color.RED

        anglePaint.textSize = Constants.ANGLE_TEXT
        anglePaint.color = Color.MAGENTA

    }

    init {
        initPaints()
    }

    fun setResults(
        poseLandmarkResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE,
        cameraFacing: Int,
        userFaceType: Int,
        isTimerCompleted: Boolean,
        kneeAngle: Float,
        hipAngle: Float,
        shoulderAngle: Float,
        elbowAngle: Float,
        xKnee: Float,
        yKnee: Float,
        xHip: Float,
        yHip: Float,
        xShoulder: Float,
        yShoulder: Float,
        xElbow: Float,
        yElbow: Float,
        isPlaying: Boolean,
    ) {
        results = poseLandmarkResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        this.kneesAngle = kneeAngle
        this.hipsAngle = hipAngle
        this.shouldersAngle = shoulderAngle
        this.elbowsAngle=elbowAngle
        this.userFaceType = userFaceType
        this.isTimerCompleted=isTimerCompleted
        this.xKnee=xKnee
        this.yKnee=yKnee
        this.xHip=xHip
        this.yHip=yHip
        this.xShoulder=xShoulder
        this.yShoulder=yShoulder
        this.xElbow=xElbow
        this.yElbow=yElbow
        this.isPlaying=isPlaying
        this.cameraFacing = cameraFacing
        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }

            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the landmarks to match with the size that the captured images will be displayed
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
    }

    override fun onInit(p0: Int) {
        System.out.println("d")
    }

    @SuppressLint("WrongConstant", "SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkResult ->

            if (!isTimerCompleted)
                return
            Log.e("PushUPS","inside draw  timerCompleted")
            for (landmark in poseLandmarkResult.landmarks()) {
                if (!Utility.isUserFullyVisible(landmark, windowWidth, windowHeight)) {
                    return
                }
            }
            if(!isPlaying)
                return
            if (yoFShoulder > yOfToe)
                return

            Log.e("PushUPS","yoFShoulder > yOfToe condition passed")
            for (landmark in poseLandmarkResult.landmarks()) {
                 for (normalizedLandmark in landmark) {
                      canvas.drawPoint(
                          normalizedLandmark.x() * imageWidth * scaleFactor,
                          normalizedLandmark.y() * imageHeight * scaleFactor,
                          pointPaint
                      )
                  }
                 PoseLandmarker.POSE_LANDMARKS.forEach {
                     canvas.drawLine(
                         poseLandmarkResult.landmarks()[0][it!!.start()]
                             .x() * imageWidth * scaleFactor,
                         poseLandmarkResult.landmarks()[0][it.start()]
                             .y() * imageHeight * scaleFactor,
                         poseLandmarkResult.landmarks()[0][it.end()]
                             .x() * imageWidth * scaleFactor,
                         poseLandmarkResult.landmarks()[0][it.end()]
                             .y() * imageHeight * scaleFactor,
                         linePaint
                     )
                 }


            }
            Log.e("PushUPS","landmark lines and poits draw")
            canvas.drawText(
                resources.getString(R.string.reps) + respCountTotal,
                Constants.TEXT_X,
                Constants.TEXT_TOTAL_RESP_Y,
                repsPaint
            )
            canvas.drawText(
                resources.getString(R.string.incorrect_reps) + respCountIncorrect,
                Constants.TEXT_X,
                Constants.TEXT_INCORRECT_RESP_Y,
                incorrectRepsPaint
            )
            val kneeAngle = (kneesAngle * 10).roundToInt() / 10
            val hipAngle = (hipsAngle * 10).roundToInt() / 10
            val shoulderAngle = (shouldersAngle * 10).roundToInt() / 10
            val elbowAngle = (elbowsAngle * 10).roundToInt() / 10




            Log.e("PushUPS","userFaceType"+userFaceType)

            when(userFaceType){
                Constants.FRONT_FACE ->
                {
                    when (Utility.getPushUpState(elbowAngle, shoulderAngle, Constants.FRONT_FACE)) {
                        Constants.STATE_UP -> {
                            canvas.drawText(
                                resources.getString(R.string.state_up),
                                Constants.TEXT_X,
                                Constants.TEXT_STATE_Y,
                                statePaint
                            )
                            if (statesSet.contains(Constants.STATE_DOWN) && statesSet.contains(Constants.STATE_MOVING)) {
                                respCountTotal++
                                statesSet.clear()
                            } else {

                            }
                        }

                        Constants.STATE_MOVING -> {
                            canvas.drawText(
                                resources.getString(R.string.state_moving),
                                Constants.TEXT_X,
                                Constants.TEXT_STATE_Y,
                                statePaint
                            )
                            statesSet.add(Constants.STATE_MOVING)
                        }

                        Constants.STATE_DOWN -> {
                            canvas.drawText(
                                resources.getString(R.string.state_down),
                                Constants.TEXT_X,
                                Constants.TEXT_STATE_Y,
                                statePaint
                            )
                            statesSet.add(Constants.STATE_DOWN)
                        }
                        Constants.STATE_UN_DECIDED -> {
                            canvas.drawText(
                                resources.getString(R.string.empty_string),
                                Constants.TEXT_X,
                                Constants.TEXT_STATE_Y,
                                statePaint
                            )
                        }

                        else -> {}
                    }
                }
                Constants.RIGHT_FACE, Constants.LEFT_FACE ->
                { canvas.drawText(
                    kneeAngle.toString(),
                    xKnee * imageWidth * scaleFactor,
                    yKnee * imageHeight * scaleFactor,
                    anglePaint
                )
                    canvas.drawText(
                        hipAngle.toString(),
                        xHip * imageWidth * scaleFactor,
                        yHip * imageHeight * scaleFactor,
                        anglePaint
                    )
                    canvas.drawText(
                        shoulderAngle.toString(),
                        xShoulder * imageWidth * scaleFactor,
                        yShoulder * imageHeight * scaleFactor,
                        anglePaint
                    )

                    canvas.drawText(
                        elbowAngle.toString(),
                        xElbow * imageWidth * scaleFactor,
                        yElbow * imageHeight * scaleFactor,
                        anglePaint
                    )
                    when (Utility.getPushUpState(elbowAngle, shoulderAngle, Constants.LEFT_FACE)) {
                        Constants.STATE_UP -> {
                            canvas.drawText(
                                resources.getString(R.string.state_up),
                                Constants.TEXT_X,
                                Constants.TEXT_STATE_Y,
                                statePaint
                            )
                            if (statesSet.contains(Constants.STATE_DOWN) && statesSet.contains(Constants.STATE_MOVING)) {
                                respCountTotal++
                                if (statesSet.contains(Constants.SQUAT_INCORRECT)) {
                                    respCountIncorrect++
                                }
                                statesSet.clear()
                            } else {

                            }

                        }

                        Constants.STATE_MOVING -> {
                            //Right/Left Face STATE_MOVING
                            canvas.drawText(
                                resources.getString(R.string.state_moving),
                                Constants.TEXT_X,
                                Constants.TEXT_STATE_Y,
                                statePaint
                            )
                            statesSet.add(Constants.STATE_MOVING)
                        }

                        Constants.STATE_DOWN -> {
                            canvas.drawText(
                                resources.getString(R.string.state_down),
                                Constants.TEXT_X,
                                Constants.TEXT_STATE_Y,
                                statePaint
                            )
                            statesSet.add(Constants.STATE_DOWN)


                        }

                        Constants.STATE_UN_DECIDED -> {
                            canvas.drawText(
                                resources.getString(R.string.empty_string),
                                Constants.TEXT_X,
                                Constants.TEXT_STATE_Y,
                                statePaint
                            )
                        }

                        else -> {}
                    }
                }

                else -> {}
            }

        }
    }
}


/*
def is_pushup_pose_correct(wrist_y, toe_y, hip_angle, knee_angle,state, shoulder_wrist_diff):
    diff = abs(wrist_y - toe_y)
    if diff < 180 and hip_angle >= 168 and knee_angle >= 165 and state == 1 and shoulder_wrist_diff > 40:
        return False
    elif diff < 180 and hip_angle >= 168 and knee_angle >= 165:
        return True
    else:
        return False
 */