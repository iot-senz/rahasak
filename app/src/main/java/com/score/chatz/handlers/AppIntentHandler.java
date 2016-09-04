package com.score.chatz.handlers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.score.chatz.exceptions.InvalidIntentType;
import com.score.chatz.ui.PhotoActivity;
import com.score.chatz.ui.RecordingActivity;

/**
 * This class is resposible to distrubute specific or general itents.
 * Please use this wrapper to send out intents inside the app
 *
 * Note:- TODO integrate all intents into this wrapper.
 *
 * Created by Lakmal on 9/4/16.
 */
public class AppIntentHandler {
    private static final String TAG = AppIntentHandler.class.getName();

    public static Intent getDataSenzIntent() {
        Intent intent = null;
        try {
            intent = getIntent(getIntentType(INTENT_TYPE.DATA_SENZ));
        } catch (InvalidIntentType ex) {
            Log.e(TAG, "No such intent, " + ex);
        }
        return  intent;
    }

    public static Intent getCameraIntent(Context context){
        Intent intent = new Intent();
        intent.setClass(context, PhotoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getRecorderIntent(Context context){
        Intent intent = new Intent();
        intent.setClass(context, RecordingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return intent;
    }

    /**
     * Intent string generator
     * Get intents from this method, to centralize where intents are generated from for easier customization in the future.
     *
     * @param intentType
     * @return
     */
    private static String getIntentType(INTENT_TYPE intentType) throws InvalidIntentType {
        String intentString = null;
        switch (intentType) {
            case DATA_SENZ:
                intentString = "com.score.chatz.DATA_SENZ";
                break;
            default:
                throw new InvalidIntentType();
        }
        return intentString;
    }

    /**
     * return an intent object for your intentString
     *
     * @param intentString
     * @return
     */
    private static Intent getIntent(String intentString) {
        return new Intent(intentString);
    }

    enum INTENT_TYPE {
        DATA_SENZ
    }
}
