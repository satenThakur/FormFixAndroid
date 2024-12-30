package com.fittracker.utilits

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.fittracker.R
import com.fittracker.model.User
import com.fittracker.utilits.ConstantsPushUps.PUSH_CORRECT_THERES_HIP_ANGLE
import com.fittracker.utilits.ConstantsPushUps.PUSH_CORRECT_THERES_HIP_ANGLE_FOR_STATE_UP
import com.fittracker.utilits.ConstantsPushUps.PUSH_CORRECT_THERES_KNEE_ANGLE
import com.fittracker.utilits.ConstantsPushUps.PUSH_CORRECT_THERES_WRIST_TOY_Y_DIFF
import com.fittracker.utilits.ConstantsPushUps.PUSH_UP_TAG
import com.fittracker.utilits.ConstantsPushUps.STRACK_THERES_HIP_ANGLE
import com.fittracker.utilits.ConstantsPushUps.STRACK_THERES_KNEE_ANGLE
import com.fittracker.utilits.ConstantsPushUps.STRACK_THERES_WRIST_TOY_Y_DIFF
import com.fittracker.utilits.ConstantsPushUps.THRESH_SHOULDER_ELBOW_DIFF
import com.fittracker.utilits.ConstantsSquats.HEELS_TOE_DIFF_THRESHOLD
import com.fittracker.utilits.ConstantsSquats.KNEE_HIP_DIFF_NEW_THRESHOLD
import com.fittracker.utilits.ConstantsSquats.KNEE_HIP_DIFF_THRESHOLD
import com.fittracker.utilits.ConstantsSquats.KNEE_TOE_THRESHOLD
import com.fittracker.utilits.ConstantsSquats.KNEE_TOE_THRESHOLD_TO_IGNORE_TUCK_HIPS
import com.fittracker.utilits.ConstantsSquats.SHOULDERS_DIFF_THRESHOLD
import com.fittracker.utilits.ConstantsSquats.STATE_DOWN
import com.fittracker.utilits.ConstantsSquats.STATE_MOVING
import com.fittracker.utilits.ConstantsSquats.STATE_UN_DECIDED
import com.fittracker.utilits.ConstantsSquats.STATE_UP
import com.google.android.material.snackbar.Snackbar
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import java.io.File
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sqrt


object Utility {

