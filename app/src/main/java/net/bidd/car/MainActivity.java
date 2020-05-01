package net.bidd.car;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

import io.github.controlwear.virtual.joystick.android.JoystickView;


public class MainActivity extends AppCompatActivity {

    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = false;
    private MediaPlayer mMediaPlayer = null;
    private LibVLC mLibVLC = null;
    private VLCVideoLayout mVideoLayout = null;
    private JoystickView direction_car;
    private CarControl carControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //状态栏透明
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
        //隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        final ArrayList<String> args = new ArrayList<>();
        // --longhelp
        // --advanced
        args.add("-vvv");
        args.add("--no-audio");//关闭音频 --audio
        args.add("--fullscreen");
        args.add("--no-keyboard-events");
        args.add("--no-mouse-events");
        args.add("--repeat");
//        args.add("--rtsp-tcp");//rtsp使用tcp
        args.add("--avcodec-hw=any");//硬件解码 --avcodec-hw={any,d3d11va,dxva2,none}
        args.add("--fps=60");
        args.add("--no-avcodec-corrupted");//不显示损坏的帧
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);

        carControl = CarControl.getCarControl();
        mVideoLayout = findViewById(R.id.video_layout);
        direction_car = findViewById(R.id.direction_car);
        direction_car.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if (strength > 30) {
                    if (angle > 135 && angle < 225) {
                        //left
                        Log.d("OnMoveListener", "LEFT");
                        carControl.write(Constants.COMMAND_CAR[2]);
                    } else if (angle <= 135 && angle >= 45) {
                        //go
                        Log.d("OnMoveListener", "GO");
                        carControl.write(Constants.COMMAND_CAR[0]);
                    } else if (angle < 45 || angle > 315) {
                        //right
                        Log.d("OnMoveListener", "RIGHT");
                        carControl.write(Constants.COMMAND_CAR[3]);
                    } else if (angle <= 315 && angle >= 225) {
                        //back
                        Log.d("OnMoveListener", "BACK");
                        carControl.write(Constants.COMMAND_CAR[1]);
                    }
//                    Log.d("OnMoveListener", "angle:" + angle + "\tstrength" + strength);
                }else{
                    carControl.write(Constants.COMMAND_CAR[4]);
                }
            }
        },100);
    }
    public void onClick(View v){
        onStop();
        onStart();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mLibVLC.release();
        carControl.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        carControl = CarControl.getCarControl();
        mMediaPlayer.attachViews(mVideoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW);
        final Media media = new Media(mLibVLC, Uri.parse(Constants.RTSP_URL));
        media.addOption(":network-caching=60");
        media.addOption(":live-caching=60");
        mMediaPlayer.setMedia(media);
        media.release();
        mMediaPlayer.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaPlayer.stop();
        mMediaPlayer.detachViews();
        carControl.close();
    }
}
