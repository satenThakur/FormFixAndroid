package com.fittracker.utilits

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import com.fittracker.utilits.Constants.KNEE_HIP_DIFF_NEW_THRESHOLD
import com.fittracker.utilits.Constants.KNEE_HIP_DIFF_THRESHOLD
import com.fittracker.utilits.Constants.KNEE_TOE_THRESHOLD
import com.fittracker.utilits.Constants.KNEE_TOE_THRESHOLD_TO_IGNORE_TUCK_HIPS
import com.fittracker.utilits.Constants.STATE_DOWN
import com.fittracker.utilits.Constants.STATE_MOVING
import com.fittracker.utilits.Constants.STATE_UN_DECIDED
import com.fittracker.utilits.Constants.STATE_UP
import com.google.android.material.snackbar.Snackbar
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
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
        Log("VideoPlayer", "fileToPlay=" + fileToPlay)
        var file = File(fileToPlay)
        if (file.exists()) {
            file.delete()
        }
    }

    fun checkIfFileExist(filename: String): Boolean {
        var fileToPlay = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .toString() + "/" + Constants.FOLDER_NAME + "/" + filename + ".mp4"
        Log("VideoPlayer", "fileToPlay=" + fileToPlay)
        var file = File(fileToPlay)
        return file.exists()
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

    fun getSquatState(kneeAngle: Int, hipAngle: Int, faceType: Int): Int {
        if (faceType == 2) {
            Log("Angels", "FaceType=FRONT")
        } else {
            Log("Angels", "FaceType=LEFT/RIGHT")
        }

        Log("Angels", "KneeAngle=$kneeAngle")
        Log("Angels", "HipAngle=$hipAngle")


        return if ((/*kneeAngle > 140 ||   */hipAngle > 146) && faceType == Constants.FRONT_FACE) {
            Log("Angels", "STATE_UP FRONT_FACE")
            Constants.STATE_UP
        } else if (hipAngle < 140 && hipAngle > 90 && faceType == Constants.FRONT_FACE) {
            Log("Angels", "STATE_MOVING FRONT_FACE")
            Constants.STATE_MOVING
        } else if (hipAngle < 90 && hipAngle > 0 && faceType == Constants.FRONT_FACE) {
            Log("Angels", "STATE_DOWN FRONT_FACE")
            Constants.STATE_DOWN
        } else if (kneeAngle > 150 && (faceType == Constants.LEFT_FACE || faceType == Constants.RIGHT_FACE)) {
            Log("Angels", "STATE_UP")
            Constants.STATE_UP
        } else if (kneeAngle < 150 && kneeAngle > 90 && hipAngle < 150 && (faceType == Constants.LEFT_FACE || faceType == Constants.RIGHT_FACE)) {
            Log("Angels", "STATE_MOVING")
            Constants.STATE_MOVING
        } else if (kneeAngle < 90 && kneeAngle > 0 && (faceType == Constants.LEFT_FACE || faceType == Constants.RIGHT_FACE)) {
            Log("Angels", "STATE_DOWN")
            Constants.STATE_DOWN
        } else {
            Log("Angels", "STATE_UN_DECIDED")
            return STATE_UN_DECIDED
        }


        /*
        if(kneeAngle<=0 || hipAngle<=0)
            return STATE_UN_DECIDED
        return if (kneeAngle > 150) {
            Log("Angels", "STATE_UP")
            Constants.STATE_UP
        } else if (kneeAngle < 82 && kneeAngle > 46 && faceType==Constants.FRONT_FACE) {
            Log("Angels", "STATE_MOVING")
            Constants.STATE_MOVING
        }else if (kneeAngle < 90 && kneeAngle > 56 && hipAngle < 95 && (faceType==Constants.LEFT_FACE||faceType==Constants.RIGHT_FACE)) {
            Log("Angels", "STATE_MOVING")
            Constants.STATE_MOVING
        } else  *//* if(kneeAngle<56 && kneeAngle>0){*//* {
            Log("Angels", "STATE_DOWN")
            Constants.STATE_DOWN
        }*/
        /* }else {
            return STATE_UN_DECIDED;
        }*/
    }


    fun getPushUpState(elbowAngle: Int, shouldrAngle: Int, faceType: Int):Int {
        if (faceType == 2) {
            Log("Angels", "FaceType=FRONT")
        } else {
            Log("Angels", "FaceType=LEFT/RIGHT")
        }

        Log("Angels", "elbowAngle=$elbowAngle")
        Log("Angels", "shouldrAngle=$shouldrAngle")

        if (elbowAngle > 110) {
            Log("Angels", "STATE_UP")
            return  Constants.STATE_UP
        } else if (elbowAngle in 85..110) {
            Log("Angels", "STATE_MOVING")
            return  Constants.STATE_MOVING
        } else if (elbowAngle in 1..85) {
            Log("Angels", "STATE_DOWN")
            return  Constants.STATE_DOWN
        }

        Log("Angels", "STATE_UN_DECIDED")
        return STATE_UN_DECIDED
    }

    fun getSquatPosition(
        kneeAngle: Float,
        hipAngle: Float,
        toeX: Float,
        knneX: Float,
        userFaceType: Int
    ): Int {
        return if (kneeAngle < 90 && kneeAngle > 0 && !isKneeAndHipAnglesDiffCrossedThredHold(
                kneeAngle,
                hipAngle
            ) && !isKneeCrossesToes(
                toeX,
                knneX,
                userFaceType
            )/*toeX - knneX >= 0*/) {
            // Log("Angles", "SQUAT_CORRECT")
            Constants.SQUAT_CORRECT
        } else {
            // Log("Angles", "SQUAT_INCORRECT")
            Constants.SQUAT_INCORRECT
        }
    }

    fun isKneeCrossesToes(toeX: Float, knneX: Float, userFaceType: Int): Boolean {
        var kneeToeXDiff = abs(toeX - knneX)
        Log("Angle", "KneeX and ToeX diff=$kneeToeXDiff threshold is=$KNEE_TOE_THRESHOLD")
        return (kneeToeXDiff > KNEE_TOE_THRESHOLD)
    }


    fun kneeHipAnglesDiff(kneeAngle: Float, hipAngle: Float, toeX:Float,
                          knneX:Float,): Boolean {
        if(abs(toeX - knneX)>KNEE_TOE_THRESHOLD_TO_IGNORE_TUCK_HIPS)
            return false
        var diff= abs(kneeAngle-hipAngle)
        Log("parallelAngels","kneeAngle="+kneeAngle+" hipAngle="+hipAngle+"diff="+diff)
        if (diff>KNEE_HIP_DIFF_NEW_THRESHOLD) {
            Log("parallelAngels","kneeAngle="+kneeAngle+" hipAngle="+hipAngle+"diff="+diff+">"+KNEE_HIP_DIFF_NEW_THRESHOLD)
            return true
        } else {
            return false
        }
    }

    private fun isKneeAndHipAnglesDiffCrossedThredHold(kneeAngle: Float, hipAngle: Float): Boolean {
        if (Math.abs(kneeAngle - hipAngle) > KNEE_HIP_DIFF_THRESHOLD) {
            Log("Angle", "DIFF_Kneeangle_HIPangle=" + Math.abs(kneeAngle - hipAngle))
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
            Log("Angle Radian is Nan", "" + 200.0f)
            200.0f
        } else {
            Math.toDegrees(radianAngle).toFloat()
        }
    }

    fun isTablet(context: Context): Boolean {
        val xlarge = context.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK === 4
        val large = context.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK === Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge || large
    }


    fun isUserFullyVisible(
        poseLandmarks: List<NormalizedLandmark>,
        screenWidth: Int,
        screenHeight: Int
    ): Boolean {
        val visibleThreshold = 0.95 // Adjust as needed
        for (landmark in poseLandmarks) {
            val landmarkX: Float = landmark.x() * screenWidth
            val landmarkY: Float = landmark.y() * screenHeight

            // Check if the landmark is within the visible portion of the screen
            if (landmarkX < 0 || landmarkX > screenWidth || landmarkY < 0 || landmarkY > screenHeight) {
                return false
            }
        }
        return true
    }

    fun Log(tag: String, message: String) {
       //Log.e(tag, message)
    }

    fun getFaceType(leftShoulder:Float,rightShoulder:Float,nose:Float) :Int{
        val leftShoulderDistance = leftShoulder - nose
        val rightShoulderDistance = rightShoulder - nose
        val leftNoseDistance = nose - leftShoulder
        val rightNoseDistance = nose - rightShoulder
        if (leftShoulderDistance > 0 && rightShoulderDistance > 0)
            return Constants.LEFT_FACE
        else if (leftNoseDistance > 0 && rightNoseDistance > 0)
            return Constants.RIGHT_FACE
        else
            return  Constants.FRONT_FACE
    }
    fun isHipsNotInCentre(leftToeX:Float, rightToeX:Float, xOfLeftHip:Float, xOfRightHip:Float, scalFactor:Float):Boolean
    {
        var avergaeDiff=getHipAnkleDiffAverage(leftToeX,rightToeX,xOfLeftHip,xOfRightHip,scalFactor)

       var isHipNotIncentre=false
        if(Constants.HIPS_ANKLE_AVARGE_DIFF<avergaeDiff) {
            isHipNotIncentre =true
        }
            return isHipNotIncentre
    }

    fun getHipAnkleDiffAverage(lefToeX:Float, rightToeX:Float, leftHipX:Float, rightHipX:Float, scalFactor:Float):Int{
        var toesCentre=abs((rightToeX+lefToeX)/2)
        var hipCentre=abs((rightHipX+leftHipX)/2)
        if(hipCentre<toesCentre){
            Log("isHipsAlign", "Hips_In_LEFT="+(toesCentre-hipCentre))
        }else{
            //Hips in Right
            Log("isHipsAlign", "Hips_In_RIGHT="+(toesCentre-hipCentre))
        }

        var avergaeDiff=abs(toesCentre-hipCentre)*100
        //2.0088553
        Log("isHipsAlign", "ankleAverage=$toesCentre, hipsAverage$hipCentre")
        Log("isHipsAlign","avergaeDiff="+ avergaeDiff)
        return avergaeDiff.toInt()
    }
    fun startPushUpTracking(wristY:Float,toeY:Float,hipAngle:Float,kneeAngle:Float) :Boolean{
        var diff=abs(wristY - toeY)
        if(diff<180 && hipAngle>=155 && kneeAngle>=155){
            return true
        } else {
            return false
        }
    }


    fun isPushUpPoseCorrect(hipAngle:Float,  kneeAngle:Float,state:Int, shoulderElbowDiff:Float, wristToeDiff:Float) :Boolean{
        //Log.e("isPushUpPoseCorrect","hipAngle="+hipAngle)
        //Log.e("isPushUpPoseCorrect","kneeAngle="+kneeAngle)
        if(wristToeDiff<110 && hipAngle>=154 && kneeAngle>=160 && state==1 && shoulderElbowDiff>56){
            return false
        } else if(wristToeDiff < 110 && hipAngle >= 154 && kneeAngle >= 160){
            return true
        }
        return false
    }

    fun getShoulderElbowXDiff(shoulderX:Float,elbowX:Float):Float{
        var shoulderElbowDiff=abs(shoulderX - elbowX)*1000
        Log("isPushUpPoseCorrect","shoulderElbowDiff="+shoulderElbowDiff)
        /*22  to 55 elbow piche rehni chahiye shoulder se*/
        return shoulderElbowDiff
    }
    fun getWristToeYDiff(wristY:Float,toeY:Float):Float{
        var wristToeDiff= abs(wristY- toeY)*1000
        /*74 to 102   wrist and toe on same plane */
        Log("isPushUpPoseCorrect","wristToeDiff="+wristToeDiff)
        return wristToeDiff
    }

    fun getPushUpState(angle:Float):Int{
        if(angle>157)
            return STATE_UP
        else if(angle<89)
            return STATE_DOWN
        else
            return STATE_MOVING

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
}



