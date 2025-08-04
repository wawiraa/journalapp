package com.example.emotionandstats;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WritingStatsFragment extends Fragment {

    private TextView tvTotalJournals, tvTotalWords, tvPositiveCount, tvNegativeCount,
            tvScopeLabel, tvSampleMessage, tvSampleMessage1;

    private BarChart barCategoryChart;
    private PieChart pieSentimentSummary;

    private JournalDao journalDao;
    private boolean isWeekly = true;

    private static final List<String> POSITIVE_WORDS = Arrays.asList(
            "good", "happy", "great", "love", "joy", "awesome", "fantastic",
            "excellent", "amazing", "wonderful", "pleased", "delighted", "cheerful", "positive", "fortunate"
    );

    private static final List<String> NEGATIVE_WORDS = Arrays.asList(
            "bad", "sad", "hate", "angry", "terrible", "awful", "horrible",
            "upset", "unhappy", "disappointed", "worst", "negative", "miserable", "depressed"
    );

    public static WritingStatsFragment newInstance(boolean isWeekly) {
        WritingStatsFragment fragment = new WritingStatsFragment();
        Bundle args = new Bundle();
        args.putBoolean("isWeekly", isWeekly);
        fragment.setArguments(args);
        return fragment;
    }

    private void zeroOutTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private String classifySentiment(String content) {
        if (content == null || content.trim().isEmpty()) return "Neutral";

        String lower = content.toLowerCase(Locale.ROOT);
        String[] words = lower.replaceAll("[^a-zA-Z\\s]", "").split("\\s+");

        int positiveCount = 0;
        int negativeCount = 0;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            boolean negation = (i > 0 && "not".equals(words[i - 1]));

            if (POSITIVE_WORDS.contains(word)) {
                if (negation) negativeCount++;
                else positiveCount++;
            } else if (NEGATIVE_WORDS.contains(word)) {
                if (negation) positiveCount++;
                else negativeCount++;
            }
        }

        if (positiveCount > negativeCount) return "Positive";
        else if (negativeCount > positiveCount) return "Negative";
        else return "Neutral";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("isWeekly", isWeekly);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_writing_stats, container, false);

        tvTotalJournals = root.findViewById(R.id.tvTotalJournals);
        tvTotalWords = root.findViewById(R.id.tvTotalWords);
        tvPositiveCount = root.findViewById(R.id.tvPositiveCount);
        tvNegativeCount = root.findViewById(R.id.tvNegativeCount);
        tvScopeLabel = root.findViewById(R.id.tvScopeLabel);
        tvSampleMessage = root.findViewById(R.id.tvSampleMessage);
        tvSampleMessage1 = root.findViewById(R.id.tvSampleMessage1);
        barCategoryChart = root.findViewById(R.id.barCategoryChart);
        pieSentimentSummary = root.findViewById(R.id.pieSentimentSummary);

        if (savedInstanceState != null) {
            isWeekly = savedInstanceState.getBoolean("isWeekly", true);
        } else if (getArguments() != null) {
            isWeekly = getArguments().getBoolean("isWeekly", true);
        }

        tvScopeLabel.setText(isWeekly ? "Weekly" : "Monthly");

        journalDao = AppDatabase.getInstance(requireContext()).journalDao();

        // Observe LiveData for automatic updates
        journalDao.getAll().observe(getViewLifecycleOwner(), entries -> {
            if (entries == null || entries.isEmpty()) {
                tvSampleMessage.setVisibility(View.VISIBLE);
                tvSampleMessage1.setVisibility(View.VISIBLE);
                setupDummyPieChart();
                setupDummyBarChart();
                tvTotalJournals.setText("0");
                tvTotalWords.setText("0");
                tvPositiveCount.setText("0");
                tvNegativeCount.setText("0");
            } else {
                processEntries(entries);
                tvSampleMessage.setVisibility(View.GONE);
                tvSampleMessage1.setVisibility(View.GONE);
            }
        });

        return root;
    }

    private void processEntries(List<JournalEntry> entries) {

        long now = System.currentTimeMillis();
        long windowStart = isWeekly
                ? now - 7L * 24 * 60 * 60 * 1000L
                : now - 90L * 24 * 60 * 60 * 1000L;

        int totalJournals = 0;
        int totalWords = 0;
        int positive = 0, negative = 0;

        int buckets = isWeekly ? 7 : 13;
        int[] bucketEntryCounts = new int[buckets];
        int[] bucketSentimentPositive = new int[buckets];
        int[] bucketSentimentNegative = new int[buckets];
        int[] bucketSentimentNeutral = new int[buckets];

        Calendar baseCal = Calendar.getInstance();
        baseCal.setTimeInMillis(windowStart);
        zeroOutTime(baseCal);

        Calendar nowCal = Calendar.getInstance();
        zeroOutTime(nowCal);

        SimpleDateFormat fullIso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        SimpleDateFormat shortIso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        fullIso.setLenient(true);
        shortIso.setLenient(true);

        for (JournalEntry je : entries) {
            long entryMillis;
            try {
                entryMillis = fullIso.parse(je.date).getTime();
            } catch (Exception e) {
                try {
                    entryMillis = shortIso.parse(je.date).getTime();
                } catch (Exception ex) {
                    continue;
                }
            }
            if (entryMillis < windowStart) continue;

            totalJournals++;

            int wordCount = 0;
            if (je.content != null && !je.content.trim().isEmpty()) {
                wordCount = je.content.trim().split("\\s+").length;
            }
            totalWords += wordCount;

            String sentiment = classifySentiment(je.content);
            if ("Positive".equals(sentiment)) positive++;
            else if ("Negative".equals(sentiment)) negative++;

            Calendar entryCal = Calendar.getInstance();
            entryCal.setTimeInMillis(entryMillis);
            zeroOutTime(entryCal);

            long diffDays = (entryCal.getTimeInMillis() - baseCal.getTimeInMillis()) / (24L * 60 * 60 * 1000L);
            long todayIndex = (nowCal.getTimeInMillis() - baseCal.getTimeInMillis()) / (24L * 60 * 60 * 1000L);

            if (entryCal.after(nowCal) || entryCal.equals(nowCal)) {
                diffDays = todayIndex;
            }

            if (isWeekly) {
                int dayIndex = (int) diffDays;
                if (dayIndex < 0 || dayIndex >= buckets) continue;
                bucketEntryCounts[dayIndex]++;
                if ("Positive".equals(sentiment)) bucketSentimentPositive[dayIndex]++;
                else if ("Negative".equals(sentiment)) bucketSentimentNegative[dayIndex]++;
                else bucketSentimentNeutral[dayIndex]++;
            } else {
                int weekIndex = (int) (diffDays / 7);
                if (weekIndex < 0 || weekIndex >= buckets) continue;
                bucketEntryCounts[weekIndex]++;
                if ("Positive".equals(sentiment)) bucketSentimentPositive[weekIndex]++;
                else if ("Negative".equals(sentiment)) bucketSentimentNegative[weekIndex]++;
                else bucketSentimentNeutral[weekIndex]++;
            }
        }

        int skipped = 0;
        for (int i = 0; i < buckets; i++) {
            if (bucketEntryCounts[i] == 0) skipped++;
        }

        int finalTotalJournals = totalJournals;
        int finalTotalWords = totalWords;
        int finalPositive = positive;
        int finalNegative = negative;
        int finalSkipped = skipped;
        boolean hasAny = (finalTotalJournals > 0);

        List<PieEntry> finalSentEntries = new ArrayList<>();
        int neutralCount = finalTotalJournals - (finalPositive + finalNegative);

        if (hasAny) {
            if (finalPositive > 0) finalSentEntries.add(new PieEntry(finalPositive, "Positive"));
            if (finalNegative > 0) finalSentEntries.add(new PieEntry(finalNegative, "Negative"));
            if (neutralCount > 0) finalSentEntries.add(new PieEntry(neutralCount, "Neutral"));
        } else {
            finalSentEntries.add(new PieEntry(1f, "Sample"));
        }

        List<BarEntry> finalBars = new ArrayList<>();
        finalBars.add(new BarEntry(0, finalSkipped));
        finalBars.add(new BarEntry(1, computeNeutral(bucketEntryCounts, bucketSentimentPositive, bucketSentimentNegative)));
        finalBars.add(new BarEntry(2, finalPositive));
        finalBars.add(new BarEntry(3, finalNegative));

        requireActivity().runOnUiThread(() -> {
            tvTotalJournals.setText(String.valueOf(finalTotalJournals));
            tvTotalWords.setText(String.valueOf(finalTotalWords));
            tvPositiveCount.setText(String.valueOf(finalPositive));
            tvNegativeCount.setText(String.valueOf(finalNegative));

            setupSentimentPie(finalSentEntries, hasAny);

            BarDataSet set = new BarDataSet(finalBars, "");
            set.setColors(
                    Color.parseColor("#D461A6"),
                    Color.parseColor("#A2D2FF"),
                    Color.parseColor("#A50062"),
                    Color.parseColor("#D62900")
            );
            set.setValueTextSize(12f);

            BarData data = new BarData(set);
            data.setBarWidth(0.9f);
            barCategoryChart.setData(data);

            XAxis x = barCategoryChart.getXAxis();
            x.setGranularity(1f);
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setDrawGridLines(false);
            x.setDrawAxisLine(false);
            x.setLabelCount(4, true);
            final String[] labels = new String[]{"Skipped", "Neutral", "Positive", "Negative"};
            x.setValueFormatter(new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    int i = (int) value;
                    return (i >= 0 && i < labels.length) ? labels[i] : "";
                }
            });
            x.setTextColor(Color.BLACK);

            YAxis left = barCategoryChart.getAxisLeft();
            left.setAxisMinimum(0f);
            left.setGranularity(1f);
            left.setDrawGridLines(false);

            barCategoryChart.getAxisRight().setEnabled(false);
            barCategoryChart.getLegend().setEnabled(false);
            barCategoryChart.getDescription().setEnabled(false);
            barCategoryChart.setFitBars(true);

            barCategoryChart.invalidate();
        });
    }

    private float computeNeutral(int[] total, int[] pos, int[] neg) {
        int neutralSum = 0;
        for (int i = 0; i < total.length; i++) {
            int known = pos[i] + neg[i];
            int n = total[i] - known;
            if (n > 0) neutralSum += n;
        }
        return neutralSum;
    }

    private void setupSentimentPie(List<PieEntry> entries, boolean hasReal) {
        PieDataSet ds = new PieDataSet(entries, "");
        if (!hasReal) {
            ds.setColors(Color.LTGRAY);
        } else {
            List<Integer> colors = new ArrayList<>();
            for (PieEntry entry : entries) {
                switch (entry.getLabel()) {
                    case "Positive":
                        colors.add(Color.parseColor("#A50062"));
                        break;
                    case "Negative":
                        colors.add(Color.parseColor("#D62900"));
                        break;
                    case "Neutral":
                        colors.add(Color.parseColor("#A2D2FF"));
                        break;
                    default:
                        colors.add(Color.LTGRAY);
                }
            }
            ds.setColors(colors);
        }
        ds.setSliceSpace(3f);
        ds.setValueTextColor(Color.WHITE);
        ds.setValueTextSize(12f);

        PieData data = new PieData(ds);
        pieSentimentSummary.setData(data);
        pieSentimentSummary.getDescription().setEnabled(false);
        pieSentimentSummary.setDrawHoleEnabled(true);
        pieSentimentSummary.setHoleRadius(40f);
        pieSentimentSummary.setTransparentCircleRadius(45f);
        pieSentimentSummary.setUsePercentValues(true);
        pieSentimentSummary.setEntryLabelColor(Color.WHITE);
        pieSentimentSummary.getLegend().setEnabled(false);
        pieSentimentSummary.animateY(600);
        pieSentimentSummary.invalidate();
    }

    private void setupDummyPieChart() {
        List<PieEntry> dummyEntries = new ArrayList<>();
        dummyEntries.add(new PieEntry(1f, "Positive"));
        dummyEntries.add(new PieEntry(1f, "Negative"));
        dummyEntries.add(new PieEntry(3f, "Neutral"));

        PieDataSet dataSet = new PieDataSet(dummyEntries, "");
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#A50062"));
        colors.add(Color.parseColor("#D62900"));
        colors.add(Color.parseColor("#A2D2FF"));
        dataSet.setColors(colors);

        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);
        pieSentimentSummary.setData(pieData);
        pieSentimentSummary.getDescription().setEnabled(false);
        pieSentimentSummary.setDrawHoleEnabled(true);
        pieSentimentSummary.setHoleRadius(40f);
        pieSentimentSummary.setTransparentCircleRadius(45f);
        pieSentimentSummary.setUsePercentValues(true);
        pieSentimentSummary.setEntryLabelColor(Color.WHITE);
        pieSentimentSummary.getLegend().setEnabled(false);

        pieSentimentSummary.invalidate();
    }

    private void setupDummyBarChart() {
        List<BarEntry> dummyEntries = new ArrayList<>();

        dummyEntries.add(new BarEntry(0, 1f));
        dummyEntries.add(new BarEntry(1, 2f));
        dummyEntries.add(new BarEntry(2, 1f));
        dummyEntries.add(new BarEntry(3, 4f));

        BarDataSet dataSet = new BarDataSet(dummyEntries, "");
        dataSet.setColors(
                Color.parseColor("#D461A6"),
                Color.parseColor("#A2D2FF"),
                Color.parseColor("#A50062"),
                Color.parseColor("#D62900")
        );
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barCategoryChart.setData(barData);

        XAxis xAxis = barCategoryChart.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = barCategoryChart.getAxisLeft();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);

        barCategoryChart.getAxisRight().setEnabled(false);
        barCategoryChart.getLegend().setEnabled(false);
        barCategoryChart.getDescription().setEnabled(false);

        barCategoryChart.invalidate();
    }
}
