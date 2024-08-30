package com.fittracker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import com.fittracker.utilits.ConstantsPushUps
import com.fittracker.utilits.ConstantsSquats
import com.fittracker.utilits.ConstantsSquats.SQUAT_INCORRECT
import com.fittracker.utilits.ConstantsSquats.TEXT_HIPANKLEAVERAGE
import com.fittracker.utilits.ConstantsSquats.TEXT_X
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
    private var correctPosepPaint = Paint()
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
    var kneeX = 0f
    var kneeY = 0f
    var hipX = 0f
    var hipY = 0f
    var shoulderX = 0f
    var shoulderY  = 0f
    var elbowX=0f
    var elbowY=0f
    var wristY=0f
    var toeY=0f
    private var isPlaying=true
    private fun initPaints() {
        linePaint.color = Color.WHITE
        linePaint.strokeWidth = ConstantsSquats.LANDMARK_LINE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.RED
        pointPaint.strokeWidth = ConstantsSquats.LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.STROKE

        statePaint.textSize = ConstantsSquats.TEXT_SIZE
        statePaint.color = Color.MAGENTA

        repsPaint.textSize = ConstantsSquats.TEXT_SIZE
        repsPaint.color = Color.BLUE

        incorrectRepsPaint.textSize = ConstantsSquats.TEXT_SIZE
        incorrectRepsPaint.color = Color.RED
        correctPosepPaint.textSize = ConstantsSquats.TEXT_SIZE
        correctPosepPaint.color = Color.GREEN

        anglePaint.textSize = ConstantsSquats.ANGLE_TEXT
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
        wristY: Float,
        toeY: Float,
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
        this.kneeX=xKnee
        this.kneeY=yKnee
        this.hipX=xHip
        this.hipY=yHip
        this.shoulderX=xShoulder
        this.shoulderY=yShoulder
        this.elbowX=xElbow
        this.elbowY=yElbow
        this.isPlaying=isPlaying
        this.toeY=toeY
        this.wristY=wristY
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
            Utility.Log(ConstantsPushUps.PUSH_UP_TAG,"inside draw  timerCompleted")
            for (landmark in poseLandmarkResult.landmarks()) {
                if (!Utility.isUserFullyVisible(landmark, windowWidth, windowHeight)) {
                    return
                }
            }
            if(!isPlaying)
                return
            if (yoFShoulder > yOfToe)
                return

            Utility.Log(ConstantsPushUps.PUSH_UP_TAG,"yoFShoulder > yOfToe condition passed")
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
            Utility.Log(ConstantsPushUps.PUSH_UP_TAG,"landmark lines and poits draw")
            canvas.drawText(
                resources.getString(R.string.reps) + respCountTotal,
                ConstantsSquats.TEXT_X,
                ConstantsSquats.TEXT_TOTAL_RESP_Y,
                repsPaint
            )
            canvas.drawText(
                resources.getString(R.string.incorrect_reps) + respCountIncorrect,
                ConstantsSquats.TEXT_X,
                ConstantsSquats.TEXT_INCORRECT_RESP_Y,
                incorrectRepsPaint
            )
            val kneeAngle = (kneesAngle * 10).roundToInt() / 10
            val hipAngle = (hipsAngle * 10).roundToInt() / 10
            val shoulderAngle = (shouldersAngle * 10).roundToInt() / 10
            val elbowAngle = (elbowsAngle * 10).roundToInt() / 10

            when(userFaceType){
                ConstantsSquats.FRONT_FACE ->
                {
                    canvas.drawText(
                        shoulderAngle.toString(),
                        shoulderX * imageWidth * scaleFactor,
                        shoulderY * imageHeight * scaleFactor,
                        anglePaint
                    )
                    canvas.drawText(
                        elbowAngle.toString(),
                        elbowX * imageWidth * scaleFactor,
                        elbowY * imageHeight * scaleFactor,
                        anglePaint
                    )

                    when (Utility.getPushUpState(elbowAngle, shoulderAngle, ConstantsSquats.FRONT_FACE)) {
                        ConstantsSquats.STATE_UP -> {
                            canvas.drawText(
                                resources.getString(R.string.state_up),
                                ConstantsSquats.TEXT_X,
                                ConstantsSquats.TEXT_STATE_Y,
                                statePaint
                            )
                            if (statesSet.contains(ConstantsSquats.STATE_DOWN) && statesSet.contains(
                                    ConstantsSquats.STATE_MOVING)) {
                                respCountTotal++
                                statesSet.clear()
                            } else {

                            }
                        }

                        ConstantsSquats.STATE_MOVING -> {
                            canvas.drawText(
                                resources.getString(R.string.state_moving),
                                ConstantsSquats.TEXT_X,
                                ConstantsSquats.TEXT_STATE_Y,
                                statePaint
                            )
                            statesSet.add(ConstantsSquats.STATE_MOVING)
                        }

                        ConstantsSquats.STATE_DOWN -> {
                            canvas.drawText(
                                resources.getString(R.string.state_down),
                                ConstantsSquats.TEXT_X,
                                ConstantsSquats.TEXT_STATE_Y,
                                statePaint
                            )
                            statesSet.add(ConstantsSquats.STATE_DOWN)
                        }
                        ConstantsSquats.STATE_UN_DECIDED -> {
                            canvas.drawText(
                                resources.getString(R.string.empty_string),
                                ConstantsSquats.TEXT_X,
                                ConstantsSquats.TEXT_STATE_Y,
                                statePaint
                            )
                        }

                        else -> {}
                    }
                }
                ConstantsSquats.RIGHT_FACE, ConstantsSquats.LEFT_FACE ->
                { canvas.drawText(
                    kneeAngle.toString(),
                    kneeX * imageWidth * scaleFactor,
                    kneeY * imageHeight * scaleFactor,
                    anglePaint
                )
                    canvas.drawText(
                        hipAngle.toString(),
                        hipX * imageWidth * scaleFactor,
                        hipY * imageHeight * scaleFactor,
                        anglePaint
                    )
                    canvas.drawText(
                        shoulderAngle.toString(),
                        shoulderX * imageWidth * scaleFactor,
                        shoulderY * imageHeight * scaleFactor,
                        anglePaint
                    )

                    canvas.drawText(
                        elbowAngle.toString(),
                        elbowX * imageWidth * scaleFactor,
                        elbowY * imageHeight * scaleFactor,
                        anglePaint
                    )

                    if(!Utility.startPushUpTracking(wristY, toeY,hipsAngle,kneesAngle)){
                        return
                    }
                    when (Utility.getPushUpState(elbowAngle, shoulderAngle, ConstantsSquats.LEFT_FACE)) {
                        ConstantsSquats.STATE_UP -> {
                            canvas.drawText(
                                resources.getString(R.string.state_up),
                                ConstantsSquats.TEXT_X,
                                ConstantsSquats.TEXT_STATE_Y,
                                statePaint
                            )

                            if(Utility.isPushUpPoseCorrect(hipsAngle, kneesAngle, ConstantsSquats.STATE_UP,
                                    Utility.getShoulderElbowXDiff(shoulderX,elbowX, ConstantsSquats.STATE_UP),Utility.getWristToeYDiff(wristY,toeY))){
                                canvas.drawText(
                                    "Correct Pose",
                                    TEXT_X,
                                    TEXT_HIPANKLEAVERAGE,
                                    correctPosepPaint
                                )

                            }else {
                                if(statesSet.contains(ConstantsSquats.STATE_DOWN) && statesSet.contains(ConstantsSquats.STATE_MOVING)) {
                                    statesSet.add(SQUAT_INCORRECT)
                                }
                                canvas.drawText(
                                    "Incorrect Pose",
                                    TEXT_X,
                                    TEXT_HIPANKLEAVERAGE,
                                    incorrectRepsPaint
                                )
                            }
                            if (statesSet.contains(ConstantsSquats.STATE_DOWN) && statesSet.contains(
                                    ConstantsSquats.STATE_MOVING)) {
                                respCountTotal++
                                if (statesSet.contains(ConstantsSquats.SQUAT_INCORRECT)) {
                                    respCountIncorrect++
                                }
                                statesSet.clear()
                            } else {

                            }

                        }

                        ConstantsSquats.STATE_MOVING -> {
                            //Right/Left Face STATE_MOVING
                            canvas.drawText(
                                resources.getString(R.string.state_moving),
                                ConstantsSquats.TEXT_X,
                                ConstantsSquats.TEXT_STATE_Y,
                                statePaint
                            )
                            statesSet.add(ConstantsSquats.STATE_MOVING)

                            if(Utility.isPushUpPoseCorrect(hipsAngle, kneesAngle, ConstantsSquats.STATE_MOVING, Utility.getShoulderElbowXDiff(shoulderX,elbowX, ConstantsSquats.STATE_MOVING),Utility.getWristToeYDiff(wristY,toeY))){
                                canvas.drawText(
                                    "Correct Pose",
                                    TEXT_X,
                                    TEXT_HIPANKLEAVERAGE,
                                    correctPosepPaint
                                )

                            }else {
                                if(statesSet.contains(ConstantsSquats.STATE_DOWN)) {
                                    statesSet.add(SQUAT_INCORRECT)
                                }
                                canvas.drawText(
                                    "Incorrect Pose",
                                    TEXT_X,
                                    TEXT_HIPANKLEAVERAGE,
                                    incorrectRepsPaint
                                )
                            }

                        }

                        ConstantsSquats.STATE_DOWN -> {
                            canvas.drawText(
                                resources.getString(R.string.state_down),
                                ConstantsSquats.TEXT_X,
                                ConstantsSquats.TEXT_STATE_Y,
                                statePaint
                            )
                            statesSet.add(ConstantsSquats.STATE_DOWN)

                            if(Utility.isPushUpPoseCorrect(hipsAngle, kneesAngle, ConstantsSquats.STATE_DOWN, Utility.getShoulderElbowXDiff(shoulderX,elbowX, ConstantsSquats.STATE_DOWN),Utility.getWristToeYDiff(wristY,toeY))){

                                canvas.drawText(
                                    "Correct Pose",
                                    TEXT_X,
                                    TEXT_HIPANKLEAVERAGE,
                                    correctPosepPaint
                                )

                            }else {
                                statesSet.add(SQUAT_INCORRECT)
                                canvas.drawText(
                                    "Incorrect Pose",
                                    TEXT_X,
                                    TEXT_HIPANKLEAVERAGE,
                                    incorrectRepsPaint
                                )
                            }

                        }

                        ConstantsSquats.STATE_UN_DECIDED -> {
                            canvas.drawText(
                                resources.getString(R.string.empty_string),
                                ConstantsSquats.TEXT_X,
                                ConstantsSquats.TEXT_STATE_Y,
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