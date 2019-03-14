package net.sourceforge.opencamera;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangIntent;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.platform.SlangSession;
import in.slanglabs.platform.SlangBuddyOptions;
import in.slanglabs.platform.action.SlangIntentAction;

import static in.slanglabs.platform.action.SlangAction.Status.FAILURE;
import static in.slanglabs.platform.action.SlangAction.Status.SUCCESS;



/**
 * TODO: Add a class header comment!
 */

public class VoiceInterface {
    public static long timerDelay = 0;
    static private Handler handler;

    public static void init(final Application appContext, String buddyId, String authKey, final boolean shouldHide) {
        try {

            SlangBuddyOptions options = new SlangBuddyOptions.Builder()
                    .setContext(appContext)
                    .setBuddyId(buddyId)
                    .setAPIKey(authKey)
                    .setIntentAction(new SlangAction())
                    .setRequestedLocales(SlangLocale.getSupportedLocales())
                    .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                    .setEnvironment(SlangBuddy.Environment.STAGING)
                    .build();

            SlangBuddy.initialize(options);
        } catch (SlangBuddyOptions.InvalidOptionException e) {
            e.printStackTrace();
        } catch (SlangBuddy.InsufficientPrivilegeException e) {
            e.printStackTrace();
        }

        handler = new Handler();
    }

    private static class SlangAction implements SlangIntentAction {
        @Override
        public Status action(SlangIntent intent, SlangSession session) {
            if (intent.getName().equals("take_selfie")) {
                if (intent.getEntity("timer_delay").isResolved()) {
                    timerDelay = ((int) Double.parseDouble(intent.getEntity("timer_delay").getValue())) * 1000;
                }

                final Activity currentActivity = session.getCurrentActivity();

                if (!(currentActivity instanceof MainActivity)) {
                    // Cannot handle this intent if its not in the main activity

                    return FAILURE;
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
                                }
                            }, 2000);
                        }
                    });
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity) currentActivity).takePicture(false);
                            timerDelay = 0;
                        }
                    }, 100);
                }
            } else if (intent.getName().equals("take_photo")) {
                if (intent.getEntity("timer_delay").isResolved()) {
                    timerDelay =
                            ((int) Double.parseDouble(intent.getEntity("timer_delay").getValue())) * 1000;
                }

                final Activity currentActivity = session.getCurrentActivity();

                if (!(currentActivity instanceof MainActivity)) {
                    // Cannot handle this intent if its not in the main activity
                    return FAILURE;
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) currentActivity).takePicture(false);
                        timerDelay = 0;
                    }
                }, 100);
            }
            return SUCCESS;
        }
    }
}
