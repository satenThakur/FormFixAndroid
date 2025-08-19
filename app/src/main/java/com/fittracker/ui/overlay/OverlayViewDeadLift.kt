package com.fittracker.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
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
import com.fittracker.model.ErrorMessage
import com.fittracker.utilits.ConstantsDeadLift
import com.fittracker.utilits.ConstantsDeadLift.ANGLE_TEXT
import com.fittracker.utilits.ConstantsDeadLift.ERROR_STROKE_WIDTH
import com.fittracker.utilits.ConstantsDeadLift.LANDMARK_LINE_WIDTH
import com.fittracker.utilits.ConstantsDeadLift.LANDMARK_STROKE_WIDTH
import com.fittracker.utilits.ConstantsDeadLift.MASK_TEXT
import com.fittracker.utilits.ConstantsDeadLift.SQUAT_INCORRECT
import com.fittracker.utilits.ConstantsDeadLift.TEXT_INCORRECT_RESP_Y
import com.fittracker.utilits.ConstantsDeadLift.TEXT_SIZE
import com.fittracker.utilits.ConstantsDeadLift.TEXT_STATE_Y
import com.fittracker.utilits.ConstantsDeadLift.TEXT_TOTAL_RESP_Y
import com.fittracker.utilits.ConstantsDeadLift.TEXT_X
import com.fittracker.utilits.FormFixSharedPreferences
import com.fittracker.utilits.DeadLiftUtility
import com.fittracker.utilits.FormFixConstants
import com.fittracker.utilits.FormFixConstants.STATE_DOWN
import com.fittracker.utilits.FormFixConstants.STATE_MOVING
import com.fittracker.utilits.FormFixConstants.STATE_UN_DECIDED
import com.fittracker.utilits.FormFixConstants.STATE_UP
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class OverlayViewDeadlift(context: Context?, attrs: AttributeSet?) : View(context, attrs), TextToSpeech.OnInitListener {
    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var pointErrorPaint = Paint()
    private var linePaint = Paint()
    private var pLinePaint = Paint()
    private var statePaint = Paint()
    private var repsPaint = Paint()
    private var incorrectRepsPaint = Paint()
    private var anglePaint = Paint()
    private var maskPaint = Paint()
    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var leftKneeAngle: Float = 0.0f
    private var leftHipAngle: Float = 0.0f
    private var heelAngle: Float = 0.0f
    private var shoulderAngle: Float = 0.0f
    private var userFaceType = 0
    private var respCountTotal = 0
    private var respCountIncorrect = 0
    private var statesSet = mutableSetOf<Int>()
    private var rightToeX=-100F
    private var leftToeX=-100F
    private var xofRightHip=-100F
    private var xofLeftHip=-100F
    private var xKnee = -100F
    private var yKnee = -100F
    private var xHip = -100F
    private var yHip = -100F
    private var toeX = -100F
    private var xHeel = -100F
    private var yHeel = -100F
    private var xofLeftKnee = -100F
    private var xofRightKnee = -100F
    private var xofLeftToe = -100F
    private var xofRightToe = -100F
    private var yOfToe = -100f
    private var yoFShoulder = -100f
    private var yForNose = -100f
    private var shulderY = -100f
    private var shoulderx = -100f
    private var ankleX = -100f
    private var ankleY = -100f
    private var yOfRightHeel=-100F
    private var yOfLeftHeel=-100F
    private var yOfRightToe=-100F
    private var yOfLeftToe=-100F
    private var yofRightShoulder=-100F
    private var yofLeftShoulder=-100F
    private var cameraFacing = -1
    private var minKneeAngle = Float.MAX_VALUE
    private var isTimerCompleted = false
    private var tts: TextToSpeech? = null
    var errorMessageList = ArrayList<ErrorMessage>()
    private var windowHeight = 0
    private var windowWidth = 0
    private var isPlaying=true
    private var userHeight=0

    init {
        initPaints()
        errorMessageList.clear()
        tts = TextToSpeech(context, this)
        var heightString= context?.let { FormFixSharedPreferences.getSharedPrefStringValue(it, FormFixConstants.HEIGHT) }
        userHeight= heightString?.toInt()!!

    }

    private fun initPaints() {
        linePaint.color = Color.RED
        linePaint.strokeWidth = LANDMARK_LINE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pLinePaint.color = ContextCompat.getColor(context!!, R.color.red_color)
        pLinePaint.strokeWidth = 6F
        pLinePaint.style = Paint.Style.STROKE


        pointPaint.color = Color.WHITE
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.STROKE

        pointErrorPaint.color = Color.RED
        pointErrorPaint.strokeWidth = ERROR_STROKE_WIDTH
        pointErrorPaint.style = Paint.Style.STROKE

        statePaint.textSize = TEXT_SIZE
        statePaint.color = Color.MAGENTA

        repsPaint.textSize = TEXT_SIZE
        repsPaint.color = Color.BLUE

        incorrectRepsPaint.textSize = TEXT_SIZE
        incorrectRepsPaint.color = Color.RED

        anglePaint.textSize = ANGLE_TEXT
        anglePaint.color = Color.GREEN

        maskPaint.color = Color.WHITE
        maskPaint.textSize = MASK_TEXT
    }

    @SuppressLint("WrongConstant", "SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkResult ->
            canvas.drawText(resources.getString(R.string.reps) + respCountTotal, TEXT_X, TEXT_TOTAL_RESP_Y, repsPaint)
            canvas.drawText(resources.getString(R.string.incorrect_reps) + respCountIncorrect, TEXT_X, TEXT_INCORRECT_RESP_Y, incorrectRepsPaint)
            for (landmark in poseLandmarkResult.landmarks()) {
                if (!DeadLiftUtility.isUserFullyVisible(landmark, windowWidth, windowHeight)) {
                    DeadLiftUtility.Log("UserVisibility", "Fully Not Visible windowWidth=$windowWidth,windowHeight=$windowHeight")
                    return
                } else {
                    DeadLiftUtility.Log("UserVisibility", "Fully Visible windowWidth=$windowWidth,windowHeight=$windowHeight")
                }

            }
           if(!isPlaying)
             return

            /*Check if Timer is Completed*/
            if (!isTimerCompleted)
                return

            if (yoFShoulder > yOfToe)
                return

            for (landmark in poseLandmarkResult.landmarks()) {
                for (normalizedLandmark in landmark) {
                    canvas.drawCircle(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,3f,
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

            val kneeAngle = (leftKneeAngle * 10).roundToInt() / 10
            val hipAngle = (leftHipAngle * 10).roundToInt() / 10
            val heelAngle = (heelAngle * 10).roundToInt() / 10
            val shoulderAngle=(shoulderAngle*10).roundToInt()/10

            /* FRONT FACE CASE */
            if (kneeAngle > 0 && hipAngle > 0 && this.shoulderAngle >0 && userFaceType == ConstantsDeadLift.FRONT_FACE) {
                when (DeadLiftUtility.getDLState(hipAngle, shoulderAngle, ConstantsDeadLift.FRONT_FACE)) {
                    STATE_UP -> {
                        drawAngles(canvas,resources.getString(R.string.state_up),kneeAngle,hipAngle,shoulderAngle)
                          if (statesSet.contains(STATE_DOWN) && statesSet.contains(STATE_MOVING)) {
                            respCountTotal++
                            canvas.drawText(resources.getString(R.string.reps) + respCountTotal, TEXT_X, TEXT_TOTAL_RESP_Y, repsPaint)
                            canvas.drawText(resources.getString(R.string.incorrect_reps) + respCountIncorrect, TEXT_X, TEXT_INCORRECT_RESP_Y, incorrectRepsPaint)
                            statesSet.clear()
                        }else{

                        }

                    }
                    STATE_MOVING -> {
                        drawAngles(canvas,resources.getString(R.string.state_moving),kneeAngle,hipAngle,shoulderAngle)
                        statesSet.add(STATE_MOVING)
                    }
                    STATE_DOWN -> {
                        drawAngles(canvas,resources.getString(R.string.state_down),kneeAngle,hipAngle,shoulderAngle)
                         statesSet.add(STATE_DOWN)
                    }

                    STATE_UN_DECIDED -> {
                        canvas.drawText(resources.getString(R.string.empty_string), TEXT_X, TEXT_STATE_Y, statePaint)
                    }

                    else -> {}

                }
                /* LEFT/RIGHT FACE CASE */
            } else if (kneeAngle > 0 && hipAngle > 0 && (userFaceType == ConstantsDeadLift.LEFT_FACE || userFaceType == ConstantsDeadLift.RIGHT_FACE)) {
                when (DeadLiftUtility.getDLState(hipAngle, shoulderAngle,ConstantsDeadLift.LEFT_FACE)) {
                    STATE_UP -> {
                        drawAngles(canvas,resources.getString(R.string.state_up),kneeAngle,hipAngle,shoulderAngle)
                        if (statesSet.contains(STATE_DOWN) && statesSet.contains(STATE_MOVING)) {
                            minKneeAngle = Float.MAX_VALUE
                            respCountTotal++
                            if (statesSet.contains(SQUAT_INCORRECT)) {
                                respCountIncorrect++
                            }
                            canvas.drawText(
                                resources.getString(R.string.reps) + respCountTotal,
                                TEXT_X,
                                TEXT_TOTAL_RESP_Y,
                                repsPaint
                            )
                            canvas.drawText(
                                resources.getString(R.string.incorrect_reps) + respCountIncorrect,
                                TEXT_X,
                                TEXT_INCORRECT_RESP_Y,
                                incorrectRepsPaint
                            )
                            statesSet.clear()
                        } else {

                        }

                    }
                    STATE_MOVING -> {
                        drawAngles(canvas,resources.getString(R.string.state_moving),kneeAngle,hipAngle,shoulderAngle)
                        statesSet.add(STATE_MOVING)
                    }
                    STATE_DOWN -> {
                        drawAngles(canvas,resources.getString(R.string.state_down),kneeAngle,hipAngle,shoulderAngle)
                        statesSet.add(STATE_DOWN)
                    }

                    STATE_UN_DECIDED -> {
                        canvas.drawText(resources.getString(R.string.empty_string), TEXT_X, TEXT_STATE_Y, statePaint)
                    }

                    else -> {}
                }


            } else {
                /*Draw USER's STATE(UP/DOWN/MOVING */
                drawEmptyText(canvas)
            }

        }


    }


    fun setResults(
        poseLandmarkResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE,
        cameraFacing: Int,
        userFaceType: Int,
        leftKneeAngle: Float,
        leftHipAngle: Float,
        heelAngle: Float,
        shoulderAngle:Float,
        heelX: Float,
        yHeel: Float,
        xHip: Float,
        yHip: Float,
        xKnee: Float,
        yKnee: Float,
        toeX: Float,
        isTimerCompleted: Boolean,
        xofLeftKnee: Float,
        xofRightKnee: Float,
        xofLeftToe: Float,
        xofRightToe: Float,
        yOfToe: Float,
        yoFShoulder: Float,
        yForNose: Float,
        shoulderx: Float,
        shulderY: Float,
        ankleX: Float,
        ankleY: Float,
        windowWidth: Int,
        windowHeight: Int,
        isPlaying: Boolean,
        leftToeX: Float,
        rightToeX: Float,
        xofLeftHip: Float,
        xofRightHip: Float,
        yofLeftShoulder: Float,
        yofRightShoulder: Float,
        yOfLeftHeel: Float,
        yOfRightHeel: Float,
        yOfLeftToe: Float,
        yOfRightToe: Float

    ) {
        results = poseLandmarkResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        this.leftKneeAngle = leftKneeAngle
        this.leftHipAngle = leftHipAngle
        this.heelAngle = heelAngle
        this.shoulderAngle= shoulderAngle
        this.userFaceType = userFaceType
        this.xHip = xHip
        this.yHip = yHip
        this.xKnee = xKnee
        this.yKnee = yKnee
        this.toeX = toeX
        this.xHeel = heelX
        this.yHeel = yHeel
        this.xofLeftKnee = xofLeftKnee
        this.xofRightKnee = xofRightKnee
        this.xofLeftToe = xofLeftToe
        this.xofRightToe = xofRightToe
        this.isTimerCompleted = isTimerCompleted
        this.yOfToe = yOfToe
        this.yForNose = yForNose
        this.yoFShoulder = yoFShoulder
        this.shoulderx = shoulderx
        this.shulderY = shulderY
        this.ankleX = ankleX
        this.ankleY = ankleY
        this.windowHeight = windowHeight
        this.windowWidth = windowWidth
        this.isPlaying=isPlaying
        this.leftToeX=leftToeX
        this.rightToeX=rightToeX
        this.xofLeftHip=xofLeftHip
        this.xofRightHip=xofRightHip
        this.yofLeftShoulder=yofLeftShoulder
        this.yofRightShoulder=yofRightShoulder
        this.yOfLeftHeel=yOfLeftHeel
        this. yOfRightHeel=yOfRightHeel
        this.yOfLeftToe=yOfLeftToe
        this.yOfRightToe=yOfRightToe
        if (this.cameraFacing != cameraFacing) {
            respCountTotal = 0
            respCountIncorrect = 0
        }
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
        invalidate()
        var scale_factor = DeadLiftUtility.getPixelToInchScalingFactor(windowHeight,userHeight,yOfLeftHeel,yOfRightHeel,yForNose)
        FormFixConstants.PIXEL_TO_CM_SCALE=scale_factor
    }


    private fun drawMessageOnScreen(
        xPos: Float,
        yPos: Float,
        message: String,
        messageType: Int,
        canvas: Canvas, needtoSpeak: Boolean,needtoDraw:Boolean
    ) {
        if (needtoSpeak) {
            saveErrorMessages(message, messageType,true)
            speakOut(message)
        }

        if(!needtoDraw)
            return
        val textHeight = abs(maskPaint.fontMetrics.top)
        val padding = 10
        var bgRect = RectF(
            xPos,
            yPos,
            (xPos + maskPaint.measureText(message) + padding * 2),
            (yPos + textHeight + padding * 2)
        )
        canvas.drawRoundRect(bgRect, 10F, 10F, statePaint)
        // Draw text inside the background rectangle
        val xPos = (bgRect.left + (bgRect.width() - maskPaint.measureText(message)) / 2)
        val yPos = (bgRect.centerY() - (maskPaint.descent() + maskPaint.ascent()) / 2)
        canvas.drawText(message, xPos, yPos, maskPaint)
    }

    private fun saveErrorMessages(message: String, messageType: Int,needToIncreaseCount:Boolean) {
        if (errorMessageList.isEmpty() || checkIfMessageTypeNotExist(messageType)) {
            var errorMsgModel = ErrorMessage();
            errorMsgModel.messageType = messageType;
            errorMsgModel.message = message
            errorMsgModel.repsCount = respCountTotal
            errorMsgModel.count = 1
            errorMessageList.add(errorMsgModel)
        } else {
            if(!needToIncreaseCount)
                return
            for (i in 0 until errorMessageList.size) {
                if (errorMessageList[i].messageType == messageType) {
                    if (errorMessageList[i].repsCount < respCountTotal) {
                        errorMessageList[i].repsCount = respCountTotal
                        errorMessageList[i].count = errorMessageList[i].count + 1

                    }

                }

            }
        }
    }

    private fun checkIfMessageTypeNotExist(currentMessageType: Int): Boolean {
        for (msg in errorMessageList) {
            if (msg.messageType == currentMessageType)
                return false
        }
        return true
    }


    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

       /*    for(voice in tts!!.voices){
               Log.e("voice",""+voice);
               //en-US-SMTf00, locale: eng_USA_f00,
               //en-US-default, locale: eng_USA_default
           }*/
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                DeadLiftUtility.Log("TTS", "The Language not supported!")
            } else {
                DeadLiftUtility.Log("TTS", "The Language is supported!")
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun speakOut(textToSpeak: String) {
        if (!tts!!.isSpeaking)
            tts!!.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "")

    }

    @SuppressLint("SuspiciousIndentation")
    private fun stopSpeaking() {
        if (tts!!.isSpeaking)
            tts!!.stop()

    }
    private fun drawAngles(canvas:Canvas, state:String, kneeAngle:Int, hipAngle:Int, shoulderAngle:Int){
        canvas.drawText(state, TEXT_X, TEXT_STATE_Y, statePaint)
        canvas.drawText(kneeAngle.toString(), xKnee * imageWidth * scaleFactor, yKnee * imageHeight * scaleFactor, anglePaint)
        canvas.drawText(hipAngle.toString(), xHip * imageWidth * scaleFactor, yHip * imageHeight * scaleFactor, anglePaint)
        canvas.drawText(shoulderAngle.toString(), shoulderx * imageWidth * scaleFactor, shulderY * imageHeight * scaleFactor, anglePaint)
    }

    fun drawEmptyText(canvas:Canvas){

        canvas.drawText(resources.getString(R.string.empty_string), TEXT_X, TEXT_STATE_Y, statePaint)
        /*Draw Total Reps Count*/
        canvas.drawText(resources.getString(R.string.empty_string), TEXT_X, TEXT_TOTAL_RESP_Y, repsPaint)
        /*Draw Incorrect Reps Count*/
        canvas.drawText(resources.getString(R.string.empty_string), TEXT_X, TEXT_INCORRECT_RESP_Y, incorrectRepsPaint)
        /*Draw Knee Angle*/
        canvas.drawText(resources.getString(R.string.empty_string), xKnee * imageWidth * scaleFactor, yKnee * imageHeight * scaleFactor, anglePaint)
        /*Draw Heel Angle*/
        canvas.drawText(heelAngle.toString(), xHeel * imageWidth * scaleFactor, yHeel * imageHeight * scaleFactor, anglePaint)
        canvas.drawCircle((xKnee - ConstantsDeadLift.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor, yKnee * imageHeight * scaleFactor, 0f, pointErrorPaint)

        /*Draw Hip Angle*/
        canvas.drawText(resources.getString(R.string.empty_string), xHip * imageWidth * scaleFactor, yHip * imageHeight * scaleFactor, anglePaint)


        canvas.drawText("", shoulderx * imageWidth * scaleFactor, shulderY * imageHeight * scaleFactor, anglePaint)
    }

}