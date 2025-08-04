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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.*;

public class MoodsStatsFragment extends Fragment {

    private static final String ARG_IS_WEEKLY = "is_weekly";

    private boolean isWeekly = true;

    private PieChart pieChart;
    private LineChart lineChart;
    private BarChart barChart;
    private TextView tvSamplePie, tvSampleLine, tvSampleBar;

    private EmotionsDao emotionsDao;

    private final Map<String, Integer> moodColorMap = new LinkedHashMap<String, Integer>() {{
        put("Great", Color.parseColor("#0c925f"));
        put("Good", Color.parseColor("#046c1e"));
        put("Okay", Color.parseColor("#114992"));
        put("Bad", Color.parseColor("#b27115"));
        put("Awful", Color.parseColor("#af0404"));
    }};
    private final Map<String, Integer> moodScoreMap = new LinkedHashMap<String, Integer>() {{
        put("Great", 5); put("Good", 4); put("Okay", 3); put("Bad", 2); put("Awful", 1);
    }};

    public MoodsStatsFragment() { }

    public static MoodsStatsFragment newInstance(boolean isWeekly) {
        MoodsStatsFragment fragment = new MoodsStatsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_WEEKLY, isWeekly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isWeekly = getArguments().getBoolean(ARG_IS_WEEKLY, true);
        }
        emotionsDao = EmotionsDatabase.getInstance(requireContext().getApplicationContext()).emotionsDao();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moods_stats, container, false);
        pieChart = view.findViewById(R.id.pieChartMoodCounts);
        lineChart = view.findViewById(R.id.lineChartMoodTrend);
        barChart = view.findViewById(R.id.barChartAverageMood);

        tvSamplePie = addSampleLabelAbove(pieChart);
        tvSampleLine = addSampleLabelAbove(lineChart);
        tvSampleBar = addSampleLabelAbove(barChart);

        observeData();
        return view;
    }

    private TextView addSampleLabelAbove(View chart) {
        ViewGroup parent = (ViewGroup) chart.getParent();
        int idx = parent.indexOfChild(chart);
        TextView tv = new TextView(getContext());
        tv.setText("Sample, Actual Statistics will be populated when there is enough data :)");
        tv.setTextColor(Color.DKGRAY);
        tv.setTextSize(14f);
        tv.setPadding(0, 0, 0, 10);
        tv.setVisibility(View.GONE);
        parent.addView(tv, idx);
        return tv;
    }

    private void observeData() {
        emotionsDao.getAllSortedByTimestamp().observe(getViewLifecycleOwner(), new Observer<List<Emotions>>() {
            @Override
            public void onChanged(List<Emotions> emotions) {
                if (emotions == null || emotions.size() < 2) {
                    showDummyCharts();
                } else {
                    List<Emotions> filtered = new ArrayList<>();
                    for (Emotions e : emotions) {
                        if (moodColorMap.containsKey(e.mainMood)) filtered.add(e);
                    }
                    showRealCharts(filtered);
                }
            }
        });
    }

    private void showSample(TextView tvSample, View chart, boolean showSample) {
        tvSample.setVisibility(showSample ? View.VISIBLE : View.GONE);
        chart.setVisibility(showSample ? View.INVISIBLE : View.VISIBLE);
    }

    private void showDummyCharts() {
        showSample(tvSamplePie, pieChart, true);
        showSample(tvSampleLine, lineChart, true);
        showSample(tvSampleBar, barChart, true);
        setupDummyPieChart();
        setupDummyLineChart();
        setupDummyBarChart();
    }

    private void setupDummyPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (String mood : moodColorMap.keySet()) {
            entries.add(new PieEntry(1, mood));
            colors.add(moodColorMap.get(mood));
        }
        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(colors);
        PieData data = new PieData(ds);
        pieChart.setData(data);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Sample Mood Counts");
        pieChart.setCenterTextSize(15f);
        pieChart.invalidate();
    }

    private void setupDummyLineChart() {
        List<Entry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        List<String> weekdayLabels = getWeekdayLabelsMondayToSunday();
        for (int i = 0; i < weekdayLabels.size(); i++) {
            String mood = "Okay";
            entries.add(new Entry(i, moodScoreMap.get(mood)));
            colors.add(moodColorMap.get(mood));
        }
        LineDataSet ds = new LineDataSet(entries, "");
        ds.setDrawCircles(true);
        ds.setCircleColors(colors);
        ds.setCircleRadius(8f);
        ds.setColor(Color.LTGRAY);
        ds.setLineWidth(3f);
        ds.setDrawValues(false);
        ds.setDrawCircleHole(false);
        lineChart.setData(new LineData(ds));
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setLabelCount(weekdayLabels.size());
        lineChart.getXAxis().setValueFormatter(new FixedWeekdayFormatter(weekdayLabels));
        lineChart.getAxisRight().setEnabled(false);
        lineChart.invalidate();
    }

    private void setupDummyBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        if (isWeekly) {
            for (int i = 0; i < 7; i++) {
                entries.add(new BarEntry(i, 3));
            }
        } else {
            for (int i = 0; i < 4; i++) {
                entries.add(new BarEntry(i, 3));
            }
        }
        BarDataSet ds = new BarDataSet(entries, "Average Mood");
        ds.setColor(Color.parseColor("#046c1e"));
        ds.setDrawValues(false); // remove value numbers inside bars
        BarData data = new BarData(ds);

        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    private void showRealCharts(List<Emotions> emotions) {
        showSample(tvSamplePie, pieChart, false);
        showSample(tvSampleLine, lineChart, false);
        showSample(tvSampleBar, barChart, false);

        Map<String, Integer> moodCounts = new LinkedHashMap<>();
        for (String mood : moodColorMap.keySet()) moodCounts.put(mood, 0);
        for (Emotions e : emotions) if (moodCounts.containsKey(e.mainMood)) moodCounts.put(e.mainMood, moodCounts.get(e.mainMood) + 1);
        buildPieChart(moodCounts);

        if (isWeekly) {
            buildLineChartWeeklyFixed(emotions);
            buildBarChartWeeklyAverage(emotions);
        } else {
            buildLineChartMonthly(emotions);
            buildBarChartMonthlyAverage(emotions);
        }
    }

    private void buildPieChart(Map<String, Integer> counts) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > 0) {
                entries.add(new PieEntry(e.getValue(), e.getKey()));
                colors.add(moodColorMap.getOrDefault(e.getKey(), Color.GRAY));
            }
        }
        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(colors);
        PieData pieData = new PieData(ds);
        pieChart.setData(pieData);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Mood Counts");
        pieChart.setCenterTextSize(15f);
        pieChart.invalidate();
    }

    private void buildLineChartWeeklyFixed(List<Emotions> emotions) {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offsetDays = (dayOfWeek + 5) % 7;
        cal.add(Calendar.DAY_OF_YEAR, -offsetDays);
        long weekStart = cal.getTimeInMillis();

        List<List<Emotions>> moodsByDayIndex = new ArrayList<>();
        for (int i = 0; i < 7; i++) moodsByDayIndex.add(new ArrayList<>());

        for (Emotions e : emotions) {
            long dayStart = getStartOfDayLocal(e.timestamp, tz);
            int dayIndex = (int)((dayStart - weekStart) / (24L * 60 * 60 * 1000));
            if (dayIndex >= 0 && dayIndex < 7) moodsByDayIndex.get(dayIndex).add(e);
        }

        List<Entry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        List<String> weekdayLabels = getWeekdayLabelsMondayToSunday();

        for (int i = 0; i < 7; i++) {
            List<Emotions> dayMoods = moodsByDayIndex.get(i);
            float avg = averageMoodScore(dayMoods);
            String domMood = dominantMood(dayMoods);
            entries.add(new Entry(i, avg));
            int circleColor = dayMoods.isEmpty() ? Color.GRAY : moodColorMap.getOrDefault(domMood, Color.LTGRAY);
            colors.add(circleColor);
        }

        LineDataSet ds = new LineDataSet(entries, "");
        ds.setDrawCircles(true);
        ds.setCircleColors(colors);
        ds.setCircleRadius(8f);
        ds.setColor(Color.LTGRAY);
        ds.setLineWidth(3f);
        ds.setDrawValues(false);
        ds.setDrawCircleHole(false);

        lineChart.setData(new LineData(ds));
        lineChart.getXAxis().setValueFormatter(new FixedWeekdayFormatter(weekdayLabels));
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setLabelCount(7);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    private void buildBarChartWeeklyAverage(List<Emotions> emotions) {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offsetDays = (dayOfWeek + 5) % 7;
        cal.add(Calendar.DAY_OF_YEAR, -offsetDays);
        long weekStart = cal.getTimeInMillis();

        List<List<Emotions>> moodsByDayIndex = new ArrayList<>();
        for (int i = 0; i < 7; i++) moodsByDayIndex.add(new ArrayList<>());

        for (Emotions e : emotions) {
            long dayStart = getStartOfDayLocal(e.timestamp, tz);
            int dayIndex = (int)((dayStart - weekStart) / (24L * 60 * 60 * 1000));
            if (dayIndex >= 0 && dayIndex < 7) moodsByDayIndex.get(dayIndex).add(e);
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = getWeekdayLabelsMondayToSunday();

        for (int i = 0; i < 7; i++) {
            float avg = averageMoodScore(moodsByDayIndex.get(i));
            entries.add(new BarEntry(i, avg));
        }

        BarDataSet ds = new BarDataSet(entries, "Avg Mood");
        ds.setColor(Color.parseColor("#046c1e"));
        ds.setDrawValues(false);

        BarData data = new BarData(ds);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getXAxis().setValueFormatter(new FixedWeekdayFormatter(labels));
        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    private void buildLineChartMonthly(List<Emotions> emotions) {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String mood : moodColorMap.keySet()) counts.put(mood, 0);

        for (Emotions e : emotions) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(e.timestamp);
            if (c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month) {
                if (counts.containsKey(e.mainMood)) counts.put(e.mainMood, counts.get(e.mainMood) + 1);
            }
        }

        List<Entry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            entries.add(new Entry(idx, e.getValue()));
            colors.add(moodColorMap.getOrDefault(e.getKey(), Color.LTGRAY));
            xLabels.add(e.getKey());
            idx++;
        }

        LineDataSet ds = new LineDataSet(entries, "");
        ds.setDrawCircles(true);
        ds.setCircleColors(colors);
        ds.setCircleRadius(8f);
        ds.setColor(Color.LTGRAY);
        ds.setLineWidth(3f);
        ds.setDrawValues(false);
        ds.setDrawCircleHole(false);

        lineChart.setData(new LineData(ds));
        lineChart.getXAxis().setValueFormatter(new FixedWeekdayFormatter(xLabels));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setLabelCount(xLabels.size());
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    private void buildBarChartMonthlyAverage(List<Emotions> emotions) {
        Map<String, List<Emotions>> monthGroups = new LinkedHashMap<>();

        SimpleDateFormat monthYear = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        for (Emotions e : emotions) {
            Date d = new Date(e.timestamp);
            String key = monthYear.format(d);
            if (!monthGroups.containsKey(key)) monthGroups.put(key, new ArrayList<>());
            monthGroups.get(key).add(e);
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, List<Emotions>> entry : monthGroups.entrySet()) {
            float avg = averageMoodScore(entry.getValue());
            entries.add(new BarEntry(index, avg));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet ds = new BarDataSet(entries, "Avg Mood");
        ds.setColor(Color.parseColor("#046c1e"));
        ds.setDrawValues(false);

        BarData data = new BarData(ds);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getXAxis().setValueFormatter(new FixedWeekdayFormatter(labels));
        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    private long getStartOfDayLocal(long timestamp, TimeZone tz) {
        Calendar cal = Calendar.getInstance(tz);
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private float averageMoodScore(List<Emotions> moods) {
        int sum = 0, count = 0;
        for (Emotions e : moods) {
            Integer score = moodScoreMap.get(e.mainMood);
            if (score != null) {
                sum += score;
                count++;
            }
        }
        return count == 0 ? 0f : ((float) sum) / count;
    }

    private String dominantMood(List<Emotions> moods) {
        Map<String, Integer> countMap = new LinkedHashMap<>();
        for (Emotions e : moods) countMap.put(e.mainMood, countMap.getOrDefault(e.mainMood, 0) + 1);

        String dominant = "Okay";
        int maxCount = -1;
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominant = entry.getKey();
            }
        }
        return dominant;
    }

    private List<String> getWeekdayLabelsMondayToSunday() {
        String[] weekdays = new SimpleDateFormat("EEE", Locale.getDefault())
                .getDateFormatSymbols().getShortWeekdays();
        List<String> labels = new ArrayList<>();
        labels.add(weekdays[Calendar.MONDAY]);
        labels.add(weekdays[Calendar.TUESDAY]);
        labels.add(weekdays[Calendar.WEDNESDAY]);
        labels.add(weekdays[Calendar.THURSDAY]);
        labels.add(weekdays[Calendar.FRIDAY]);
        labels.add(weekdays[Calendar.SATURDAY]);
        labels.add(weekdays[Calendar.SUNDAY]);
        return labels;
    }

    private static class FixedWeekdayFormatter extends ValueFormatter {
        private final List<String> labels;

        FixedWeekdayFormatter(List<String> labels) {
            this.labels = labels;
        }

        @Override
        public String getFormattedValue(float value) {
            int idx = Math.round(value);
            if (idx >= 0 && idx < labels.size()) {
                return labels.get(idx);
            }
            return "";
        }
    }
}
