package com.score.chatz.handlers;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.exceptions.CameraBusyException;
import com.score.chatz.interfaces.IComHandler;
import com.score.chatz.pojo.Secret;
import com.score.chatz.pojo.SenzStream;
import com.score.chatz.services.SenzServiceConnection;
import com.score.chatz.utils.CameraUtils;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Lakmal on 9/4/16.
 */
public class SenzPhotoHandler extends BaseHandler implements IComHandler {
    private static final String TAG = SenzPhotoHandler.class.getName();
    private static SenzPhotoHandler instance;

    /**
     * Singleton
     *
     * @return
     */
    public static SenzPhotoHandler getInstance() {
        if (instance == null) {
            instance = new SenzPhotoHandler();
        }
        return instance;
    }

    public void handleSenz(Senz senz, ISenzService senzService, SenzorsDbSource dbSource, Context context) {

        //If camera not available send unsuccess confirmation to user
        if (CameraUtils.isCameraFrontAvailable(context)) {
            //Start camera activity
            try {
                launchCamera(context, senz);
            } catch (CameraBusyException ex) {
                Log.e(TAG, "Camera is busy right now.");
                sendConfirmation(null, senzService, senz.getSender(), false);
            }

        } else {
            //Camera not available on this phone
            sendConfirmation(null, senzService, senz.getSender(), false);

        }
    }

    /**
     * Share back to user if unsuccessful
     *
     * @param senzService
     * @param receiver
     * @param isDone
     */
    public void sendConfirmation(Senz _senz, ISenzService senzService, User receiver, boolean isDone) {
        if (isDone == false) {
            try {
                // create senz attributes
                HashMap<String, String> senzAttributes = new HashMap<>();
                senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
                senzAttributes.put("msg", "SensorNotAvailable");

                String id = "_ID";
                String signature = "";
                SenzTypeEnum senzType = SenzTypeEnum.DATA;
                Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

                senzService.send(senz);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initiate send photo from here!!
     * @param image
     * @param senz
     * @param context
     */
    public void sendPhoto(final byte[] image, final Senz senz, final Context context) {

        //Get servicve connection
        final SenzServiceConnection serviceConnection = SenzHandler.getInstance(context).getServiceConnection();

        serviceConnection.executeAfterServiceConnected(new Runnable() {
            @Override
            public void run() {
                // service instance
                ISenzService senzService = serviceConnection.getInterface();

                // service instance
                Log.d(TAG, "send response(share back) for photo : " + image);

                Log.i(TAG, "USER INFO - senz.getSender() : " + senz.getSender().getUsername() + ", senz.getReceiver() : " + senz.getReceiver().getUsername());
                try {
                    // compose senzes
                    Senz startSenz = getStartPhotoSharingSenze(senz);
                    senzService.send(startSenz);

                    ArrayList<Senz> photoSenzList = getPhotoStreamingSenz(senz, image, context);

                    Senz stopSenz = getStopPhotoSharingSenz(senz);

                    ArrayList<Senz> senzList = new ArrayList<Senz>();
                    senzList.addAll(photoSenzList);
                    senzList.add(stopSenz);
                    senzService.sendInOrder(senzList);


                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private ArrayList<Senz> getPhotoStreamingSenz(Senz senz, byte[] image, Context context) {
        String imageAsString = Base64.encodeToString(image, Base64.DEFAULT);
        String thumbnail = CameraUtils.resizeBase64Image(imageAsString);

        ArrayList<Senz> senzList = new ArrayList<>();
        String[] imgs = split(imageAsString, 1024);

        if (senz.getAttributes().containsKey("chatzphoto")) {
            //Save photo to db before sending if its a chatzphoto
            new SenzorsDbSource(context).createSecret(new Secret(null, imageAsString, thumbnail, senz.getReceiver(), senz.getSender()));
        }

        for (int i = 0; i < imgs.length; i++) {
            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.DATA;

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            if (senz.getAttributes().containsKey("chatzphoto")) {
                senzAttributes.put("chatzphoto", imgs[i].trim());
            } else if (senz.getAttributes().containsKey("profilezphoto")) {
                senzAttributes.put("profilezphoto", imgs[i].trim());
            }

            Senz _senz = new Senz(id, signature, senzType, senz.getReceiver(), senz.getSender(), senzAttributes);
            senzList.add(_senz);
        }

        return senzList;
    }


    private Senz getStartPhotoSharingSenze(Senz senz) {
        //senz is the original senz
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
        senzAttributes.put("stream", "on");

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.DATA;
        Log.i(TAG, "Senz receiver - " + senz.getReceiver());
        Log.i(TAG, "Senz sender - " + senz.getSender());
        Senz _senz = new Senz(id, signature, senzType, senz.getReceiver(), senz.getSender(), senzAttributes);
        return _senz;
    }

    private Senz getStopPhotoSharingSenz(Senz senz) {
        // create senz attributes
        //senz is the original senz
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
        senzAttributes.put("stream", "off");

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.DATA;
        Senz _senz = new Senz(id, signature, senzType, senz.getReceiver(), senz.getSender(), senzAttributes);
        return _senz;
    }

    /**
     * Launch your camera from here!!
     * @param context
     * @param senz
     * @throws CameraBusyException
     */
    public void launchCamera(Context context, Senz senz) throws CameraBusyException {
        //Start
        Intent intent = AppIntentHandler.getCameraIntent(context);

        //To pass:
        intent.putExtra("Senz", senz);
        if (senz.getAttributes().containsKey("chatzphoto")) {
            intent.putExtra("StreamType", SenzStream.SENZ_STEAM_TYPE.CHATZPHOTO);
        } else if (senz.getAttributes().containsKey("profilezphoto")) {
            intent.putExtra("StreamType", SenzStream.SENZ_STEAM_TYPE.PROFILEZPHOTO);
        }

        try {
            context.startActivity(intent);
        } catch (Exception ex) {
            Log.d(TAG, "Camera might already be in use... exception: " + ex);
            throw new CameraBusyException();
        }
    }
}
