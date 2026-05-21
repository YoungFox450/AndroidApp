package com.example.nox_group;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.Executors;

@Database(entities = {Person.class}, version = 3, exportSchema = false)
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
                    dao.insert(new Person("Mvula", "Keza", "Odrey", "Masculin",
                        "L2 Informatique Appliquée",
                        "Etudiant passionné par l'informatique et les nouvelle technologie, avec une formation electronique et embarquée et une experience dans le dev web. mal heuresement trop borné et avec un égo surdimensioné", "nox"));
                    
                    dao.insert(new Person("Milolo", "Bula", "Jael", "Féminin",
                        "L2 Informatique Appliquer",
                        "Étudiante brillante se spécialisant dans les réseaux.", null));
                    
                    dao.insert(new Person("Ngoy", "Tshilumba", "Patrick", "Masculin", 
                        "L3 Électronique", 
                        "Expert en maintenance d'appareils médicaux et microprocesseurs.", null));
                    
                    dao.insert(new Person("Kanyimba", "Lumbwe", "Alice", "Féminin", 
                        "Prepa Mécanique", 
                        "Nouvelle recrue avec une forte aptitude pour le dessin industriel.", null));
                    
                    dao.insert(new Person("Mukendi", "Kasongo", "David", "Masculin",
                        "L1 Aviation", 
                        "Rêve de devenir ingénieur de bord et travaille sur des simulateurs.", null));
                }
            });
        }
    };
}
