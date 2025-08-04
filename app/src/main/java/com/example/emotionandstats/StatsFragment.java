package com.example.emotionandstats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class StatsFragment extends Fragment {

    private CardView cardWriting, cardMoods, cardWeekly, cardMonthly;


    private TextView tvWriting, tvMoods, tvWeekly, tvMonthly;

    private boolean isWeekly = true;
    private boolean showingMoods = true;

    public StatsFragment() {}

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isWeekly", isWeekly);
        outState.putBoolean("showingMoods", showingMoods);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        cardWriting = view.findViewById(R.id.cardWriting);
        cardMoods = view.findViewById(R.id.cardMoods);
        cardWeekly = view.findViewById(R.id.cardWeekly);
        cardMonthly = view.findViewById(R.id.cardMonthly);

        tvWriting = cardWriting.findViewById(android.R.id.content);

        tvWriting = (TextView) cardWriting.getChildAt(0);

        tvMoods = (TextView) cardMoods.getChildAt(0);
        tvWeekly = (TextView) cardWeekly.getChildAt(0);
        tvMonthly = (TextView) cardMonthly.getChildAt(0);

        if (savedInstanceState != null) {
            isWeekly = savedInstanceState.getBoolean("isWeekly", true);
            showingMoods = savedInstanceState.getBoolean("showingMoods", true);
        }

        updateToggleUI();

        loadCurrentFragment();

        cardWriting.setOnClickListener(v -> {
            showingMoods = false;
            updateToggleUI();
            loadCurrentFragment();
        });

        cardMoods.setOnClickListener(v -> {
            showingMoods = true;
            updateToggleUI();
            loadCurrentFragment();
        });

        cardWeekly.setOnClickListener(v -> {
            isWeekly = true;
            updateToggleUI();
            loadCurrentFragment();
        });

        cardMonthly.setOnClickListener(v -> {
            isWeekly = false;
            updateToggleUI();
            loadCurrentFragment();
        });

        return view;
    }


    private void updateToggleUI() {

        if (!showingMoods) {
            cardWriting.setCardBackgroundColor(android.graphics.Color.parseColor("#A50062"));
            ((TextView) cardWriting.getChildAt(0)).setTextColor(android.graphics.Color.BLACK);
        } else {
            cardWriting.setCardBackgroundColor(android.graphics.Color.parseColor("#D461A6"));
            ((TextView) cardWriting.getChildAt(0)).setTextColor(android.graphics.Color.BLACK);
        }


        if (showingMoods) {
            cardMoods.setCardBackgroundColor(android.graphics.Color.parseColor("#5BA3E6"));
            ((TextView) cardMoods.getChildAt(0)).setTextColor(android.graphics.Color.BLACK);
        } else {
            cardMoods.setCardBackgroundColor(android.graphics.Color.parseColor("#A3D3FF"));
            ((TextView) cardMoods.getChildAt(0)).setTextColor(android.graphics.Color.BLACK);
        }


        if (isWeekly) {
            cardWeekly.setCardBackgroundColor(android.graphics.Color.parseColor("#5BA3E6"));
            ((TextView) cardWeekly.getChildAt(0)).setTextColor(android.graphics.Color.BLACK);
        } else {
            cardWeekly.setCardBackgroundColor(android.graphics.Color.parseColor("#A3D3FF"));
            ((TextView) cardWeekly.getChildAt(0)).setTextColor(android.graphics.Color.BLACK);
        }


        if (!isWeekly) {
            cardMonthly.setCardBackgroundColor(android.graphics.Color.parseColor("#A50062"));
            ((TextView) cardMonthly.getChildAt(0)).setTextColor(android.graphics.Color.BLACK);
        } else {
            cardMonthly.setCardBackgroundColor(android.graphics.Color.parseColor("#D461A6"));
            ((TextView) cardMonthly.getChildAt(0)).setTextColor(android.graphics.Color.BLACK);
        }
    }

    private void loadCurrentFragment() {
        Fragment fragment = showingMoods
                ? MoodsStatsFragment.newInstance(isWeekly)
                : WritingStatsFragment.newInstance(isWeekly);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.statsContainer, fragment);
        transaction.commit();
    }
}
