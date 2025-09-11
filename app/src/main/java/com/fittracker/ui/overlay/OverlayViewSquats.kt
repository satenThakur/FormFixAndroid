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
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.fittracker.R
import com.fittracker.model.ErrorMessage
import com.fittracker.utilits.ConstantsSquats
import com.fittracker.utilits.ConstantsSquats.ANGLE_TEXT
import com.fittracker.utilits.ConstantsSquats.BEND_AT_THE_KNEES
import com.fittracker.utilits.ConstantsSquats.ERROR_STROKE_WIDTH
import com.fittracker.utilits.ConstantsSquats.HEEL_MAX_ANGLE
import com.fittracker.utilits.ConstantsSquats.HEEL_MIN_ANGLE
import com.fittracker.utilits.ConstantsSquats.CIRCLE_RADIUS
import com.fittracker.utilits.ConstantsSquats.EXTERNALLY_ROTATE_FEET
import com.fittracker.utilits.ConstantsSquats.TUCK_HIPS
import com.fittracker.utilits.ConstantsSquats.KNEES_CROSSING_TOES
import com.fittracker.utilits.ConstantsSquats.KNEES_GOING_INWARDS
import com.fittracker.utilits.ConstantsSquats.LANDMARK_LINE_WIDTH
import com.fittracker.utilits.ConstantsSquats.LANDMARK_STROKE_WIDTH
import com.fittracker.utilits.ConstantsSquats.LINE_LENGTH
import com.fittracker.utilits.ConstantsSquats.MASK_TEXT
import com.fittracker.utilits.ConstantsSquats.SPEAKERWAITTIMEFORSAMEMESSAGE
import com.fittracker.utilits.ConstantsSquats.SQUAT_INCORRECT
import com.fittracker.utilits.FormFixConstants.STATE_DOWN
import com.fittracker.utilits.FormFixConstants.STATE_MOVING
import com.fittracker.utilits.FormFixConstants.STATE_UN_DECIDED
import com.fittracker.utilits.FormFixConstants.STATE_UP
import com.fittracker.utilits.ConstantsSquats.TEXT_INCORRECT_RESP_Y
import com.fittracker.utilits.ConstantsSquats.TEXT_SIZE
import com.fittracker.utilits.ConstantsSquats.TEXT_STATE_Y
import com.fittracker.utilits.ConstantsSquats.TEXT_TOTAL_RESP_Y
import com.fittracker.utilits.ConstantsSquats.TEXT_X
import com.fittracker.utilits.ConstantsSquats.TOE_KNEE_X_DIFFS_MIN_THRESHOLD
import com.fittracker.utilits.FormFixConstants
import com.fittracker.utilits.FormFixSharedPreferences
import com.fittracker.utilits.FormFixUtility
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class OverlayViewSquats(context: Context?, attrs: AttributeSet?) :
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
    private var maskBitmap: Bitmap? = null
    private var minKneeAngle = Float.MAX_VALUE
    private var isTimerCompleted = false
    private var isHeelCorrect: Boolean = true
    private var isFrontFaceErrorMessage: Boolean = false
    private var tts: TextToSpeech? = null
    private var kneesCrossingToesTimeStamp: Long = 0
    private var tuckHipsTimeStamp: Long = 0
    private var externallyRotateFeetTimeStamp: Long = 0
    private var hipsnotincentreTimeStamp: Long = 0
    private var shouldernotbalancedTimeStamp: Long = 0
    private var heelsNotbalancedTimeStamp: Long = 0
    private var kneesGoingInwardsTimeStamp: Long = 0
    private var bendAtTheKneesTimeStamp: Long = 0
    private var kneeNewAngle = 0f
    private var hipNewAngle = 0f
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
                if (!FormFixUtility.isUserFullyVisible(landmark, windowWidth, windowHeight)) {
                    FormFixUtility.Log(
                        "UserVisibility",
                        "Fully Not Visible windowWidth=$windowWidth,windowHeight=$windowHeight"
                    )
                    return
                } else {
                    FormFixUtility.Log(
                        "UserVisibility",
                        "Fully Visible windowWidth=$windowWidth,windowHeight=$windowHeight"
                    )
                }

            }
           if(!isPlaying)
             return


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
                    FormFixUtility.calculateAngles(pAnkleX, pAnkleY, pKnee2x, pKnee2y, pKnee1x, pKnee1y)
                hipNewAngle =
                    FormFixUtility.calculateAngles(phip1x, phip1y, phip2x, phip2y, pShoulderx, pShouldery)
            }


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
            Log.e("heelAngle","heelAngle="+heelAngle+" hipAngle="+hipAngle+" kneeAngle="+kneeAngle)
            /* FRONT FACE CASE */
            if (kneeAngle > 0 && hipAngle > 0 && userFaceType == ConstantsSquats.FRONT_FACE) {
                when (FormFixUtility.getSquatState(kneeAngle, hipAngle, ConstantsSquats.FRONT_FACE)) {
               /*     GlobalScope.launch (Dispatchers.IO) {
                        val mediaData = TransCriptionData(exerciseType=exerciseType, uri=""+mUri,filename=filename,
                            date=Utility.getCurrentDate(), time=Utility.getCurrentTime())
                        FormfitApplication.database.transcriptionDao().insertTranscriptionData(mediaData)
                    }*/
                    STATE_UP -> {
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
                           /* if (respCountIncorrect == 0) {
                                errorMessageList.clear()
                            }*/
                            isHeelCorrect = true
                            isFrontFaceErrorMessage = false
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
                        canvas.drawText(
                            resources.getString(R.string.state_moving),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                        statesSet.add(STATE_MOVING)
                        if(FormFixUtility.isHipsNotInCentre(leftToeX,rightToeX,xofLeftHip,xofRightHip)){
                            var diff = abs(xofLeftKnee - xofRightKnee) - abs(xofLeftToe - xofRightToe)
                            diff = diff * imageHeight * scaleFactor
                            FormFixUtility.Log("diff","kneesAndToesDiff="+diff)
                            if (!(diff < -TOE_KNEE_X_DIFFS_MIN_THRESHOLD)) {
                                isFrontFaceErrorMessage = true
                                var needToSpeak = false
                                if (hipsnotincentreTimeStamp == 0.toLong() || System.currentTimeMillis() - hipsnotincentreTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                    hipsnotincentreTimeStamp = System.currentTimeMillis();
                                    needToSpeak = true
                                }
                                drawMessageOnScreen(
                                    xHeel * imageWidth * scaleFactor,
                                    yHeel * imageHeight * scaleFactor,
                                    context.resources.getString(R.string.hips_not_in_centre),
                                    ConstantsSquats.HIPS_NOT_CENTERED,
                                    canvas, needToSpeak, true
                                )
                            }

                        }
                        if(!FormFixUtility.isShoulderBalanced(yofLeftShoulder,yofRightShoulder,userHeight,yOfLeftHeel,yOfRightHeel,yForNose)){
                            isFrontFaceErrorMessage = true
                            var needToSpeak = false
                            if (shouldernotbalancedTimeStamp == 0.toLong() || System.currentTimeMillis() - shouldernotbalancedTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                shouldernotbalancedTimeStamp = System.currentTimeMillis();
                                needToSpeak = true
                            }
                            drawMessageOnScreen(
                                shoulderx * imageWidth * scaleFactor,
                                yofLeftShoulder * imageHeight * scaleFactor,
                                context.resources.getString(R.string.shoulders_not_balanced),
                                ConstantsSquats.SHOULDER_NOT_BALANCED,
                                canvas, needToSpeak, true
                            )
                        }

                    }
                    STATE_DOWN -> {
                        canvas.drawText(
                            resources.getString(R.string.state_down),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                        statesSet.add(STATE_DOWN)
                        if (heelAngle > 0) {
                            if (heelAngle < HEEL_MIN_ANGLE) {
                                isHeelCorrect = false
                                var needToSpeak = false;
                                if (externallyRotateFeetTimeStamp == 0.toLong() || System.currentTimeMillis() - externallyRotateFeetTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                    externallyRotateFeetTimeStamp = System.currentTimeMillis();
                                    needToSpeak = true
                                }
                                isFrontFaceErrorMessage = true
                                // Draw background rectangle
                                drawMessageOnScreen(
                                    xHeel * imageWidth * scaleFactor,
                                    yHeel * imageHeight * scaleFactor,
                                    context.resources.getString(R.string.externally_rotate_feet),
                                    EXTERNALLY_ROTATE_FEET,
                                    canvas, needToSpeak,true
                                )


                            }


                            if (HEEL_MIN_ANGLE <= heelAngle || heelAngle <= HEEL_MAX_ANGLE) {
                                isHeelCorrect = true
                                var diff = abs(xofLeftKnee - xofRightKnee) - abs(xofLeftToe - xofRightToe)
                                diff = diff * imageHeight * scaleFactor
                                FormFixUtility.Log("diff","kneesAndToesDiff="+diff)
                                if (diff < -TOE_KNEE_X_DIFFS_MIN_THRESHOLD) {
                                    isFrontFaceErrorMessage = true
                                    var needToSpeak = false;
                                    if (kneesGoingInwardsTimeStamp == 0.toLong() || System.currentTimeMillis() - kneesGoingInwardsTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                        kneesGoingInwardsTimeStamp = System.currentTimeMillis();
                                        needToSpeak = true
                                    }

                                    drawMessageOnScreen(
                                        xofRightKnee * imageWidth * scaleFactor,
                                        yKnee * imageHeight * scaleFactor,
                                        context.resources.getString(R.string.knees_going_inwards),
                                        KNEES_GOING_INWARDS,
                                        canvas, needToSpeak,true
                                    )
                                }
                            } else {
                                isHeelCorrect = false
                            }

                            if(FormFixUtility.isHipsNotInCentre(leftToeX,rightToeX,xofLeftHip,xofRightHip)){
                                var diff = abs(xofLeftKnee - xofRightKnee) - abs(xofLeftToe - xofRightToe)
                                diff = diff * imageHeight * scaleFactor
                                FormFixUtility.Log("diff","kneesAndToesDiff="+diff)
                                if (!(diff < -TOE_KNEE_X_DIFFS_MIN_THRESHOLD)) {
                                    isFrontFaceErrorMessage = true
                                    var needToSpeak = false
                                    if (hipsnotincentreTimeStamp == 0.toLong() || System.currentTimeMillis() - hipsnotincentreTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                        hipsnotincentreTimeStamp = System.currentTimeMillis();
                                        needToSpeak = true
                                    }
                                    drawMessageOnScreen(
                                        xHeel * imageWidth * scaleFactor,
                                        yHeel * imageHeight * scaleFactor,
                                        context.resources.getString(R.string.hips_not_in_centre),
                                        ConstantsSquats.HIPS_NOT_CENTERED,
                                        canvas, needToSpeak, true
                                    )
                                }
                            }

                            if(!FormFixUtility.isShoulderBalanced(yofLeftShoulder,yofRightShoulder,userHeight,yOfLeftHeel,yOfRightHeel,yForNose)){
                                isFrontFaceErrorMessage = true
                                var needToSpeak = false
                                if (shouldernotbalancedTimeStamp == 0.toLong() || System.currentTimeMillis() - shouldernotbalancedTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                    shouldernotbalancedTimeStamp = System.currentTimeMillis();
                                    needToSpeak = true
                                }
                                drawMessageOnScreen(
                                    shoulderx * imageWidth * scaleFactor,
                                    yofLeftShoulder * imageHeight * scaleFactor,
                                    context.resources.getString(R.string.shoulders_not_balanced),
                                    ConstantsSquats.SHOULDER_NOT_BALANCED,
                                    canvas, needToSpeak, true
                                )
                            }

                            canvas.drawText(
                                heelAngle.toString(),
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
            } else if (kneeAngle > 0 && hipAngle > 0 && (userFaceType == ConstantsSquats.LEFT_FACE || userFaceType == ConstantsSquats.RIGHT_FACE)) {
                when (FormFixUtility.getSquatState(kneeAngle, hipAngle, ConstantsSquats.LEFT_FACE)) {
                    STATE_UP -> {
                        //Right/Left Face STATE_UP
                        canvas.drawText(
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
                            resources.getString(R.string.state_up),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )
                        if ((hipAngle <=90) && kneeAngle >= 160 && !statesSet.contains(STATE_DOWN)) {
                            var needToSpeak = false;
                            if (bendAtTheKneesTimeStamp == 0.toLong() || System.currentTimeMillis() - bendAtTheKneesTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                bendAtTheKneesTimeStamp = System.currentTimeMillis();
                                needToSpeak = true
                            }

                            drawMessageOnScreen(
                                (xKnee - ConstantsSquats.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
                                yKnee * imageHeight * scaleFactor,
                                context.resources.getString(R.string.bend_at_the_knees),
                                BEND_AT_THE_KNEES,
                                canvas, needToSpeak,false
                            )
                        }

                        if (statesSet.contains(STATE_DOWN) && statesSet.contains(STATE_MOVING)) {
                            minKneeAngle = Float.MAX_VALUE
                            respCountTotal++
                            if (statesSet.contains(SQUAT_INCORRECT)) {
                                respCountIncorrect++
                            }
                          /*  if (respCountIncorrect == 0) {
                                errorMessageList.clear()
                            }*/
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

                    }
                    STATE_MOVING -> {
                        //Right/Left Face STATE_MOVING
                        canvas.drawText(
                            kneeAngle.toString(),
                            xKnee * imageWidth * scaleFactor,
                            yKnee * imageHeight * scaleFactor,
                            anglePaint
                        )

                 /*       if(!Utility.isHeelBalanced(yOfLeftHeel,yOfRightHeel,yOfLeftToe,yOfRightToe,userFaceType)){
                            isFrontFaceErrorMessage = true
                            var needToSpeak = false
                            if (heelsNotbalancedTimeStamp == 0.toLong() || System.currentTimeMillis() - heelsNotbalancedTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                heelsNotbalancedTimeStamp = System.currentTimeMillis();
                                needToSpeak = true
                            }
                            drawMessageOnScreen(
                                xHeel * imageWidth * scaleFactor,
                                yHeel * imageHeight * scaleFactor,
                                context.resources.getString(R.string.heels_not_balanced),
                                ConstantsSquats.HEELS_NOT_BALANCED,
                                canvas, needToSpeak, true
                            )
                        }*/
                        canvas.drawText(
                            hipAngle.toString(),
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


                    }
                    STATE_DOWN -> {
                        canvas.drawText(
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
                            resources.getString(R.string.state_down),
                            TEXT_X,
                            TEXT_STATE_Y,
                            statePaint
                        )

                        statesSet.add(STATE_DOWN)
                        /*check knee crossing toes when both angles are below 80 and stack contains MOVING_STATE and UP_STATE*/
                        if (FormFixUtility.isKneeCrossesToes(toeX, xKnee, userFaceType)) {
                            canvas.drawCircle(
                                (xKnee - ConstantsSquats.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
                                yKnee * imageHeight * scaleFactor,
                                CIRCLE_RADIUS,
                                pointErrorPaint
                            )

                            var needToSpeak = false;
                            if (kneesCrossingToesTimeStamp == 0.toLong() || System.currentTimeMillis() - kneesCrossingToesTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                kneesCrossingToesTimeStamp = System.currentTimeMillis();
                                needToSpeak = true
                            }
                            if (needToSpeak) {
                                statesSet.add(SQUAT_INCORRECT)
                            }
                           drawMessageOnScreen(
                                (xKnee - ConstantsSquats.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
                                yKnee * imageHeight * scaleFactor,
                                context.resources.getString(R.string.knee_crossing_toes),
                               KNEES_CROSSING_TOES,
                                canvas, needToSpeak,true
                            )


                        } else {
                            canvas.drawCircle(0.0F, 0.0F, 0F, pointErrorPaint)
                        }
                        if (FormFixUtility.kneeHipAnglesDiff(kneeNewAngle, hipNewAngle, toeX, xKnee)) {
                            canvas.drawCircle(
                                (xHip + ConstantsSquats.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
                                yHip * imageHeight * scaleFactor,
                                CIRCLE_RADIUS,
                                pointErrorPaint
                            )

                            // Draw background rectangle
                            var needToSpeak = false;
                            if (tuckHipsTimeStamp == 0.toLong() || System.currentTimeMillis() - tuckHipsTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                tuckHipsTimeStamp = System.currentTimeMillis();
                                needToSpeak = true
                            }
                            if (needToSpeak) {
                                statesSet.add(SQUAT_INCORRECT)
                            }
                            drawMessageOnScreen(
                                (xHip + ConstantsSquats.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
                                yHip * imageHeight * scaleFactor,
                                context.resources.getString(R.string.tuck_hips),
                                TUCK_HIPS,
                                canvas, needToSpeak,true
                            )


                        } else {
                            canvas.drawCircle(0.0F, 0.0F, 0F, pointErrorPaint)
                        }
                        if(!FormFixUtility.isHeelBalanced(yOfLeftHeel,yOfRightHeel,yOfLeftToe,yOfRightToe,userFaceType)){
                            isFrontFaceErrorMessage = true
                            var needToSpeak = false
                            if (heelsNotbalancedTimeStamp == 0.toLong() || System.currentTimeMillis() - heelsNotbalancedTimeStamp > SPEAKERWAITTIMEFORSAMEMESSAGE) {
                                heelsNotbalancedTimeStamp = System.currentTimeMillis();
                                needToSpeak = true
                            }
                            drawMessageOnScreen(
                                xHeel * imageWidth * scaleFactor,
                                yHeel * imageHeight * scaleFactor,
                                context.resources.getString(R.string.heels_not_balanced),
                                ConstantsSquats.HEELS_NOT_BALANCED,
                                canvas, needToSpeak, true
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
                }

                if (kneeAngle < minKneeAngle && kneeAngle < 90 && kneeAngle > 0) {
                    minKneeAngle = kneeAngle.toFloat()
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
                    heelAngle.toString(),
                    xHeel * imageWidth * scaleFactor,
                    yHeel * imageHeight * scaleFactor,
                    anglePaint
                )
                canvas.drawCircle(
                    (xKnee - ConstantsSquats.KNEE_TOE_THRESHOLD) * imageWidth * scaleFactor,
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
        var scale_factor = FormFixUtility.getPixelToInchScalingFactor(windowHeight,userHeight,yOfLeftHeel,yOfRightHeel,yForNose)
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
                FormFixUtility.Log("TTS", "The Language not supported!")
            } else {
                FormFixUtility.Log("TTS", "The Language is supported!")
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