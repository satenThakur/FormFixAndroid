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
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.fittracker.model.ErrorMessage
import com.fittracker.utilits.Constants
import com.fittracker.utilits.Constants.ANGLE_TEXT
import com.fittracker.utilits.Constants.ERROR_STROKE_WIDTH
import com.fittracker.utilits.Constants.HEEL_MAX_ANGLE
import com.fittracker.utilits.Constants.HEEL_MIN_ANGLE
import com.fittracker.utilits.Constants.CIRCLE_RADIUS
import com.fittracker.utilits.Constants.HEEL_MESSAGE
import com.fittracker.utilits.Constants.HIP_MESSAGE
import com.fittracker.utilits.Constants.KNEE_MESSAGE
import com.fittracker.utilits.Constants.KNEE_TOE_X_MESSAGE
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


class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs), TextToSpeech.OnInitListener {
    private var runningMode: RunningMode = RunningMode.LIVE_STREAM
    private var scaleBitmap: Bitmap? = null
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
    private var userFaceType = 0
    private var respCountTotal = 0
    private var respCountIncorrect = 0
    private var statesSet = mutableSetOf<Int>()
    private var xKnee = -100F
    private var yKnee = -100F
    private var xHip = -100F
    private var yHip = -100F
    private var toeX = -100F
    private var xHeel = -100F
    private var yHeel = -100F
    private var xofLeftKnee=-100F
    private var xofRightKnee=-100F
    private var xofLeftToe=-100F
    private var xofRightToe=-100F
    private var yOfToe=-100f
    private var yoFShoulder=-100f;
    private var yForNose=-100f
    private var shulderY=-100f
    private var shoulderx=-100f
    private var ankleX=-100f
    private var ankleY=-100f
    private var cameraFacing = -1
    private var maskBitmap: Bitmap? = null
    private var minKneeAngle = Float.MAX_VALUE
    private var isTimerCompleted = false
    private var isHeelCorrect: Boolean = true
    private var isFrontFaceErrorMessage: Boolean = false
    private var tts: TextToSpeech? = null
    private var kneeMessageTimeStemp: Long = 0
    private var hipMessageTimeStamp: Long = 0
    private var heelMessageTimeStamp: Long = 0
    private var heelxToeXMessageTimeStamp: Long = 0
    private var kneeNewAngle=0f;
    private var hipNewAngle=0f;
    public var errorMessageList = ArrayList<ErrorMessage>()
   var userSelectedFace=11;
    private var windowHeight=0
    private var windowWidth=0
    init {
        initPaints()
        errorMessageList.clear()
        tts = TextToSpeech(context, this)
    }

