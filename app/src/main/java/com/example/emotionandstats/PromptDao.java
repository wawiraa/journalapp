package com.example.emotionandstats;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PromptDao {
    @Query("SELECT*FROM prompt")
    List<Prompt> getAll();

    @Query("SELECT * FROM prompt ORDER BY RANDOM() LIMIT 1")
    Prompt getRandomPrompt  ();


    @Query("SELECT * FROM prompt WHERE id = :id LIMIT 1")
    Prompt getPromptById(int id);

    @Insert
    void add(Prompt prompt);


    @Insert
    void addAll(List<Prompt> prompts);



}
