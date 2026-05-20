package com.example.nox_group;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.Executors;

@Database(entities = {Person.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PersonDao personDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "person_database")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                PersonDao dao = INSTANCE.personDao();
                
                if (dao.getAnyPerson() == null) {
                    dao.insert(new Person("Mvula", "Keza", "Odrey", "M",
                        "L2 Informatique Appliquée",
                        "Un mec chill, surper intello, personne ne lui arrive à la cheville, un baiseur née, ultra puissant", "nox"));
                    
                    dao.insert(new Person("Mwamba", "Kanyinda", "Marie", "F", 
                        "L2 Électricité", 
                        "Étudiante brillante se spécialisant dans la domotique et les réseaux.", null));
                    
                    dao.insert(new Person("Ngoy", "Tshilumba", "Patrick", "M", 
                        "L3 Électronique", 
                        "Expert en maintenance d'appareils médicaux et microprocesseurs.", null));
                    
                    dao.insert(new Person("Kanyimba", "Lumbwe", "Alice", "F", 
                        "Prepa Mécanique", 
                        "Nouvelle recrue avec une forte aptitude pour le dessin industriel.", null));
                    
                    dao.insert(new Person("Mukendi", "Kasongo", "David", "M", 
                        "L1 Aviation", 
                        "Rêve de devenir ingénieur de bord et travaille sur des simulateurs.", null));
                }
            });
        }
    };
}