    private fun initPaints() {
        linePaint.color = ContextCompat.getColor(context!!, R.color.colorPrimary)
        linePaint.strokeWidth = LANDMARK_LINE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pLinePaint.color = ContextCompat.getColor(context!!, R.color.black_color)
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
        Log.e("timeDiff=", "draw")
        /*scaleBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, pointPaint)
        }*/
        results?.let { poseLandmarkResult ->

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
            for (landmark in poseLandmarkResult.landmarks()) {
                if(!Utility.isUserFullyVisible(landmark,windowWidth,windowHeight)){
                    Log.e("UserVisibility","Fully Not Visible windowWidth="+windowWidth+",windowHeight="+windowHeight)
                   return
                }else{
                    Log.e("UserVisibility","Fully Visible windowWidth="+windowWidth+",windowHeight="+windowHeight)
                }
              /*  for (normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }*/
               /* PoseLandmarker.POSE_LANDMARKS.forEach {
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
                }*/


            }


             var phip1x=imageWidth * scaleFactor*xHip-LINE_LENGTH
             var phip1y=imageHeight * scaleFactor*yHip
             var phip2x=imageWidth * scaleFactor*xHip
             var phip2y=imageHeight * scaleFactor*yHip

            var pShoulderx=imageWidth * scaleFactor*shoulderx
            var pShouldery=imageHeight * scaleFactor*shulderY


            var pKnee1x=imageWidth * scaleFactor*xKnee+LINE_LENGTH
            var pKnee1y=imageHeight * scaleFactor*yKnee;
            var pKnee2x=imageWidth * scaleFactor*xKnee
            var pKnee2y=imageHeight * scaleFactor*yKnee

            var pAnkleX=imageWidth * scaleFactor*ankleX
            var pAnkleY=imageHeight * scaleFactor*ankleY

            //canvas.drawLine(phip1x,phip1y,phip2x,phip2y,pLinePaint)
           // canvas.drawLine(pKnee1x,pKnee1y,pKnee2x,pKnee2y,pLinePaint)
            //canvas.drawLine(p2x,p2y, p3x,p3y,pLinePaint)
            if(pAnkleX>0 && pAnkleY>0 && pShoulderx>0 && pShouldery>0) {
                kneeNewAngle =
                    Utility.calculateAngles(pAnkleX, pAnkleY, pKnee2x, pKnee2y, pKnee1x, pKnee1y)
                hipNewAngle =
                    Utility.calculateAngles(phip1x, phip1y, phip2x, phip2y, pShoulderx, pShouldery)
            }

            //todo uncomment for enable masking
            if (poseLandmarkResult.segmentationMasks().isPresent) {
                var segmentation = poseLandmarkResult.segmentationMasks()
                var mpImage = segmentation.get()[0]
                //onMaskResult(mpImage)
                //onMaskResultScaled(mpImage)
            }
         /*Check if Timer is Completed*/
            if (!isTimerCompleted)
                return
            Log.e("NOSEY","yForNose="+yForNose)
            if(yoFShoulder>yOfToe)
           return

         /*   if(!(userSelectedFace==userFaceType || ((userFaceType== Constants.LEFT_FACE || userFaceType == Constants.RIGHT_FACE) && userSelectedFace!= Constants.FRONT_FACE) ))
                return
*/
            val knee =(leftKneeAngle * 10).roundToInt() / 10
            val hip = (leftHipAngle * 10).roundToInt() / 10
            val heel = (heelAngle * 10).roundToInt() / 10


            /* FRONT FACE CASE */
            if (xHeel > 0 && yHeel > 0 && userFaceType == Constants.FRONT_FACE) {

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

                when (Utility.getState(leftKneeAngle, leftHipAngle, Constants.FRONT_FACE)) {
                    STATE_MOVING -> {
                        canvas.drawText(
                            resources.getString(R.string.state_moving),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                        statesSet.add(STATE_MOVING)
                        if(leftHipAngle<100 && leftHipAngle>90 && !statesSet.contains(STATE_DOWN)){
                            speakOut(resources.getString(R.string.squat_down_further))
                        }
                    }

                    STATE_UP -> {
                        Log.e("STATE_UP","for FRONT")
                        canvas.drawText(
                            resources.getString(R.string.state_up),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                        if (statesSet.contains(STATE_DOWN) && statesSet.contains(STATE_MOVING)) {
                            respCountTotal++
                            if (!isHeelCorrect || isFrontFaceErrorMessage) {
                                respCountIncorrect++
                            }
                            if(respCountIncorrect==0){
                                errorMessageList.clear()
                            }
                            isHeelCorrect = true
                            isFrontFaceErrorMessage=false
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

                    STATE_DOWN -> {
                        //stopSpeaking()
                        canvas.drawText(
                            resources.getString(R.string.state_down),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                        statesSet.add(STATE_DOWN)
                        Log.e("diff_toesX",""+abs((xofRightToe-xofLeftToe)* imageWidth * scaleFactor))
                        Log.e("diff_kneesX",""+abs((xofRightKnee-xofLeftKnee)* imageWidth * scaleFactor))
                        Log.e("diff____",""+abs(((xofRightKnee-xofLeftKnee)-(xofRightToe-xofLeftToe))* imageWidth * scaleFactor))
                        if (heel > 0) {
                            if (heel<HEEL_MIN_ANGLE) {
                                Log.e("ErrorMessage","3 heelAngle="+heel)
                                isHeelCorrect = false
                                var needToSpeak=false;
                                if (heelMessageTimeStamp == 0.toLong() || System.currentTimeMillis() - heelMessageTimeStamp > 1000) {
                                    heelMessageTimeStamp = System.currentTimeMillis();
                                    needToSpeak=true
                                }
                                isFrontFaceErrorMessage=true
                                // Draw background rectangle
                                    drawMessageOnScreen(
                                        xHeel * imageWidth * scaleFactor,
                                        yHeel * imageHeight * scaleFactor,
                                        context.resources.getString(R.string.externally_rotate_feet),
                                        HEEL_MESSAGE,
                                        canvas,needToSpeak
                                    )


                            }
                            if (HEEL_MIN_ANGLE <= heel || heel <= HEEL_MAX_ANGLE) {
                                isHeelCorrect = true
                            var diff=abs(xofLeftKnee-xofRightKnee)-abs(xofLeftToe-xofRightToe)
                              diff=  diff*imageHeight * scaleFactor

                                Log.e("ErrorMessage","4 kneex toex diff="+diff)
                                if(diff<-TOE_KNEE_X_DIFFS_MIN_THRESHOLD){
                                    isFrontFaceErrorMessage=true;
                                    Log.e("ErrorMessage","4 kneex toex diff=_true"+diff)

                                    var needToSpeak=false;
                                    if (heelxToeXMessageTimeStamp == 0.toLong() || System.currentTimeMillis() - heelxToeXMessageTimeStamp > 1000) {
                                        heelxToeXMessageTimeStamp = System.currentTimeMillis();
                                        needToSpeak=true
                                    }

                                        drawMessageOnScreen(
                                        xofRightKnee * imageWidth * scaleFactor,
                                        yKnee * imageHeight * scaleFactor,
                                        context.resources.getString(R.string.knees_going_inwards),
                                        KNEE_TOE_X_MESSAGE,
                                        canvas,needToSpeak
                                    )
                                }
                            }else{
                                isHeelCorrect=false
                            }
                            canvas.drawText(
                                heel.toString(),
                                xHeel * imageWidth * scaleFactor,
                                yHeel * imageHeight * scaleFactor,
                                anglePaint
                            )
                        } else {
                            canvas.drawText(
                                resources.getString(R.string.empty_string),
                                xHeel * imageWidth * scaleFactor,
                                yHeel * imageHeight * scaleFactor,
                                anglePaint
                            )
                        }
                    }

                    STATE_UN_DECIDED -> {
                        canvas.drawText(
                            resources.getString(R.string.empty_string),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                    }

                    else -> {}

                }
                /* LEFT/RIGHT FACE CASE */
            } else if (xHip > 0 && yHip > 0 && xKnee > 0 && yKnee > 0 && (userFaceType == Constants.LEFT_FACE || userFaceType == Constants.RIGHT_FACE)) {



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
                when (Utility.getState(leftKneeAngle, leftHipAngle, Constants.LEFT_FACE)) {
                    STATE_MOVING -> {
                        canvas.drawText(
                            knee.toString(),
                            xKnee * imageWidth * scaleFactor,
                            yKnee * imageHeight * scaleFactor,
                            anglePaint
                        )


                        canvas.drawText(
                            hip.toString(),
                            xHip * imageWidth * scaleFactor,
                            yHip * imageHeight * scaleFactor,
                            anglePaint
                        )
                        canvas.drawText(
                            resources.getString(R.string.state_moving),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                        statesSet.add(STATE_MOVING)
                        if(leftHipAngle<100 && leftHipAngle>90 && !statesSet.contains(STATE_DOWN)){
                            speakOut(resources.getString(R.string.squat_down_further))
                        }
                        if(leftHipAngle<85 && leftKneeAngle>110 && !statesSet.contains(STATE_DOWN)){
                            speakOut(resources.getString(R.string.bend_at_the_knees))
                        }
                    }

                    STATE_UP -> {
                        canvas.drawText(
                            knee.toString(),
                            xKnee * imageWidth * scaleFactor,
                            yKnee * imageHeight * scaleFactor,
                            anglePaint
                        )


                        canvas.drawText(
                            hip.toString(),
                            xHip * imageWidth * scaleFactor,
                            yHip * imageHeight * scaleFactor,
                            anglePaint
                        )
                        canvas.drawText(
                            resources.getString(R.string.state_up),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                        if (statesSet.contains(STATE_DOWN) && statesSet.contains(STATE_MOVING)) {
                            minKneeAngle = Float.MAX_VALUE
                            respCountTotal++
                            if (statesSet.contains(SQUAT_INCORRECT)) {
                                respCountIncorrect++
                            }
                            if(respCountIncorrect==0){
                                errorMessageList.clear()
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
                        }
                        if(leftHipAngle<85 && leftKneeAngle>110 && !statesSet.contains(STATE_DOWN)){
                            speakOut(resources.getString(R.string.bend_at_the_knees))
                        }
                    }

                    STATE_DOWN -> {
                      //  stopSpeaking()
                        var kneeNangle=(kneeNewAngle * 10).roundToInt() / 10
                        var hipNangle=(hipNewAngle * 10).roundToInt() / 10
                        canvas.drawText(
                            kneeNangle.toString(),
                            xKnee * imageWidth * scaleFactor,
                            yKnee * imageHeight * scaleFactor,
                            anglePaint
                        )


                        canvas.drawText(
                            hipNangle.toString(),
                            xHip * imageWidth * scaleFactor,
                            yHip * imageHeight * scaleFactor,
                            anglePaint
                        )
                        canvas.drawText(
                            resources.getString(R.string.state_down),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                        statesSet.add(STATE_DOWN)
                        if (Utility.isKneeCrossesToes(
                                toeX,
                                xKnee,
                                userFaceType
                            ) ) {
                            Log.e("ErrorMessage","3 kneeCrossingToes="+ abs(toeX - xKnee))
                            canvas.drawCircle(
                                (xKnee - Constants.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
                                yKnee * imageHeight * scaleFactor,
                                CIRCLE_RADIUS,
                                pointErrorPaint
                            )

                            // Draw background rectangle

                            var needToSpeak=false;
                            if (kneeMessageTimeStemp == 0.toLong() || System.currentTimeMillis() - kneeMessageTimeStemp > 1000) {
                                kneeMessageTimeStemp = System.currentTimeMillis();
                                needToSpeak=true
                            }
                            if(needToSpeak){
                                statesSet.add(SQUAT_INCORRECT)
                            }
                                drawMessageOnScreen(
                                    (xKnee - Constants.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
                                    yKnee * imageHeight * scaleFactor,
                                    context.resources.getString(R.string.knee_crossing_toes),
                                    KNEE_MESSAGE,
                                    canvas,needToSpeak
                                )



                        } else {
                            canvas.drawCircle(0.0F, 0.0F, 0F, pointErrorPaint)
                        }
                        if (Utility.kneeHipAnglesDiff(
                                kneeNewAngle,
                                hipNewAngle
                            ) ){
                            Log.e("ErrorMessage","4 tuckHips="+ Math.abs(leftKneeAngle-leftHipAngle)+" kneeAngle="+leftKneeAngle+" hipAngle="+leftHipAngle)
                            canvas.drawCircle(
                                (xHip + Constants.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
                                yHip * imageHeight * scaleFactor,
                                CIRCLE_RADIUS,
                                pointErrorPaint
                            )

                            // Draw background rectangle
                            var needToSpeak=false;
                            if (hipMessageTimeStamp == 0.toLong() || System.currentTimeMillis() - hipMessageTimeStamp > 1000) {
                                hipMessageTimeStamp = System.currentTimeMillis();
                                needToSpeak=true
                            }
                            if(needToSpeak){
                                statesSet.add(SQUAT_INCORRECT)
                            }
                                drawMessageOnScreen(
                                    (xHip + Constants.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
                                    yHip * imageHeight * scaleFactor,
                                    context.resources.getString(R.string.tuck_hips),
                                    HIP_MESSAGE,
                                    canvas,needToSpeak
                                )


                        } else {
                            canvas.drawCircle(0.0F, 0.0F, 0F, pointErrorPaint)
                        }
                    }

                    STATE_UN_DECIDED -> {
                        canvas.drawText(
                            resources.getString(R.string.empty_string),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                    }
                }

              if (leftKneeAngle < minKneeAngle && leftKneeAngle < 90 && leftKneeAngle > 0) {
                    minKneeAngle = leftKneeAngle
                 /*  val state = Utility.getSquatPosition(
                        leftKneeAngle,
                        leftHipAngle,
                        toeX,
                        xKnee,
                        userFaceType
                    ) */
                    //statesSet.add(state)
                } else {

                }

            } else {
                /*Draw USER's STATE(UP/DOWN/MOVING */
                canvas.drawText(
                    resources.getString(R.string.empty_string),
                    TEXT_X,
                    TEXT_STATE_Y,
                    statePaint
                )
                /*Draw Total Reps Count*/
                canvas.drawText(
                    resources.getString(R.string.empty_string),
                    TEXT_X,
                    TEXT_TOTAL_RESP_Y,
                    repsPaint
                )
                /*Draw Incorrect Reps Count*/
                canvas.drawText(
                    resources.getString(R.string.empty_string),
                    TEXT_X,
                    TEXT_INCORRECT_RESP_Y,
                    incorrectRepsPaint
                )
                /*Draw Heel Angle*/
                canvas.drawText(
                    heel.toString(),
                    xHeel * imageWidth * scaleFactor,
                    yHeel * imageHeight * scaleFactor,
                    anglePaint
                )
                canvas.drawCircle(
                    (xKnee - Constants.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
                    yKnee * imageHeight * scaleFactor,
                    0f,
                    pointErrorPaint
                )
                /*Draw Knee Angle*/
                canvas.drawText(
                    resources.getString(R.string.empty_string),
                    xKnee * imageWidth * scaleFactor,
                    yKnee * imageHeight * scaleFactor,
                    anglePaint
                )
                /*Draw Hip Angle*/
                canvas.drawText(
                    resources.getString(R.string.empty_string),
                    xHip * imageWidth * scaleFactor,
                    yHip * imageHeight * scaleFactor,
                    anglePaint
                )

            }

        }


    }


    fun setResults(
        poseLandmarkResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE,
        cameraFacing: Int,
        leftKneeAngle: Float,
        leftHipAngle: Float,
        heelAngle: Float,
        heelX: Float,
        yHeel: Float,
        userFaceType: Int,
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
        userSelectedFace: Int,
        yOfToe: Float,
        yoFShoulder: Float,
        yForNose: Float,
        shoulderx: Float,
        shulderY: Float,
        ankleX: Float,
        ankleY: Float,
        windowWidth: Int,
        windowHeight: Int

    ) {
        results = poseLandmarkResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        this.leftKneeAngle = leftKneeAngle
        this.leftHipAngle = leftHipAngle
        this.heelAngle = heelAngle
        this.userFaceType = userFaceType
        this.xHip = xHip
        this.yHip = yHip
        this.xKnee = xKnee
        this.yKnee = yKnee
        this.toeX = toeX
        this.xHeel = heelX
        this.yHeel = yHeel
        this.xofLeftKnee=xofLeftKnee
        this.xofRightKnee=xofRightKnee
        this.xofLeftToe=xofLeftToe
        this.xofRightToe=xofRightToe
        this.isTimerCompleted = isTimerCompleted
        this.userSelectedFace=userSelectedFace
        this.yOfToe=yOfToe
        this.yForNose=yForNose
        this.yoFShoulder=yoFShoulder
        this.shoulderx=shoulderx
        this.shulderY=shulderY
        this.ankleX=ankleX
        this.ankleY=ankleY
        this.windowHeight=windowHeight
        this.windowWidth=windowWidth
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
    }


    private fun onMaskResultScaled(mpImage: MPImage) {
        val scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / mpImage.width, height * 1f / mpImage.height)
            }

            RunningMode.LIVE_STREAM -> {
                max(width * 1f / mpImage.width, height * 1f / mpImage.height)
            }
        }
        val scaleWidth = (mpImage.width * scaleFactor + scaleFactor).toInt()
        val scaleHeight = (mpImage.height * scaleFactor + scaleFactor).toInt()
        val bitmap = Bitmap.createBitmap(mpImage.width, mpImage.height, Bitmap.Config.ARGB_8888)
        val byteBuffer = ByteBufferExtractor.extract(mpImage)
        bitmap.copyPixelsFromBuffer(byteBuffer)
        scaleBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, false);
        Log.e("segment", " h=" + mpImage.height + " w=" + mpImage.width)
        Log.e("segment", " scaleHeight=" + scaleHeight + " scaleWidth=" + scaleWidth)
        invalidate()
    }

    private fun onMaskResult(
        mpImage: MPImage
    ) {

        scaleBitmap = Bitmap.createBitmap(mpImage.width, mpImage.height, Bitmap.Config.ARGB_8888)
        val byteBuffer = ByteBufferExtractor.extract(mpImage)
        scaleBitmap?.copyPixelsFromBuffer(byteBuffer)
        invalidate()
    }

    fun flipBitmap(src: Bitmap): Bitmap? {
        val matrix = Matrix()
        matrix.preScale(-1.0f, 1.0f)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    fun clear() {
        results = null
        pointPaint.reset()
        pointErrorPaint.reset()
        linePaint.reset()
        statePaint.reset()
        repsPaint.reset()
        incorrectRepsPaint.reset()
        anglePaint.reset()
        maskPaint.reset()
        maskBitmap = null
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        invalidate()

    }

    fun Int.toAlphaColor(): Int {
        return Color.argb(
            OverlayView.ALPHA_COLOR,
            Color.red(this),
            Color.green(this),
            Color.blue(this)
        )
    }

    private fun drawMessageOnScreen(
        xPos: Float,
        yPos: Float,
        message: String,
        messageType: Int,
        canvas: Canvas,needtoSpeak:Boolean
    ) {
        if(needtoSpeak) {
            saveErrorMessages(message, messageType)
            speakOut(message)
        }
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

    private fun saveErrorMessages(message: String, messageType: Int) {
        if (errorMessageList.isEmpty() || checkIfMessageTypeNotExist(messageType)) {
            var errorMsgModel = ErrorMessage();
            errorMsgModel.messageType = messageType;
            errorMsgModel.message = message
            errorMsgModel.repsCount = respCountTotal
            errorMsgModel.count = 1
            errorMessageList.add(errorMsgModel)
        } else {
            for (i in 0..errorMessageList.size-1) {
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
            for (mesg in errorMessageList) {
                if (mesg.messageType == currentMessageType)
                    return false
            }
            return true
        }
        companion object {
            const val ALPHA_COLOR = 128
            private const val BOUNDING_RECT_TEXT_PADDING = 8
        }

        override fun onInit(status: Int) {
            if (status == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language not supported!")
                } else {
                    Log.e("TTS", "The Language is supported!")
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
    }