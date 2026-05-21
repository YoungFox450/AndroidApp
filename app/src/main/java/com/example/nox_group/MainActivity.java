package com.example.nox_group;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PersonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forcer le mode Edge-to-Edge avec la couleur bg_main et icônes claires
        int bgColor = ContextCompat.getColor(this, R.color.bg_main);
        EdgeToEdge.enable(this, 
            SystemBarStyle.dark(bgColor), // Status bar
            SystemBarStyle.dark(bgColor)  // Navigation bar
        );
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Ajuster les marges pour ne pas être caché par la barre de navigation
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0); // Padding latéral pour le contenu
            
            // Ajuster spécifiquement le FAB
            ViewGroup.MarginLayoutParams fabParams = (ViewGroup.MarginLayoutParams) fabAdd.getLayoutParams();
            fabParams.bottomMargin = systemBars.bottom + (int) (24 * getResources().getDisplayMetrics().density);
            fabAdd.setLayoutParams(fabParams);

            return windowInsets;
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPersonActivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PersonAdapter(new ArrayList<>(), person -> {
            Intent intent = new Intent(MainActivity.this, PersonDetailsActivity.class);
            intent.putExtra("person", person);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        AppDatabase.getDatabase(this).personDao().getAll().observe(this, persons -> {
            if (persons != null) {
                adapter.updateData(persons);
            }
        });

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
