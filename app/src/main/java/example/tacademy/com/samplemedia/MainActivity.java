package example.tacademy.com.samplemedia;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mPlayer;
    enum PlayState {
        IDLE,
        INITIALIED,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED,
        ERROR,
        RELEASED
    }

    PlayState mState;

    SeekBar progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressView = (SeekBar)findViewById(R.id.seek_progress);
        mPlayer = MediaPlayer.create(this, R.raw.winter_blues);
        mState = PlayState.PREPARED;

        progressView.setMax(mPlayer.getDuration());

        Button btn = (Button)findViewById(R.id.btn_play);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });
        btn = (Button)findViewById(R.id.btn_pause);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause();
            }
        });

        btn = (Button)findViewById(R.id.btn_stop);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });

        progressView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           int progress = -1;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    this.progress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                progress = -1;
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(progress != -1){
                    if(mState == PlayState.STARTED){
                        mPlayer.seekTo(progress);
                    }
                }
                isSeeking = false;
            }
        });
    }

    boolean isSeeking = false;
    Handler mHandler = new Handler(Looper.getMainLooper());

    Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if(mState == PlayState.STARTED){
                if(!isSeeking){
                    int position = mPlayer.getCurrentPosition();
                    progressView.setProgress(position);
                }
                mHandler.postDelayed(this,100);
            }
        }
    };

    private void play() {
        if (mState == PlayState.INITIALIED || mState == PlayState.STOPPED) {
            try {
                mPlayer.prepare();
                mState = PlayState.PREPARED;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mState == PlayState.PREPARED || mState == PlayState.PAUSED) {
            mPlayer.seekTo(progressView.getProgress());
            mPlayer.start();
            mState = PlayState.STARTED;
            mHandler.post(progressRunnable);
        }
    }

    private void pause() {
        if (mState == PlayState.STARTED) {
            mPlayer.pause();
            mState = PlayState.PAUSED;
        }
    }

    private void stop() {
        if (mState == PlayState.STARTED || mState == PlayState.PAUSED) {
            mPlayer.stop();
            mState = PlayState.STOPPED;
            progressView.setProgress(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release();
        mState = PlayState.RELEASED;
        mPlayer = null;
    }
}
