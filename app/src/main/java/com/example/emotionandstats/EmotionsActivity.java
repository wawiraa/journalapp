package com.example.emotionandstats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmotionsActivity extends AppCompatActivity {

    private EmotionsDatabase db;


    private final Map<String, Integer> moodIconMap = new HashMap<String, Integer>() {{
        put("Great", R.drawable.ic_happy);
        put("Good", R.drawable.ic_good);
        put("Okay", R.drawable.ic_okay);
        put("Bad", R.drawable.ic_bad);
        put("Awful", R.drawable.ic_awful);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emotions);


        db = Room.databaseBuilder(getApplicationContext(),
                        EmotionsDatabase.class, "emotions_db")
                .build();

        setupClick(R.id.btnGreat, "Great");
        setupClick(R.id.btnGood, "Good");
        setupClick(R.id.btnOkay, "Okay");
        setupClick(R.id.btnBad, "Bad");
        setupClick(R.id.btnAwful, "Awful");
    }

    private void setupClick(int viewId, String mainMood) {
        View moodButton = findViewById(viewId);
        moodButton.setOnClickListener(view -> {

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Emotions emotion = new Emotions();
                emotion.mainMood = mainMood;
                emotion.timestamp = System.currentTimeMillis();
                emotion.subEmotion = null;

                db.emotionsDao().insert(emotion);
            });

            Intent intent = new Intent(EmotionsActivity.this, SecondaryEmotionActivity.class);
            intent.putExtra("MOOD", mainMood);
            int iconResId = moodIconMap.getOrDefault(mainMood, R.drawable.ic_back);
            intent.putExtra("MOOD_ICON", iconResId);

            startActivity(intent);
        });
    }

}
