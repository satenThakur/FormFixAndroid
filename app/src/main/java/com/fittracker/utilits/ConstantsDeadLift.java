package com.fittracker.utilits;

import android.os.Environment;

public interface ConstantsDeadLift {
    long SPEAKERWAITTIMEFORSAMEMESSAGE=2500;
    int LINE_LENGTH=300;
    int TUCK_HIPS =1;
    int KNEES_CROSSING_TOES =2;
    int BEND_AT_THE_KNEES=3;
    int EXTERNALLY_ROTATE_FEET=4;
    int KNEES_GOING_INWARDS=5;

    int HIPS_NOT_CENTERED=6;
    int SHOULDER_NOT_BALANCED=7;
    int HEELS_NOT_BALANCED=8;

    float CIRCLE_RADIUS = 5.0F;

    int LEFT_FACE = 1;
    int FRONT_FACE = 2;
    int RIGHT_FACE = 3;
    float LANDMARK_STROKE_WIDTH = 18F;


    float ERROR_STROKE_WIDTH = 22F;
    float LANDMARK_LINE_WIDTH = 3F;
    float TEXT_SIZE = 45F;
    float ANGLE_TEXT = 56F;
    float MASK_TEXT = 33F;
    int STATE_UP = 1;
    int STATE_MOVING = 2;
    int STATE_DOWN = 3;
    int STATE_UN_DECIDED = 100;

    int SQUAT_INCORRECT = 5;

    /*Paint Text X Y Coordinates*/
    float TEXT_X = 40f;
    float TEXT_STATE_Y = 80f;
    float TEXT_TOTAL_RESP_Y = 150f;

    float TEXT_INCORRECT_RESP_Y = 225;

    /* Error messages Constants */
    float HEEL_MIN_ANGLE =29; // earlier it was 33 to 70 // todo correcting angles 40 to 33;
    float HEEL_MAX_ANGLE =66;

    float KNEE_TOE_THRESHOLD = 0.070F;//0.082F;//0.072F;



    float TOE_KNEE_X_DIFFS_MIN_THRESHOLD =33;//32;//39//42;//55;
}
