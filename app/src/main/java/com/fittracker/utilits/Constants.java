package com.fittracker.utilits;

import android.os.Environment;

public interface Constants {
    long SPLASH_DELAY=3000;
    int LINE_LENGTH=300;
    int HEEL_MESSAGE=111;
    int KNEE_MESSAGE=222;
    int HIP_MESSAGE=333;
    int KNEE_TOE_X_MESSAGE=444;
    int TOAST_LENGTH=100;


    float CIRCLE_RADIUS = 5.0F;
    long timerLimit = 10000;
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
    float TEXT_FACE_Y = 280f;

    /*Screen Recording Work */
    int SCREEN_RECORD_REQUEST_CODE = 777;
    int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    int PERMISSION_REQ_POST_NOTIFICATIONS = 33;
    int REQUEST_MULTIPLE_PERMISSIONS = 321;
    int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = 23;

    String FOLDER_NAME = "FormFix";
    String FOLDER_DIRECTORY = "Movies";
    String MEDIA_FILES_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + Constants.FOLDER_NAME;
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
    float HEEL_MIN_ANGLE =40;
    float HEEL_MAX_ANGLE =70;

    float KNEE_TOE_THRESHOLD = 0.072F;//0.03F;

    float KNEE_HIP_DIFF_THRESHOLD=40;
    float KNEE_HIP_DIFF_NEW_THRESHOLD=34;

    float TOE_KNEE_X_DIFFS_MIN_THRESHOLD =55;
    float TOE_KNEE_X_DIFFS_MAX_THRESHOLD=100;

}
