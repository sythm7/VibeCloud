package com.example.vibecloud;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class MusicListening extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    //fields
    TextView nameView, authorView;
    ImageView imageView, play_pause, nextSong, previousSong, repeat;
    SeekBar seekbar;
    private int loop=0;
    public volatile String json_return;
    public ArrayList<Music> listMusicNext = new ArrayList();
    private String web_song_url;
    Context context = this;
    String texte = "Hello";
    private ArrayList<Music> recommendation = new ArrayList();

    private int index_playlist, max;

    TextView current, missed;

    BlurView blurView;
    MediaPlayer mediaPlayer;
    Music song;
    String image_url, name, author;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_listening);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //extracting infos from previously Activity
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("bundle_recommendation");
        recommendation = (ArrayList<Music>) args.getSerializable("recommendation");

        for (int i=0; i<recommendation.size(); i++){
            System.out.println(recommendation.get(i).getName());
        }
        //this.index_playlist=getIntent().getExtras().getInt("i");
        //System.out.println(this.index_playlist);

        /*for (int i=0; i<4;i++){
            System.out.println(song_info[i]);
        }
        System.out.println("ICI : " + song_info[0]);*/
        song = recommendation.get(index_playlist);
        String name = song.getName();
        String author = song.getAuthor();
        image_url = song.getImage();
        web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();

        current = findViewById(R.id.song_current);
        missed = findViewById(R.id.song_missed);

        //Setting variables uptoDate
        nameView = findViewById(R.id.music_name);
        authorView = findViewById(R.id.music_author);
        imageView = findViewById(R.id.music_image);
        play_pause = findViewById(R.id.pause_play_image);
        seekbar = findViewById(R.id.seek_bar);

        nextSong = findViewById(R.id.next);
        previousSong = findViewById(R.id.previous);
        blurView = findViewById(R.id.blur_layout);

        nameView.setText(name);
        authorView.setText(author);

        repeat = findViewById(R.id.repeat);

        //Starting song
        mediaPlayer = new MediaPlayer();

        Thread t = new Thread() {
            public void run() {
                InputStream is = null;
                try {
                    is = (InputStream) new URL(image_url).getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Drawable d = Drawable.createFromStream(is, "blurry image");
                LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                linearLayout.setBackgroundDrawable(d);
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        blurBackground();
        start_music();

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    seekbar.setProgress(mediaPlayer.getCurrentPosition()/1000);
                    mediaPlayer.pause();
                    Drawable myDrawable = getResources().getDrawable(R.drawable.play);
                    play_pause.setImageDrawable(myDrawable);
                }
                else{
                    mediaPlayer.start();
                    Drawable myDrawable = getResources().getDrawable(R.drawable.pause);
                    play_pause.setImageDrawable(myDrawable);
                }
            }
        });

        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loop==0) {
                    if (!(index_playlist==recommendation.size()-1)) {
                        mediaPlayer.reset();
                        index_playlist++;

                        song=recommendation.get(index_playlist);
                        image_url=song.getImage();

                        Thread t = new Thread() {
                            public void run() {
                                InputStream is = null;
                                try {
                                    is = (InputStream) new URL(image_url).getContent();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Drawable d = Drawable.createFromStream(is, "blurry image");
                                LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                                linearLayout.setBackgroundDrawable(d);
                            }
                        };
                        t.start();
                        start_music();
                    }
                    else{
                        mediaPlayer.seekTo(0);
                        mediaPlayer.start();
                    }
                }
                else{
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
            }
        });

        previousSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index_playlist!=0) {
                    mediaPlayer.reset();
                    index_playlist--;

                    song=recommendation.get(index_playlist);
                    image_url=song.getImage();

                    Thread t = new Thread() {
                        public void run() {
                            InputStream is = null;
                            try {
                                is = (InputStream) new URL(image_url).getContent();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Drawable d = Drawable.createFromStream(is, "blurry image");
                            LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                            linearLayout.setBackgroundDrawable(d);
                        }
                    };
                    t.start();

                    //blurBackground();
                    start_music();
                }
                else {
                    System.out.println(index_playlist);
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }

            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if(fromUser) {
                    mediaPlayer.seekTo(seekbar.getProgress() * 1000);
                }
                int time = seekBar.getProgress();
                int decimal = time-(time/60)*60;
                String d = String.valueOf(time-(time/60)*60);
                if (decimal<10){
                    d=0+String.valueOf(decimal);
                }
                String c = String.valueOf(time/60) + ":" + d;
                current.setText(c);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loop==0){
                    loop=1;
                    Drawable myDrawable = getResources().getDrawable(R.drawable.repeat_loop);
                    repeat.setImageDrawable(myDrawable);
                }
                else{
                    loop=0;
                    Drawable myDrawable = getResources().getDrawable(R.drawable.repeat);
                    repeat.setImageDrawable(myDrawable);
                }
            }
        });

        mediaPlayer.setOnCompletionListener(this);


        if (index_playlist==recommendation.size()-1){
            t = new Thread() {
                public void run() {
                    continueRecommendation(recommendation.get(index_playlist).getId());
                }
            };
            t.start();
        }

    }

    private void blurBackground() {
        float radius = 5f;

        View decorView = getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);

        //blurView.setAlpha(0.2f);

    }

    public void continueRecommendation(String song){
        String search = "{\"video\": \"" + song + "\"}";
        String url = MusicSelection.url_base + "recommendation";
        System.out.println(search);
        System.out.println(url);

        json_return = null;

        Thread t = new Thread() {
            public void run() {
                try {
                    json_return = MainActivity.sendRequest(url, search);
                } catch (RequestException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            JSONArray ja = new JSONArray(json_return);

            for (int i=0; i<15; i++){
                //MUSIC
                JSONObject j = ja.getJSONObject(i);
                String title = j.getString("title");
                String url_image = j.getString("thumbnail");
                String a = j.getString("artists");
                String id = j.getString("link");
                Music m = new Music(title, a, url_image);
                m.setId(id);
                if (!recommendation.contains(m))
                    recommendation.add(m);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void setIndex_playlist(int i){
        this.index_playlist=i;
    }

    public void start_music(){
        //Starting song
        name = song.getName();
        author = song.getAuthor();
        image_url = song.getImage();
        web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();

        nameView.setText(name);
        authorView.setText(author);

        try {
            Glide.with(this).load(image_url).into(imageView);
            System.out.println(web_song_url);
            mediaPlayer.setDataSource(web_song_url);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    SystemClock.sleep(200);
                    mp.start();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            System.out.println("WTF");
            e.printStackTrace();
        }

        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                while (!mediaPlayer.isPlaying());
                max = mediaPlayer.getDuration()/1000;
                seekbar.setMax(max);
                seekbar.setProgress(mediaPlayer.getCurrentPosition()/1000);

                int decimal = max-(max/60)*60;
                String d = String.valueOf(max-(max/60)*60);
                if (decimal<10){
                    d=0+String.valueOf(decimal);
                }
                String c = String.valueOf(max/60) + ":" + d;
                missed.setText(c);
            }
        }, 1000, 1000);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (loop==0){
            if (!(index_playlist==recommendation.size()-1)) {
                mediaPlayer.reset();
                mediaPlayer.release();
                index_playlist++;

                song=recommendation.get(index_playlist);
                image_url=song.getImage();

                Thread t = new Thread() {
                    public void run() {
                        InputStream is = null;
                        try {
                            is = (InputStream) new URL(image_url).getContent();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Drawable d = Drawable.createFromStream(is, "blurry image");
                        LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                        linearLayout.setBackgroundDrawable(d);
                    }
                };
                t.start();

                //blurBackground();
                start_music();
                /*timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        max = mediaPlayer.getDuration()/1000;
                        seekbar.setMax(max);
                        seekbar.setProgress(mediaPlayer.getCurrentPosition()/1000);

                        int decimal = max-(max/60)*60;
                        String d = String.valueOf(max-(max/60)*60);
                        if (decimal<10){
                            d=0+String.valueOf(decimal);
                        }
                        String c = String.valueOf(max/60) + ":" + d;
                        missed.setText(c);
                    }
                }, 1000, 1000);*/

            }
            else{
                Thread t = new Thread() {
                    public void run() {
                        continueRecommendation(recommendation.get(index_playlist).getId());
                    }
                };
                t.start();
            }
        }
        else{
            System.out.println("HERE1");
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    public void onBackPressed() {
        Intent otherActivity;
        otherActivity = new Intent(getApplicationContext(), ActivityHome.class);
        startActivity(otherActivity);
        finish();
    }
}