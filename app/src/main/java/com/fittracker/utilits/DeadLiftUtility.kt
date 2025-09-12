package com.fittracker.utilits

import android.util.Log
import com.fittracker.landmarkModels.Point3D
import com.fittracker.utilits.FormFixConstants.STATE_DOWN
import com.fittracker.utilits.FormFixConstants.STATE_MOVING
import com.fittracker.utilits.FormFixConstants.STATE_UN_DECIDED
import com.fittracker.utilits.FormFixConstants.STATE_UP
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.abs
import kotlin.math.roundToInt


object DeadLiftUtility {
    fun getDLState(hipAngle: Int, shoulderAngle: Int, faceType: Int): Int {
        Log("Angels", "hipAngle=$hipAngle shoulderAngle=$shoulderAngle")
        if (faceType == ConstantsSquats.FRONT_FACE)
            return getFrontFaceState(hipAngle, shoulderAngle)
        else if (faceType == ConstantsSquats.LEFT_FACE || faceType == ConstantsSquats.RIGHT_FACE)
            return getSideFaceState(hipAngle, shoulderAngle)
        else
            return STATE_UN_DECIDED
    }
    private fun getFrontFaceState(hipAngle: Int, shoulderAngle: Int): Int {
        return if ((hipAngle > 130) /*&& (shoulderAngle in 1..34)*/) {
            STATE_UP
        } else if ((hipAngle in 100..129) && (shoulderAngle in 36..44)) {
            STATE_MOVING
        } else if (hipAngle < 125 && shoulderAngle > 45) {
            STATE_DOWN
        } else {
            return STATE_UN_DECIDED
        }
    }
    private fun getSideFaceState(hipAngle: Int, shoulderAngle: Int): Int {
        return if (hipAngle > 150 && shoulderAngle < 15 && shoulderAngle > 0) {
            STATE_UP
        } else if (hipAngle in 71..149 && (shoulderAngle in 16..54)) {
            STATE_MOVING
        } else if (hipAngle in 1..69 && shoulderAngle > 55) {
            STATE_DOWN
        } else {
            return STATE_UN_DECIDED
        }
    }
    fun isHipsNotInCentre(
        leftToeX: Float,
        rightToeX: Float,
        xOfLeftHip: Float,
        xOfRightHip: Float,
    ): Boolean {
        val diffInInches = getHipAnkleDiffAverage(leftToeX, rightToeX, xOfLeftHip, xOfRightHip)
        var isHipNotIncentre = false
        if (ConstantsDeadLift.HIPS_ANKLE_AVARGE_DIFF < diffInInches) {
           // Log("isHipsAlign", "hipsNotInCentre TRUE")
            isHipNotIncentre = true
        }
        return isHipNotIncentre
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
            //Log("isHipsAlign", "Hips_In_LEFT=" + (toesCentre - hipCentre))
        } else {
            //Hips in Right
            //Log("isHipsAlign", "Hips_In_RIGHT=" + (toesCentre - hipCentre))
        }
        var averageDiff = (abs(toesCentre - hipCentre) * FormFixConstants.PIXEL_TO_CM_SCALE) / 2.54