    fun saveUser(user: User?, context: Context) {
        if (user != null) {
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.IS_USER_LOGEDIN, true
            )
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.NAME, user.name!!
            )
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.PHONE,
                user.phone!!
            )
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.EMAIL,
                user.email!!
            )
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.HEIGHT,
                user.height!!
            )
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.WEIGHT,
                user.weight!!
            )
        } else {
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.IS_USER_LOGEDIN, false
            )
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.NAME, ""
            )
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.PHONE,
                ""
            )
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.EMAIL,
                ""
            )
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.HEIGHT,
                ""
            )
            FormFixSharedPreferences.saveSharedPreferencesValue(
                context,
                FormFixConstants.WEIGHT,
                ""
            )
        }
    }

    fun hideKeyboard(context: Context, view: View) {
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun deleteMediaFile(filename: String) {
        var fileToPlay = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .toString() + "/" + ConstantsSquats.FOLDER_NAME + "/" + filename + ".mp4"
        Log("VideoPlayer", "fileToPlay=" + fileToPlay)
        var file = File(fileToPlay)
        if (file.exists()) {
            file.delete()
        }
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun checkIfFileExist(filename: String): Boolean {
        var fileToPlay = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .toString() + "/" + ConstantsSquats.FOLDER_NAME + "/" + filename + ".mp4"
        Log("VideoPlayer", "fileToPlay=" + fileToPlay)
        var file = File(fileToPlay)
        return file.exists()
    }

    fun generateFileName(): String {
        val formatter =
            SimpleDateFormat(ConstantsSquats.FILENAME_DATE_FORMATTER, Locale.getDefault())
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate).replace(" ", "")
    }

    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat(ConstantsSquats.DATE_FORMAT, Locale.getDefault())
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate)
    }

    fun getCurrentTime(): String {
        val formatter = SimpleDateFormat(ConstantsSquats.TIME_FORMAT, Locale.getDefault())
        val curTime = Date(System.currentTimeMillis())
        return formatter.format(curTime)
    }

    fun showDialog(context: Context, tirle: String, msg: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_custom)

        val title = dialog.findViewById(R.id.tvTitle) as androidx.appcompat.widget.AppCompatTextView
        title.setText(tirle)

        val message =
            dialog.findViewById(R.id.tvMessage) as androidx.appcompat.widget.AppCompatTextView
        message.setText(msg)
        val yesBtn = dialog.findViewById(R.id.btn_ok) as androidx.appcompat.widget.AppCompatButton
        yesBtn.setOnClickListener {
            dialog.dismiss()
        }


        dialog.show()
    }

    fun showErrorSnackBar(view: View, msg: String) {
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

    fun showMessageSnackBar(view: View, msg: String) {
        //Snackbar(view)
        val snackbar = Snackbar.make(
            view, msg,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackbar.setActionTextColor(Color.BLUE)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(Color.GREEN)
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


        return if ((kneeAngle > 150) && faceType == ConstantsSquats.FRONT_FACE) {
            Log("Angels", "STATE_UP FRONT_FACE")
            ConstantsSquats.STATE_UP
        } else if (kneeAngle < 150 && kneeAngle > 90 && faceType == ConstantsSquats.FRONT_FACE) {
            Log("Angels", "STATE_MOVING FRONT_FACE")
            ConstantsSquats.STATE_MOVING
        } else if (kneeAngle < 90 /*&& hipAngle > 0*/ && faceType == ConstantsSquats.FRONT_FACE) {
            Log("Angels", "STATE_DOWN FRONT_FACE")
            ConstantsSquats.STATE_DOWN
        } else if (kneeAngle > 150 && (faceType == ConstantsSquats.LEFT_FACE || faceType == ConstantsSquats.RIGHT_FACE)) {
            Log("Angels", "STATE_UP")
            ConstantsSquats.STATE_UP
        } else if (kneeAngle < 150 && kneeAngle > 90 && hipAngle < 150 && (faceType == ConstantsSquats.LEFT_FACE || faceType == ConstantsSquats.RIGHT_FACE)) {
            Log("Angels", "STATE_MOVING")
            ConstantsSquats.STATE_MOVING
        } else if (kneeAngle < 90 && kneeAngle > 0 && (faceType == ConstantsSquats.LEFT_FACE || faceType == ConstantsSquats.RIGHT_FACE)) {
            Log("Angels", "STATE_DOWN")
            ConstantsSquats.STATE_DOWN
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
            ConstantsSquats.SQUAT_CORRECT
        } else {
            // Log("Angles", "SQUAT_INCORRECT")
            ConstantsSquats.SQUAT_INCORRECT
        }
    }

    fun isKneeCrossesToes(toeX: Float, knneX: Float, userFaceType: Int): Boolean {
        var kneeToeXDiff = abs(getXinPixels(toeX) - getXinPixels(knneX))
        val diffInInch = (kneeToeXDiff*FormFixConstants.PIXEL_TO_CM_SCALE)/2.54
        Log("Angle", "KneeX and ToeX diff=$kneeToeXDiff threshold is=$KNEE_TOE_THRESHOLD")
        return (diffInInch > 2)
    }


    fun kneeHipAnglesDiff(
        kneeAngle: Float, hipAngle: Float, toeX: Float,
        knneX: Float,
    ): Boolean {
        if (abs(toeX - knneX) > KNEE_TOE_THRESHOLD_TO_IGNORE_TUCK_HIPS)
            return false
        var diff = abs(kneeAngle - hipAngle)
        Log("parallelAngels", "kneeAngle=" + kneeAngle + " hipAngle=" + hipAngle + "diff=" + diff)
        if (diff > KNEE_HIP_DIFF_NEW_THRESHOLD) {
            Log(
                "parallelAngels",
                "kneeAngle=" + kneeAngle + " hipAngle=" + hipAngle + "diff=" + diff + ">" + KNEE_HIP_DIFF_NEW_THRESHOLD
            )
            return true
        } else {
            return false
        }
    }

    private fun isKneeAndHipAnglesDiffCrossedThredHold(kneeAngle: Float, hipAngle: Float): Boolean {
        if (abs(kneeAngle - hipAngle) > KNEE_HIP_DIFF_THRESHOLD) {
            Log("Angle", "DIFF_Kneeangle_HIPangle=" + abs(kneeAngle - hipAngle))
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
            point1[1] - point2[1]/*,
            point1[2] - point2[2]*/
        )
        val vector2 = doubleArrayOf(
            point3[0] - point2[0],
            point3[1] - point2[1]/*,
            point3[2] - point2[2]*/
        )
        val dotProduct =
            vector1[0] * vector2[0] + vector1[1] * vector2[1]/* + vector1[2] * vector2[2]*/
        val magnitudeVector1 = Math.sqrt(
            vector1[0] * vector1[0] + vector1[1] * vector1[1]/* + vector1[2] * vector1[2]*/
        )
        val magnitudeVector2 = Math.sqrt(
            vector2[0] * vector2[0] + vector2[1] * vector2[1] /*+ vector2[2] * vector2[2]*/
        )
        val angle =
            Math.acos(dotProduct / (magnitudeVector1 * magnitudeVector2))
        return Math.toDegrees(angle)
    }

    fun getFace(userFaceType: Int): String {
        var usersface = "UnKnown";
        if (userFaceType == ConstantsSquats.RIGHT_FACE) {
            usersface = "Right"
        } else if (userFaceType == ConstantsSquats.LEFT_FACE) {
            usersface = "Left"
        } else if (userFaceType == ConstantsSquats.FRONT_FACE) {
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

    fun getFaceType(leftShoulder: Float, rightShoulder: Float, nose: Float): Int {
        val leftShoulderDistance = leftShoulder - nose
        val rightShoulderDistance = rightShoulder - nose
        val leftNoseDistance = nose - leftShoulder
        val rightNoseDistance = nose - rightShoulder
        if (leftShoulderDistance > 0 && rightShoulderDistance > 0)
            return ConstantsSquats.LEFT_FACE
        else if (leftNoseDistance > 0 && rightNoseDistance > 0)
            return ConstantsSquats.RIGHT_FACE
        else
            return ConstantsSquats.FRONT_FACE
    }

    fun isHipsNotInCentre(
        leftToeX: Float,
        rightToeX: Float,
        xOfLeftHip: Float,
        xOfRightHip: Float,
    ): Boolean {
        var averageDiff = getHipAnkleDiffAverage(leftToeX, rightToeX, xOfLeftHip, xOfRightHip)
        val diffInInches = (averageDiff*FormFixConstants.PIXEL_TO_CM_SCALE)/2.54
        var isHipNotIncentre = false
        if (ConstantsSquats.HIPS_ANKLE_AVARGE_DIFF < diffInInches) {
            Log("isHipsAlign", "hipsNotInCentre TRUE")
            isHipNotIncentre = true
        }
        return isHipNotIncentre
    }

    fun isShoulderBalanced(
        yOfLeftShoulder: Float,
        yOfRightShoulder: Float,
        personHeightCm: Int,
        heelY_left:Float,
        heelY_right:Float,
        noseY:Float
    ): Boolean {
        var isShoulderBalanced = true
        var shouldersYDiff = abs((yOfLeftShoulder*FormFixConstants.SCREEN_HEIGHT) - (yOfRightShoulder*FormFixConstants.SCREEN_HEIGHT))

        val scalingFactor = getPixelToInchScalingFactor(FormFixConstants.SCREEN_HEIGHT,personHeightCm,heelY_left,heelY_right,noseY)
        val deviation = (shouldersYDiff*scalingFactor)/2.54
        if (deviation.toInt()-1 >=SHOULDERS_DIFF_THRESHOLD) {
            isShoulderBalanced = false
        }

        return isShoulderBalanced
    }

    fun isHeelBalanced(
        yOfLeftHeel: Float, yOfRightHeel: Float,
        yOfLeftToe: Float, yOfRightToe: Float, FaceType: Int
    ): Boolean {
        var isHeelBalanced = true
        var heelAndToeDiff = 0f
        if (FaceType == ConstantsSquats.FRONT_FACE) {
            var leftDiff = abs(yOfLeftHeel - yOfLeftToe) * 100
            var rightDiff = abs(yOfRightHeel - yOfRightToe) * 100
            heelAndToeDiff = if (leftDiff > rightDiff)
                leftDiff
            else
                rightDiff
            Log.e("heelsToeYDiff", "FRONT_FACE rightDiff=" + rightDiff)
            Log.e("heelsToeYDiff", "FRONT_FACE leftDiff=" + leftDiff)
        } else if (FaceType == ConstantsSquats.LEFT_FACE) {
            heelAndToeDiff = abs(yOfLeftHeel - yOfLeftToe) * 100
            Log.e("heelsToeYDiff", "LEFT_FACE Diff=" + heelAndToeDiff)
        } else if (FaceType == ConstantsSquats.RIGHT_FACE) {
            heelAndToeDiff = abs(yOfRightHeel - yOfRightToe) * 100
            Log.e("heelsToeYDiff", "RIGHT_FACE Diff=" + heelAndToeDiff)
        }

        if (heelAndToeDiff > HEELS_TOE_DIFF_THRESHOLD) {
            isHeelBalanced = false
        }
        Log.e("heelsToeYDiff", "isHeelBalanced=" + isHeelBalanced)
        return isHeelBalanced
    }

    private fun getHipAnkleDiffAverage(
        lefToeX: Float,
        rightToeX: Float,
        leftHipX: Float,
        rightHipX: Float,
    ): Int {
        var toesCentre = abs((getXinPixels(rightToeX) + getXinPixels(lefToeX)) / 2)
        var hipCentre = abs((getXinPixels(rightHipX) + getXinPixels(leftHipX)) / 2)
        if (hipCentre < toesCentre) {
            Log("isHipsAlign", "Hips_In_LEFT=" + (toesCentre - hipCentre))
        } else {
            //Hips in Right
            Log("isHipsAlign", "Hips_In_RIGHT=" + (toesCentre - hipCentre))
        }

        var averageDiff = abs(toesCentre - hipCentre)
        //2.0088553
        Log("isHipsAlign", "ankleAverage=$toesCentre, hipsAverage$hipCentre")
        Log("isHipsAlign", "avergaeDiff=$averageDiff")
        return averageDiff.toInt()
    }

    fun getPushUpState(elbowAngle: Int, shoulderAngle: Int, faceType: Int): Int {
        if (faceType == 2) {
            Log(PUSH_UP_TAG, "FaceType=FRONT")
        } else {
            Log(PUSH_UP_TAG, "FaceType=LEFT/RIGHT")
        }
        Log(PUSH_UP_TAG, "elbowAngle=$elbowAngle")
        Log(PUSH_UP_TAG, "shoulderAngle=$shoulderAngle")

        if (elbowAngle > 135) {
            Log(PUSH_UP_TAG, "STATE_UP")
            return ConstantsSquats.STATE_UP
        } else if (elbowAngle < 90) {
            Log(PUSH_UP_TAG, "STATE_DOWN")
            return ConstantsSquats.STATE_DOWN
        } else {
            //else if (elbowAngle in 90..135) {
            Log(PUSH_UP_TAG, "STATE_MOVING")
            return ConstantsSquats.STATE_MOVING
        }
    }

    fun getXinPixels(x:Float): Int{
        return (x*FormFixConstants.SCREEN_WIDTH).roundToInt()
    }

    fun startPushUpTracking(
        wristY: Float,
        toeY: Float,
        hipAngle: Float,
        kneeAngle: Float
    ): Boolean {
        var diff = getWristToeYDiff(wristY, toeY)
        Utility.Log(PUSH_UP_TAG, "hipAngle=$hipAngle,kneeAngle=$kneeAngle")
        if (diff < STRACK_THERES_WRIST_TOY_Y_DIFF && hipAngle >= STRACK_THERES_HIP_ANGLE && kneeAngle >= STRACK_THERES_KNEE_ANGLE) {
            Log(PUSH_UP_TAG, "startPushUpTracking=True")
            return true
        } else {
            Log(PUSH_UP_TAG, "startPushUpTracking=False")
            return false
        }
    }


    fun isPushUpPoseCorrect(
        hipAngle: Float,
        kneeAngle: Float,
        state: Int,
        shoulderElbowDiff: Float,
        wristToeDiff: Float
    ): Boolean {
        if (state == 1) {
            if (wristToeDiff < PUSH_CORRECT_THERES_WRIST_TOY_Y_DIFF && hipAngle >= PUSH_CORRECT_THERES_HIP_ANGLE_FOR_STATE_UP && kneeAngle >= PUSH_CORRECT_THERES_KNEE_ANGLE && state == 1 && shoulderElbowDiff > THRESH_SHOULDER_ELBOW_DIFF) {
                return false
            } else if (wristToeDiff < PUSH_CORRECT_THERES_WRIST_TOY_Y_DIFF && hipAngle >= PUSH_CORRECT_THERES_HIP_ANGLE_FOR_STATE_UP && kneeAngle >= PUSH_CORRECT_THERES_KNEE_ANGLE) {
                return true
            }
        } else {
            if (wristToeDiff < PUSH_CORRECT_THERES_WRIST_TOY_Y_DIFF && hipAngle >= PUSH_CORRECT_THERES_HIP_ANGLE && kneeAngle >= PUSH_CORRECT_THERES_KNEE_ANGLE && state == 1 && shoulderElbowDiff > THRESH_SHOULDER_ELBOW_DIFF) {
                return false
            } else if (wristToeDiff < PUSH_CORRECT_THERES_WRIST_TOY_Y_DIFF && hipAngle >= PUSH_CORRECT_THERES_HIP_ANGLE && kneeAngle >= PUSH_CORRECT_THERES_KNEE_ANGLE) {
                return true
            }
        }
        return false
    }

    fun getShoulderElbowXDiff(shoulderX: Float, elbowX: Float, state: Int): Float {
        var shoulderElbowDiff = abs(shoulderX - elbowX) * 1000
        if (state == 1)
            Log.e(
                "isPushUpPoseCorrect",
                "shoulderElbowDiff=" + shoulderElbowDiff + "Threshold=" + THRESH_SHOULDER_ELBOW_DIFF + ",state=" + state
            )
        /*22  to 55 elbow piche rehni chahiye shoulder se*/
        return shoulderElbowDiff
    }

    fun getWristToeYDiff(wristY: Float, toeY: Float): Float {
        var diff = abs(wristY - toeY) * 1000
        Log(
            "isPushUpPoseCorrect",
            "wristToeYDiff=" + diff + "Threshold=" + PUSH_CORRECT_THERES_WRIST_TOY_Y_DIFF
        )
        return diff
    }

    fun getPushUpState(angle: Float): Int {
        if (angle > 157)
            return STATE_UP
        else if (angle < 89)
            return STATE_DOWN
        else
            return STATE_MOVING

    }


    fun hipShift(
        toe1_X: Float,
        toe2_X: Float,
        hip1_X: Float,
        hip2_X: Float,
        personHeightCm: Int
    ): Long {
        var t_center = ((toe1_X * personHeightCm) + (toe2_X * personHeightCm)) / 2
        var h_center = ((hip1_X * personHeightCm) + (hip2_X * personHeightCm)) / 2

        var hip_shift_in_inches = abs(t_center - h_center) / 2.54
        return (hip_shift_in_inches * 10).roundToLong() / 10
    }


    fun shoulderShift(
        shoulder1_Y: Float,
        shoulder2_Y: Float,
        personHeightCm: Int,
        screenHeightInPixel:Int,
        heelY_left:Float,
        heelY_right:Float,
        noseY:Float
    ): Long {
         var deviationInPixels = abs((shoulder1_Y * screenHeightInPixel)-(shoulder2_Y*screenHeightInPixel))
         var deviationInCm = deviationInPixels * getPixelToInchScalingFactor(screenHeightInPixel,personHeightCm,heelY_left,heelY_right,noseY)
         var deviation_inches = deviationInCm/2.54
        if (deviation_inches>=1){
            deviation_inches=deviation_inches-1
        }
        return (deviation_inches).roundToLong()
    }

    fun kneesCrossToesShift(toeX: Float, kneeX: Float,  personHeightCm: Int): Long {
        var kneeToeXDiff = abs(toeX - kneeX)*personHeightCm
        var k_shift_in_inches = kneeToeXDiff / 2.54
        return (k_shift_in_inches * 10).roundToLong() / 10
    }
    fun heelsShift(
        yOfLeftHeel: Float, yOfRightHeel: Float,
        yOfLeftToe: Float, yOfRightToe: Float, FaceType: Int,personHeightCm: Int
    ): Long {
        var heelAndToeDiff = 0f
        if (FaceType == ConstantsSquats.FRONT_FACE) {
            var leftDiff = abs(yOfLeftHeel*personHeightCm - yOfLeftToe*personHeightCm)
            var rightDiff = abs(yOfRightHeel*personHeightCm - yOfRightToe*personHeightCm)
            heelAndToeDiff = if (leftDiff > rightDiff)
                leftDiff
            else
                rightDiff
        } else if (FaceType == ConstantsSquats.LEFT_FACE) {
            heelAndToeDiff = abs(yOfLeftHeel*personHeightCm - yOfLeftToe*personHeightCm)
        } else if (FaceType == ConstantsSquats.RIGHT_FACE) {
            heelAndToeDiff = abs(yOfRightHeel*personHeightCm - yOfRightToe*personHeightCm)
        }
        var heelsShiftInCm = heelAndToeDiff / 2.54
        return (heelsShiftInCm * 10).roundToLong() / 10

    }

    fun getPixelToInchScalingFactor(screenHeight: Int, heightInCm:Int, heelY_left:Float,heelY_right:Float, noseY:Float): Float{
        val scaling_factor: Float = heightInCm/ calculateHeightInPixels(screenHeight,heelY_left,heelY_right,noseY)
        return scaling_factor
    }

    fun calculateHeightInPixels(screenHeight: Int, heelY_left:Float,heelY_right:Float, noseY:Float): Float{
        var heel : Float= 0F
        if(heelY_right<heelY_left){
            heel = heelY_right
        }else{
            heel = heelY_left
        }
        val headTop = calculate_headTopY((noseY*screenHeight).toInt())
        val heelInPixel = (heel*screenHeight).toInt()
        val userHeightInPixels = abs(headTop-heelInPixel)


        return userHeightInPixels.toFloat()
    }

    fun calculate_headTopY(noseY:Int): Int{

        val head = noseY/0.92
        return head.roundToInt()

//        // Percentage less than headTop
//
//        // Percentage less than X
//        val percentageLess = 8.0
//
//        // Convert percentage to a decimal
//
//        // Convert percentage to a decimal
//        val percentageDecimal = percentageLess / 100
//
//        // Calculate headTop
//
//        // Calculate X
//        val headTopY = (noseY / (1 - percentageDecimal)).toInt()
//        return headTopY
    }


    fun getSquatPercentage(kneeAngle:Int,hipAngle:Int,faceType:Int):Int{

        var lowerValue =0.0

        if (faceType == ConstantsSquats.FRONT_FACE){
            lowerValue = 80.0 // degree
        }else{
            lowerValue = 60.0 // degree
        }
        val percentage= calculatePercentage(kneeAngle.toDouble(), 160.0, lowerValue)
        return percentage

    }
    fun calculatePercentage(value: Double, upperBound: Double, lowerBound: Double): Int {
        return if (value >= upperBound) {
            0 // Above or equal to the upper bound
        } else if (value <= lowerBound) {
            100 // Below or equal to the lower bound
        } else {
            // Calculate percentage using linear interpolation
            val percentage:Double = ((upperBound - value) / (upperBound - lowerBound))*100
            return percentage.toInt()

        }
    }
}    /*
    def is_pushup_pose_correct(wrist_y, toe_y, hip_angle, knee_angle,state, shoulder_wrist_diff):
    diff = abs(wrist_y - toe_y)
    if diff < 180 and hip_angle >= 168 and knee_angle >= 165 and state == 1 and shoulder_wrist_diff > 40:
        return False
    elif diff < 180 and hip_angle >= 168 and knee_angle >= 165:
        return True
    else:
        return False
 */




