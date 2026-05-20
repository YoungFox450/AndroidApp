package com.example.nox_group;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PersonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Gestion des marges pour tenir compte de la barre de navigation système
        ViewCompat.setOnApplyWindowInsetsListener(fabAdd, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + (int) (24 * getResources().getDisplayMetrics().density);
            params.rightMargin = systemBars.right + (int) (24 * getResources().getDisplayMetrics().density);
            v.setLayoutParams(params);
            return windowInsets;
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPersonActivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialisation de l'adapter avec une liste vide
        adapter = new PersonAdapter(new ArrayList<>(), person -> {
            Intent intent = new Intent(MainActivity.this, PersonDetailsActivity.class);
            intent.putExtra("person", person);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Observation des données via LiveData pour un affichage immédiat même au premier démarrage
        AppDatabase.getDatabase(this).personDao().getAll().observe(this, persons -> {
            if (persons != null) {
                adapter.updateData(persons);
            }
        });

        // Animation au scroll pour le FAB
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fabAdd.isExtended()) {
                    fabAdd.shrink();
                } else if (dy < 0 && !fabAdd.isExtended()) {
                    fabAdd.extend();
                }
            }
        });
    }
}
