package com.score.chatz.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.handlers.SenzHandler;
import com.score.chatz.handlers.SenzSoundHandler;
import com.score.chatz.pojo.Secret;
import com.score.chatz.utils.AudioRecorder;
import com.score.chatz.utils.AudioUtils;
import com.score.chatz.utils.CameraUtils;
import com.score.chatz.utils.PreferenceUtils;
import com.score.chatz.utils.VibrationUtils;
import com.score.senzc.pojos.User;
import com.skyfishjy.library.RippleBackground;

public class RecordingActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = RecordingActivity.class.getName();
    private TextView mTimerTextView;
    private long mStartTime = 10;
    private Thread ticker;

    private User sender;
    private User receiver;

    private boolean isRecordingDone;

    SenzorsDbSource dbSource;

    AudioRecorder audioRecorder;

    private PowerManager.WakeLock wakeLock;

    private View moving_layout;
    private boolean hasRecordingStarted;


    private Rect startBtnRectRelativeToScreen;
    private Rect cancelBtnRectRelativeToScreen;
    private CircularImageView cancelBtn;
    private ImageView startBtn;
    private RippleBackground goRipple;
    private float dX, dY, startX, startY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_recording);


        this.mTimerTextView = (TextView) this.findViewById(R.id.timer);
        this.mTimerTextView.setText(mStartTime + "");

        this.moving_layout = (View) findViewById(R.id.moving_layout);

        Intent intent = getIntent();
        try {
            receiver = PreferenceUtils.getUser(getApplicationContext());
        } catch (NoUserException e) {
            e.printStackTrace();
        }
        String senderString = intent.getStringExtra("SENDER");
        sender = new User("", senderString);

        dbSource = new SenzorsDbSource(this);

        audioRecorder = new AudioRecorder();

        isRecordingDone = false;

        Log.i(TAG, "SENDER FROM RECORDING ACTIVITY - " + sender.getUsername());
        Log.i(TAG, "RECEIVERE FROM RECORDING ACTIVITY - " + receiver.getUsername());

        setupSwipeBtns();
        startBtnAnimations();
        startVibrations();
        setupHandlesForSwipeBtnContainers();

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        wakeLock.acquire();


    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            startBtnRectRelativeToScreen = new Rect(startBtn.getLeft(), startBtn.getTop(), startBtn.getRight(), startBtn.getBottom());
            cancelBtnRectRelativeToScreen = new Rect(cancelBtn.getLeft(), cancelBtn.getTop(), cancelBtn.getRight(), cancelBtn.getBottom());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Release screen lock, so the phone can go back to sleep
        wakeLock.release();
        stopVibrations();
    }

    private void setupSwipeBtns(){
        cancelBtn = (CircularImageView) findViewById(R.id.cancel);
        startBtn = (ImageView) findViewById(R.id.start);
    }

    private void startVibrations(){
        VibrationUtils.startVibrationForPhoto(VibrationUtils.getVibratorPatterIncomingPhotoRequest(), this);
    }

    private void stopVibrations(){
        VibrationUtils.stopVibration(this);
    }

    private void setupHandlesForSwipeBtnContainers() {
        goRipple.setOnTouchListener(this);
    }

    private void startBtnAnimations(){
        Animation anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake);
        goRipple=(RippleBackground)findViewById(R.id.go_ripple);
        goRipple.startRippleAnimation();
        goRipple.startAnimation(anim);
    }

    public boolean onTouch(View v, MotionEvent event) {
        int x = (int)event.getRawX();
        int y = (int)event.getRawY();
        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                v.clearAnimation();
                startX = v.getX();
                startY = v.getY();
                dX = v.getX() - event.getRawX();
                dY = v.getY() - event.getRawY();

                break;

            case (MotionEvent.ACTION_MOVE):
                v.animate()
                        .x(event.getRawX() + dX)
                        .y(event.getRawY() + dY)
                        .setDuration(0)
                        .start();
                if(startBtnRectRelativeToScreen.contains((int)(event.getRawX()), (int)(event.getRawY()))){
                    // Inside start button region
                    if(hasRecordingStarted == false) {
                        hasRecordingStarted = true;
                        stopVibrations();
                        startRecording();
                        moving_layout.setVisibility(View.INVISIBLE);
                    }
                }else if(cancelBtnRectRelativeToScreen.contains((int)(event.getRawX()), (int)(event.getRawY()))){
                    // Inside cancel button region
                    stopVibrations();
                    stopRecording();
                }
                break;
            case (MotionEvent.ACTION_UP):
                v.animate()
                        .x(startX)
                        .y(startY)
                        .setDuration(0)
                        .start();
                Animation anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake);
                v.startAnimation(anim);
                break;
        }
        return true;
    }


    private void startRecording() {
        audioRecorder.startRecording(getApplicationContext());
        ticker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mStartTime > 0) {
                    tick();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Log.e(TAG, "Ticker thread interrupted");
                    }
                }
            }
        });
        ticker.start();
    }

    private void stopRecording() {
        // stops the recording activity
        if(isRecordingDone == false) {
            isRecordingDone = true;
            audioRecorder.stopRecording();
            if (audioRecorder.getRecording() != null) {
                Secret secret = getSoundSecret(receiver, sender, Base64.encodeToString(audioRecorder.getRecording().toByteArray(), 0));
                dbSource.createSecret(secret);
                sendSecret(secret);
            }
        }
        this.finish();
    }

    private void sendSecret(Secret secret) {
        SenzSoundHandler.getInstance().sendSound(secret, this);
    }

    private Secret getSoundSecret(User sender, User receiver, String sound) {
        //Swapping receiever and sender here cause we need to send the secret out
        Secret secret = new Secret(null, null, null, receiver, sender);
        secret.setSound(sound);
        return secret;
    }


    private void tick() {
        try {
            if (mStartTime > 0) {
                mStartTime--;
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTimerTextView.setText(mStartTime + "");
                    }
                });
                if (mStartTime == 0) {
                    stopRecording();
                }
            } else {
                stopRecording();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Tick method exceptop - " + ex);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}







