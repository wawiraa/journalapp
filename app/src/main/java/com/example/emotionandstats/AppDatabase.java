package com.example.emotionandstats;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {JournalEntry.class, Prompt.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract PromptDao promptDao();
    public abstract JournalDao journalDao();

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static  synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "journal_db").addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            databaseWriteExecutor.execute(() -> {
                                PromptDao promptDao = AppDatabase.getInstance(context).promptDao();

                                try {
                                    InputStream inputStream = context.getResources().openRawResource(R.raw.prompts);
                                    Reader reader = new InputStreamReader(inputStream);
                                    Prompt[] prompts = new Gson().fromJson(reader, Prompt[].class);

                                    for (Prompt prompt : prompts) {
                                        promptDao.add(prompt);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    })
                    .build();
        }
        return instance;
    }
}
