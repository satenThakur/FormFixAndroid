package com.fittracker.utilits

import android.graphics.Color
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import com.fittracker.utilits.Constants.KNEE_HIP_DIFF_NEW_THRESHOLD
import com.fittracker.utilits.Constants.KNEE_HIP_DIFF_THRESHOLD
import com.fittracker.utilits.Constants.KNEE_TOE_THRESHOLD
import com.fittracker.utilits.Constants.STATE_UN_DECIDED
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

object Utility {
    fun deleteMediaFile(filename: String) {
        var fileToPlay = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .toString() + "/" + Constants.FOLDER_NAME + "/" + filename + ".mp4"
        Log.e("VideoPlayer", "fileToPlay=" + fileToPlay)
        var file = File(fileToPlay)
        if (file.exists()) {
            file.delete()
        }
    }

    fun checkIfFileExist(filename: String): Boolean {
        var fileToPlay = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .toString() + "/" + Constants.FOLDER_NAME + "/" + filename + ".mp4"
        Log.e("VideoPlayer", "fileToPlay=" + fileToPlay)
        var file = File(fileToPlay)
        if (file.exists()) {
            return true
        } else {
            return false
        }
    }

    fun generateFileName(): String {
        val formatter = SimpleDateFormat(Constants.FILENAME_DATE_FORMATTER, Locale.getDefault())
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate).replace(" ", "")
    }

    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate)
    }

    fun getCurrentTime(): String {
        val formatter = SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault())
        val curTime = Date(System.currentTimeMillis())
        return formatter.format(curTime)
    }

    fun onSNACK(view: View, msg: String) {
        //Snackbar(view)
        val snackbar = Snackbar.make(
            view, msg,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackbar.setActionTextColor(Color.BLUE)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(Color.RED)
        val textView =
            snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        textView.textSize = 14f
        snackbar.show()
    }

    fun getState(kneeAngle: Float, hipAngle: Float,faceType:Int): Int {
        Log.e("Angels", "KneeAngle=$kneeAngle")
        Log.e("Angels", "HipAngle=$hipAngle")


        return  if ((/*kneeAngle > 140 ||   */hipAngle>150) && faceType==Constants.FRONT_FACE) {
            Log.e("Angels", "STATE_UP FRONT_FACE")
            Constants.STATE_UP
        } else if (hipAngle < 140 && hipAngle > 90 && faceType==Constants.FRONT_FACE) {
            Log.e("Angels", "STATE_MOVING FRONT_FACE")
            Constants.STATE_MOVING
        }else   if(hipAngle<90 && hipAngle>0 && faceType==Constants.FRONT_FACE) {
            Log.e("Angels", "STATE_DOWN FRONT_FACE")
            Constants.STATE_DOWN
        } else if (kneeAngle > 150 && (faceType==Constants.LEFT_FACE||faceType==Constants.RIGHT_FACE)) {
            Log.e("Angels", "STATE_UP")
            Constants.STATE_UP
        }else if (kneeAngle < 150 && kneeAngle > 90 && hipAngle < 150 && (faceType==Constants.LEFT_FACE||faceType==Constants.RIGHT_FACE)) {
            Log.e("Angels", "STATE_MOVING")
            Constants.STATE_MOVING
        } else   if(kneeAngle<90 && kneeAngle>0 && (faceType==Constants.LEFT_FACE||faceType==Constants.RIGHT_FACE)) {
            Log.e("Angels", "STATE_DOWN")
            Constants.STATE_DOWN
        } else{
            return STATE_UN_DECIDED
        }



      /*
        if(kneeAngle<=0 || hipAngle<=0)
            return STATE_UN_DECIDED
        return if (kneeAngle > 150) {
            Log.e("Angels", "STATE_UP")
            Constants.STATE_UP
        } else if (kneeAngle < 82 && kneeAngle > 46 && faceType==Constants.FRONT_FACE) {
            Log.e("Angels", "STATE_MOVING")
            Constants.STATE_MOVING
        }else if (kneeAngle < 90 && kneeAngle > 56 && hipAngle < 95 && (faceType==Constants.LEFT_FACE||faceType==Constants.RIGHT_FACE)) {
            Log.e("Angels", "STATE_MOVING")
            Constants.STATE_MOVING
        } else  *//* if(kneeAngle<56 && kneeAngle>0){*//* {
            Log.e("Angels", "STATE_DOWN")
            Constants.STATE_DOWN
        }*/
        /* }else {
            return STATE_UN_DECIDED;
        }*/
    }

    fun getSquatPosition(
        kneeAngle: Float,
        hipAngle: Float,
        toeX: Float,
        knneX: Float,
        userFaceType: Int
    ): Int {
        return if (kneeAngle < 90 && kneeAngle > 0 && !isKneeAndHipAnglesDiffCrossedThredHold(kneeAngle,hipAngle) && !isKneeCrossesToes(
                toeX,
                knneX,
                userFaceType
            )/*toeX - knneX >= 0*/) {
            // Log.e("Angles", "SQUAT_CORRECT")
            Constants.SQUAT_CORRECT
        } else {
            // Log.e("Angles", "SQUAT_INCORRECT")
            Constants.SQUAT_INCORRECT
        }
    }

    fun isKneeCrossesToes(toeX: Float, knneX: Float, userFaceType: Int): Boolean {
        Log.e("Angle", "DIFF_Knee_toeX=" + abs(toeX - knneX))
        return (abs(toeX - knneX) > KNEE_TOE_THRESHOLD)
    }
    fun isKneeAndHipAnglesDiffCrossedThredHold(kneeAngle: Float, hipAngle: Float,): Boolean {
        if (Math.abs(kneeAngle-hipAngle)>KNEE_HIP_DIFF_THRESHOLD) {
            Log.e("Angle","DIFF_Kneeangle_HIPangle="+Math.abs(kneeAngle-hipAngle))
            return true
        } else {
            return false
        }
    }

    fun kneeHipAnglesDiff(kneeAngle: Float, hipAngle: Float,): Boolean {
        Log.e("NEWAGNGELS_DIFF","="+Math.abs(kneeAngle-hipAngle))
        Log.e("NEWAGNGELS_DIFF","kneeAngle="+kneeAngle+" hipAngle="+hipAngle)
        if (Math.abs(kneeAngle-hipAngle)>KNEE_HIP_DIFF_NEW_THRESHOLD) {
            Log.e("Angle","DIFF_Kneeangle_HIPangle="+Math.abs(kneeAngle-hipAngle))
            return true
        } else {
            return false
        }
    }

    /*Screen Recorder Functions */

    fun angleBetweenPoints(
        point1: DoubleArray,
        point2: DoubleArray,
        point3: DoubleArray
    ): Double {
        val vector1 = doubleArrayOf(
            point1[0] - point2[0],
            point1[1] - point2[1],
            point1[2] - point2[2]
        )
        val vector2 = doubleArrayOf(
            point3[0] - point2[0],
            point3[1] - point2[1],
            point3[2] - point2[2]
        )
        val dotProduct =
            vector1[0] * vector2[0] + vector1[1] * vector2[1] + vector1[2] * vector2[2]
        val magnitudeVector1 = Math.sqrt(
            vector1[0] * vector1[0] + vector1[1] * vector1[1] + vector1[2] * vector1[2]
        )
        val magnitudeVector2 = Math.sqrt(
            vector2[0] * vector2[0] + vector2[1] * vector2[1] + vector2[2] * vector2[2]
        )
        val angle =
            Math.acos(dotProduct / (magnitudeVector1 * magnitudeVector2))
        return Math.toDegrees(angle)
    }

    fun getFace(userFaceType: Int): String {
        var usersface = "UnKnown";
        if (userFaceType == Constants.RIGHT_FACE) {
            usersface = "Right"
        } else if (userFaceType == Constants.LEFT_FACE) {
            usersface = "Left"
        } else if (userFaceType == Constants.FRONT_FACE) {
            usersface = "Front"
        }
        return usersface
    }



     fun calculateAngles(
        p0x: Float,
        p0y: Float,
        cx: Float,
        cy: Float,
        p1x: Float,
        p1y: Float
    ): Float {
        val p0c = sqrt(
            (cx - p0x).toDouble().pow(2.0) +
                    (cy - p0y).toDouble().pow(2.0)
        )

        val p1c = sqrt(
            (cx - p1x).toDouble().pow(2.0) +
                    (cy.toDouble() - p1y).pow(2.0)
        )

        val p0p1 = sqrt(
            (p1x - p0x).toDouble().pow(2.0) +
                    (p1y - p0y).toDouble().pow(2.0)
        )

        val radianAngle = acos((p1c * p1c + p0c * p0c - p0p1 * p0p1) / (2 * p1c * p0c))

        return if (radianAngle.isNaN()) {
            Log.e("Angle Radian is Nan", "" + 200.0f)
            200.0f
        } else {
            Math.toDegrees(radianAngle).toFloat()
        }
    }
}