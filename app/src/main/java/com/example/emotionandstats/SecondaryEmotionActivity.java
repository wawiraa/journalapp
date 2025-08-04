package com.example.emotionandstats;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;

public class SecondaryEmotionActivity extends AppCompatActivity {

    private String mainMood;
    private final List<String> selectedSubEmotions = new ArrayList<>();
    private EmotionsDatabase db;
    private final List<CardView> selectedCards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondary_emotions);


        Intent intent = getIntent();
        mainMood = intent.getStringExtra("MOOD");
        int moodIconRes = intent.getIntExtra("MOOD_ICON", R.drawable.ic_back);

        db = EmotionsDatabase.getInstance(this);

        GridLayout gridLayout = findViewById(R.id.emotionGrid);
        Button saveBtn = findViewById(R.id.btnSave);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView moodIcon = findViewById(R.id.moodIcon);

        moodIcon.setImageResource(moodIconRes);
        btnBack.setOnClickListener(v -> finish());



        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View cardView = gridLayout.getChildAt(i);
            if (cardView instanceof CardView) {
                CardView card = (CardView) cardView;
                LinearLayout layout = (LinearLayout) card.getChildAt(0);
                TextView textView = (TextView) layout.getChildAt(1);
                String subEmotion = textView.getText().toString();

                card.setOnClickListener(v -> {
                    if (selectedSubEmotions.contains(subEmotion)) {

                        selectedSubEmotions.remove(subEmotion);
                        selectedCards.remove(card);
                        card.setCardBackgroundColor(Color.parseColor("#FAF5F5"));
                    } else {

                        selectedSubEmotions.add(subEmotion);
                        selectedCards.add(card);
                        card.setCardBackgroundColor(Color.parseColor("#E5A980"));
                    }
                });
            }
        }
        Button fakebutton = findViewById(R.id.fakebutton);
        fakebutton.setOnClickListener(v -> {
            Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
            Intent statsIntent = new Intent(SecondaryEmotionActivity.this, StatsHostActivity.class);
            startActivity(statsIntent);

        });














        saveBtn.setOnClickListener(v -> {
            String joinedSubEmotions = String.join(", ", selectedSubEmotions);

            Emotions emotion = new Emotions();
            emotion.mainMood = mainMood;
            emotion.subEmotion = joinedSubEmotions;
            emotion.timestamp = System.currentTimeMillis();

            new Thread(() -> {
                db.emotionsDao().insert(emotion);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();

                });
            }).start();
        });
    }
}
