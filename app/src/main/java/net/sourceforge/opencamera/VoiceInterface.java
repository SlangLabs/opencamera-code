package net.sourceforge.opencamera;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

import in.slanglabs.platform.application.ISlangApplicationStateListener;
import in.slanglabs.platform.application.SlangApplication;
import in.slanglabs.platform.application.SlangApplicationUninitializedException;
import in.slanglabs.platform.application.actions.DefaultResolvedIntentAction;
import in.slanglabs.platform.session.SlangResolvedIntent;
import in.slanglabs.platform.session.SlangSession;

/**
 * TODO: Add a class header comment!
 */

public class VoiceInterface {
    public static long timerDelay = 0;
    static private Handler handler;

    public static void init(final Application appContext, String appId, String authKey, final boolean shouldHide) {
        SlangApplication.initialize(appContext, appId, authKey, new ISlangApplicationStateListener() {
            @Override
            public void onInitialized() {
                try {
                    registerActionsNew();
                } catch (SlangApplicationUninitializedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onInitializationFailed(FailureReason reason) {
                Toast.makeText(appContext, "Not able to initialize", Toast.LENGTH_LONG).show();
            }
        });

        SlangApplication.setDefaultContinuationMode(SlangSession.ContinuationMode.DISMISS);
        handler = new Handler();
    }

    private static void registerActionsNew() throws SlangApplicationUninitializedException {
        SlangApplication.getIntentDescriptor("take_selfie").setResolutionAction(new DefaultResolvedIntentAction() {
            @Override
            public SlangSession.Status action(final SlangResolvedIntent intent, final SlangSession session) {
                if (intent.getEntity("timer_delay").isResolved()) {
                    timerDelay =
                        ((int) Double.parseDouble(intent.getEntity("timer_delay").getValue())) * 1000;
                }

                final Activity currentActivity = SlangApplication.getScreenContext().getCurrentActivity();

                if (!(currentActivity instanceof MainActivity)) {
                    // Cannot handle this intent if its not in the main activity
                    return session.failure();
                }

                if (!((MainActivity) currentActivity).isFrontCameraOn()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity) currentActivity).switchToFrontCamera();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((MainActivity) currentActivity).takePicture(false);
                                    timerDelay = 0;
                                    session.success();
                                }
                            }, 2000);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity) currentActivity).takePicture(false);
                            timerDelay = 0;
                            session.success();
                        }
                    });
                }

                return session.suspend();
            }
        });

        SlangApplication.getIntentDescriptor("take_photo").setResolutionAction(new DefaultResolvedIntentAction() {
            @Override
            public SlangSession.Status action(final SlangResolvedIntent intent, final SlangSession session) {
                if (intent.getEntity("timer_delay").isResolved()) {
                    timerDelay =
                        ((int) Double.parseDouble(intent.getEntity("timer_delay").getValue())) * 1000;
                }

                final Activity currentActivity = SlangApplication.getScreenContext().getCurrentActivity();

                if (!(currentActivity instanceof MainActivity)) {
                    // Cannot handle this intent if its not in the main activity
                    return session.failure();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) currentActivity).takePicture(false);
                        timerDelay = 0;
                        session.success();
                    }
                });

                return session.suspend();
            }
        });
    }
}
