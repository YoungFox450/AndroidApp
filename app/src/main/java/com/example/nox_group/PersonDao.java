package com.example.nox_group;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PersonDao {
    @Query("SELECT * FROM persons")
    LiveData<List<Person>> getAll();

    @Query("SELECT * FROM persons LIMIT 1")
    Person getAnyPerson();

    @Insert
    void insert(Person person);

    @Delete
    void delete(Person person);
}
