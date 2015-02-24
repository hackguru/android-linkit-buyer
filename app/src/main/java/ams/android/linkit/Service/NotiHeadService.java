package ams.android.linkit.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ams.android.linkit.Activity.MainActivity;
import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;
import me.leolin.shortcutbadger.ShortcutBadgeException;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by Aidin on 2/2/2015.
 */
public class NotiHeadService extends Service {

    View rootView = null;
    ImageView img = null;
    TextView txtTitle = null;
    RelativeLayout layoutInfo = null;
    Timer timer = new Timer();
    ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options;
    ImageLoadingListener imageListener;
    Handler handler;
    private WindowManager windowManager;

    public static Bitmap drawShadow(Bitmap bitmap, int leftRightThk, int bottomThk, int padTop) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int newW = w - (leftRightThk * 2);
        int newH = h - (bottomThk + padTop);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(w, h, conf);
        Bitmap sbmp = Bitmap.createScaledBitmap(bitmap, newW, newH, false);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(bmp);

        // Left
        int leftMargin = (leftRightThk + 7) / 2;
        Shader lshader = new LinearGradient(0, 0, leftMargin, 0, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);
        paint.setShader(lshader);
        c.drawRect(0, padTop, leftMargin, newH, paint);

        // Right
        Shader rshader = new LinearGradient(w - leftMargin, 0, w, 0, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        paint.setShader(rshader);
        c.drawRect(newW, padTop, w, newH, paint);

        // Bottom
        Shader bshader = new LinearGradient(0, newH, 0, bitmap.getHeight(), Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        paint.setShader(bshader);
        c.drawRect(leftMargin - 3, newH, newW + leftMargin + 3, bitmap.getHeight(), paint);
        c.drawBitmap(sbmp, leftRightThk, 0, null);

        return bmp;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String imageUrl = (String) intent.getExtras().get("imageUrl");
        final String linkSrceenShot = (String) intent.getExtras().get("linkSrceenShot");
        final String productLink = (String) intent.getExtras().get("productLink");
        final String text = (String) intent.getExtras().get("text");

        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.ic_launcher)
                .showStubImage(R.drawable.ic_launcher)
                .showImageForEmptyUri(R.drawable.ic_launcher).cacheInMemory()
                .cacheOnDisc().build();

        imageListener = new ImageDisplayListener();

        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        }

        // badget number
        try {
            int count = ((GlobalApplication) getApplication()).getBadgeCount();
            ShortcutBadger.setBadge(getApplicationContext(), count);
            ((GlobalApplication) getApplication()).setBadgetCount(count + 1);
        } catch (ShortcutBadgeException e) {
            //handle the Exception
        }


        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (rootView == null) rootView = inflater.inflate(R.layout.noti_head, null);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP;


        layoutInfo = (RelativeLayout) rootView.findViewById(R.id.lay_noti_text);
        //RelativeLayout layClick = (RelativeLayout)rootView.findViewById(R.id.lay_noti_main);
        img = (ImageView) rootView.findViewById(R.id.img_noti);
        txtTitle = (TextView) rootView.findViewById(R.id.txtNotiTitle);

        txtTitle.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            @Override
            public void onSwipeRight() {
                //Toast.makeText(getApplicationContext(), "Right", Toast.LENGTH_SHORT).show();
//                new mainTask().run();
//                timer.cancel();
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if (event.getAction()==MotionEvent.ACTION_UP)
                {
                    //Toast.makeText(getApplicationContext(), "CLICK", Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent().setClass(NotiHeadService.this, MainActivity.class);
                    myIntent.putExtra("RunByNoti", true);
                    myIntent.putExtra("imageUrl", imageUrl);
                    myIntent.putExtra("linkSrceenShot", linkSrceenShot);
                    myIntent.putExtra("productLink", productLink);
                    myIntent.putExtra("text", text);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(myIntent);

                    new mainTask().run();
                    timer.cancel();
                }

                return super.onTouch(v, event);

            }
        });

        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(imageUrl, img, options, imageListener);
        txtTitle.setText(text);

        // Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
        //img.setImageBitmap(drawShadow(bm, 0, 0, 0));

//        rootView.setOnTouchListener(new View.OnTouchListener() {
//            private int initialX;
//            private int initialY;
//            private float initialTouchX;
//            private float initialTouchY;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                //Toast.makeText(getApplicationContext(),"Touch",Toast.LENGTH_SHORT).show();
//
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        initialX = params.x;
//                        initialY = params.y;
//                        initialTouchX = event.getRawX();
//                        initialTouchY = event.getRawY();
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                        return true;
//                    case MotionEvent.ACTION_MOVE:
//                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
//                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
//                        windowManager.updateViewLayout(rootView, params);
//                        return true;
//                }
//                return false;
//            }
//        });

        Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_in);
        animation.setStartOffset(0);
        Animation animation2 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_in);
        animation2.setStartOffset(250);
        img.startAnimation(animation);
        layoutInfo.startAnimation(animation2);
        windowManager.addView(rootView, params);
        timer.schedule(new mainTask(), 10000);
        handler = new Handler();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private static class ImageDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections
                .synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    private class mainTask extends TimerTask {
        public void run() {
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_out);
                            animation.setStartOffset(250);
                            Animation animation2 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_out);
                            animation2.setStartOffset(0);
                            animation2.setFillAfter(true);
                            animation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    if (rootView != null) windowManager.removeView(rootView);
                                    getApplicationContext().stopService(new Intent(getApplicationContext(), NotiHeadService.class));
                                }
                            });
                            img.startAnimation(animation);
                            layoutInfo.startAnimation(animation2);
                        }
                    });

            //
        }
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureListener());
        }

        public void onSwipeLeft() {
        }

        public void onSwipeRight() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_DISTANCE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();
                    return true;
                }
                return false;
            }
        }
    }

}