        //2.0088553
      /*  Log(
            "isHipsAlign",
            "toesCentre=$toesCentre, hipCentre=$hipCentre" + "avergaeDiff In Inches=$averageDiff"
        )*/
        return averageDiff.toInt()
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
        Log.e(tag, message)
    }

    fun getXinPixels(x: Float): Int {
        return (x * FormFixConstants.SCREEN_WIDTH).roundToInt()
    }

    fun getPixelToInchScalingFactor(
        screenHeight: Int,
        heightInCm: Int,
        heelY_left: Float,
        heelY_right: Float,
        noseY: Float
    ): Float {
        val scaling_factor: Float =
            heightInCm / calculateHeightInPixels(screenHeight, heelY_left, heelY_right, noseY)
        //Log("scaling_factor",""+scaling_factor)
        return scaling_factor
    }

    fun calculateHeightInPixels(
        screenHeight: Int,
        heelY_left: Float,
        heelY_right: Float,
        noseY: Float
    ): Float {
        var heel: Float = 0F
        if (heelY_right < heelY_left) {
            heel = heelY_right
        } else {
            heel = heelY_left
        }
        val headTop = calculate_headTopY((noseY * screenHeight).toInt())
        val heelInPixel = (heel * screenHeight).toInt()
        val userHeightInPixels = abs(headTop - heelInPixel)


        return userHeightInPixels.toFloat()
    }
    fun calculate_headTopY(noseY: Int): Int {

        val head = noseY / 0.92
        return head.roundToInt()
    }

    fun angleRodVsHips(
        lHip: Point3D,
        rHip: Point3D,
        lWrist: Point3D,
        rWrist: Point3D
    ): Double {
        fun vec(from: Point3D, to: Point3D) = Point3D(
            to.x - from.x,
            to.y - from.y,
            to.z - from.z
        )

        fun dot(a: Point3D, b: Point3D): Double =
            (a.x * b.x + a.y * b.y + a.z * b.z).toDouble()

        fun cross(a: Point3D, b: Point3D) = Point3D(
            a.y * b.z - a.z * b.y,
            a.z * b.x - a.x * b.z,
            a.x * b.y - a.y * b.x
        )

        fun norm(v: Point3D): Double =
            kotlin.math.sqrt(dot(v, v))

        fun angleBetweenDeg(a: Point3D, b: Point3D): Double {
            val c = cross(a, b)
            val d = dot(a, b)
            return Math.toDegrees(kotlin.math.atan2(norm(c), d))
        }

        val hipVec = vec(lHip, rHip)
        val rodVec = vec(lWrist, rWrist)

        if (norm(hipVec) < 1e-5 || norm(rodVec) < 1e-5) return Double.NaN

        val angle = angleBetweenDeg(hipVec, rodVec)

        // always return acute angle [0°–90°] for parallel check
        return angle.coerceAtMost(180.0 - angle)
    }


    /**
     * Returns the absolute angle (in degrees) between the hip line and rod line,
     * considering only the X-axis difference.
     *
     * Range: 0°–180°
     */
    fun angleRodVsHips_XOnly(
        lHip: Point3D,
        rHip: Point3D,
        lWrist: Point3D,
        rWrist: Point3D
    ): Double {
        // X components only
        val hipVecX = rHip.x - lHip.x
        val rodVecX = rWrist.x - lWrist.x

        // dot product (1D is just multiplication)
        val dot = (hipVecX * rodVecX).toDouble()
        val magHip = kotlin.math.abs(hipVecX.toDouble())
        val magRod = kotlin.math.abs(rodVecX.toDouble())

        if (magHip < 1e-5 || magRod < 1e-5) return Double.NaN

        // cosθ = dot / (|a| * |b|)
        val cosTheta = (dot / (magHip * magRod)).coerceIn(-1.0, 1.0)

        // angle in degrees
        return Math.toDegrees(kotlin.math.acos(cosTheta))
    }



    /**
     * Returns the acute angle (0°–90°) between the hip line (LHip–RHip)
     * and the rod line (LWrist–RWrist), considering only X & Y coordinates.
     *
     * @return angle in degrees, or NaN if vectors are invalid.
     */
    fun angleRodVsAnkle_XY(
        lAnkle: Point3D,
        rAnkle: Point3D,
        lWrist: Point3D,
        rWrist: Point3D
    ): Int {
        // Vector in XY plane only
        fun vec2(from: Point3D, to: Point3D) =
            Pair(to.x - from.x, to.y - from.y)

        fun dot(a: Pair<Float, Float>, b: Pair<Float, Float>) =
            a.first * b.first + a.second * b.second

        fun norm(a: Pair<Float, Float>) =
            kotlin.math.sqrt((a.first * a.first + a.second * a.second).toDouble())

        val hipVec = vec2(lAnkle, rAnkle)
        val rodVec = vec2(lWrist, rWrist)

        val magHip = norm(hipVec)
        val magRod = norm(rodVec)
        if (magHip < 1e-6 || magRod < 1e-6) return -1 // or Int.MIN_VALUE as error

        val cosTheta = (dot(hipVec, rodVec).toDouble() / (magHip * magRod)).coerceIn(-1.0, 1.0)
        var angle = Math.toDegrees(kotlin.math.acos(cosTheta))

        // Always return acute angle [0°–90°]
        val acute = angle.coerceAtMost(180.0 - angle)

        return acute.toInt()  // convert to Int
    }
    fun angleRodVsAnkle_X(
        lAnkle: Point3D,
        rAnkle: Point3D,
        lWrist: Point3D,
        rWrist: Point3D
    ): Int {
        // Vector in X-axis only
        fun vecX(from: Point3D, to: Point3D) =
            to.x - from.x

        val hipVec = vecX(lAnkle, rAnkle)
        val rodVec = vecX(lWrist, rWrist)

        val magHip = kotlin.math.abs(hipVec.toDouble())
        val magRod = kotlin.math.abs(rodVec.toDouble())
        if (magHip < 1e-6 || magRod < 1e-6) return -1 // invalid / zero length

        // In 1D, dot product is just multiplication
        val cosTheta = (hipVec * rodVec) / (magHip * magRod)
        val angle = Math.toDegrees(
            kotlin.math.acos(cosTheta.coerceIn(-1.0, 1.0))
        )

        // Always return acute angle [0°–90°]
        val acute = angle.coerceAtMost(180.0 - angle)

        return acute.toInt()
    }



    fun isRodParallelToAnkles(
        lAnkle: Point3D, rAnkle: Point3D,
        lWrist: Point3D, rWrist: Point3D,
        angleTolDeg: Double = 7.0   // tolerance in degrees
    ): Int {
        fun vec2(a: Point3D, b: Point3D) = Pair((b.x - a.x).toDouble(), (b.y - a.y).toDouble())
        fun dot(u: Pair<Double, Double>, v: Pair<Double, Double>) = u.first * v.first + u.second * v.second
        fun norm(u: Pair<Double, Double>) = kotlin.math.sqrt(u.first*u.first + u.second*u.second)

        val hipVec = vec2(lAnkle, rAnkle)   // ankle line (proxy for ground)
        val rodVec = vec2(lWrist, rWrist)   // wrist line (rod/bar)

        val nh = norm(hipVec)
        val nr = norm(rodVec)
        if (nh < 1e-6 || nr < 1e-6) return 0  // invalid data

        val cos = (dot(hipVec, rodVec) / (nh * nr)).coerceIn(-1.0, 1.0)
        val angle = Math.toDegrees(kotlin.math.acos(cos))
        val acute = angle.coerceAtMost(180.0 - angle)   // 0..90

        return acute.toInt()
    }

    fun isDeadliftKneePositionValid(
        leftKneeX: Float,
        rightKneeX: Float,
        leftAnkleX: Float,
        rightAnkleX: Float,
        scale: Float = 100f // default multiplier
    ): Int {
        val kneeDistX = kotlin.math.abs(leftKneeX - rightKneeX)
        val ankleDistX = kotlin.math.abs(leftAnkleX - rightAnkleX)

        if (ankleDistX == 0f) return 0 // avoid divide-by-zero

        return (((kneeDistX - ankleDistX) / ankleDistX) * scale).toInt()
    }

    fun foorKneesDistnaceDiff(
        leftFootIndex: Point3D,
        rightFootIndex: Point3D,
        leftHeel: Point3D,
        rightHeel: Point3D,
        scale: Float = 100f
    ): Int {
        val footDistX = kotlin.math.abs(leftFootIndex.x - rightFootIndex.x)
        val heelDistX = kotlin.math.abs(leftHeel.x - rightHeel.x)
        if (heelDistX == 0f) return 0
        return (((footDistX - heelDistX) / heelDistX) * scale).toInt()
    }

}




