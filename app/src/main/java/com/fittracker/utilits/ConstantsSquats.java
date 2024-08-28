package com.fittracker.utilits;

import android.os.Environment;

public interface ConstantsSquats {
    String SQUAT_TAG="SQUAT_TAG";
    long SPLASH_DELAY=3000;
    long SPEAKERWAITTIMEFORSAMEMESSAGE=2500;
    int SELECT_TAG_1=1;
    int SELECT_TAG_0=0;
    float SHOULDERSDIFF_CONSTANT=0.1f;

    float tagRadiousTab =18f;
    float tagRadiousPhone =10f;
    float tagBorderTab =2.5f;
    float tagBorderPhone =1.5f;
    float tagTextTab =20f;
    float tagTextPhone =14f;



    int LINE_LENGTH=300;


    int TUCK_HIPS =1;
    int KNEES_CROSSING_TOES =2;
    int SQUAT_DEEPER=3;
    int BEND_AT_THE_KNEES=4;
    int EXTERNALLY_ROTATE_FEET=5;
    int KNEES_GOING_INWARDS=6;

    int HIPS_NOT_CENTERED=7;
    int TOAST_LENGTH=100;


    float CIRCLE_RADIUS = 5.0F;
    long timerLimit = 10000;
    long squatdepertimerLimit = 4000;
   int  SquatTimerLimit=5;
    long timerInterval = 1000;
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
    int SQUAT_CORRECT = 4;
    int SQUAT_INCORRECT = 5;
    String sharedPrefFile = "FORMFIX_PREFRENCES";


    /*Paint Text X Y Coordinates*/
    float TEXT_X = 40f;
    float TEXT_STATE_Y = 80f;
    float TEXT_TOTAL_RESP_Y = 150f;

    float TEXT_INCORRECT_RESP_Y = 225;
    float TEXT_HIPANKLEAVERAGE = 280f;

    /*Screen Recording Work */
    int SCREEN_RECORD_REQUEST_CODE = 777;
    int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    int PERMISSION_REQ_POST_NOTIFICATIONS = 33;
    int REQUEST_MULTIPLE_PERMISSIONS = 321;
    int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = 23;

    String FOLDER_NAME = "FormFix";
    String FOLDER_DIRECTORY = "Movies";
    String MEDIA_FILES_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + ConstantsSquats.FOLDER_NAME;
    String VIDEO_TYPE = "video/mp4";
    String EXERCISE_TYPE = "exerciseType";

    String SELECTED_EXERCISE = "SELECTED_EXERCISE";
    String FILENAME_DATE_FORMATTER = "yyyy-MM-dd-HH-mm-ss";
    String DATE_FORMAT = "E, dd MMM yyyy";
    String TIME_FORMAT = "hh:mm a";
    String FILE_NAME = "FILE_NAME";


    String MESSAGE_TYPE = "MESSAGE_TYPE";
    String PREF_KEY_VIDEO_RECORDER_PERMISSION = "PREF_KEY_VIDEO_RECORDER_PERMISSION";




    String FILE_TYPE ="FILE_TYPE";
    int MESSAGE_TYPE_HEEL=111;
    int MESSAGE_TYPE_KNEE_CROSS_TOES=112;
    int MESSAGE_TYPE_KNEE_HIP_DIFF=113;

    int MESSAGE_TYPE_TOE_HIP_X_DIFF=114;
    /* Error messages Constants */
    float HEEL_MIN_ANGLE =29; // earlier it was 33 to 70 // todo correcting angles 40 to 33;
    float HEEL_MAX_ANGLE =66;

    float KNEE_TOE_THRESHOLD = 0.070F;//0.082F;//0.072F;
    float KNEE_TOE_THRESHOLD_TO_IGNORE_TUCK_HIPS =0.052F;
    float KNEE_HIP_DIFF_THRESHOLD=40;
    float KNEE_HIP_DIFF_NEW_THRESHOLD=34;


    float TOE_KNEE_X_DIFFS_MAX_THRESHOLD=100;

    float TOE_KNEE_X_DIFFS_MIN_THRESHOLD =33;//32;//39//42;//55;
    float HIPS_ANKLE_AVARGE_DIFF=2.0f;//1.8f;//2.2



}
