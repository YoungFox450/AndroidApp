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

/**
 * Activité principale de l'application.
 * Affiche la liste des membres du groupe et permet d'accéder à l'ajout de nouveaux profils.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PersonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Activation du mode Edge-to-Edge pour une immersion totale sous les barres système.
        // On définit la couleur de fond et le style (icônes blanches sur fond sombre).
        int bgColor = ContextCompat.getColor(this, R.color.bg_main);
        EdgeToEdge.enable(this, 
            SystemBarStyle.dark(bgColor), 
            SystemBarStyle.dark(bgColor)
        );
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bouton flottant pour ajouter une nouvelle personne
        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Gestion dynamique des marges (Insets) pour éviter que le contenu ne soit caché par
        // la barre de navigation système ou les coins arrondis.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // On applique un padding latéral et supérieur (pour la barre de statut)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            
            // On ajuste spécifiquement la marge basse du bouton flottant pour qu'il reste au-dessus de la barre de navigation
            ViewGroup.MarginLayoutParams fabParams = (ViewGroup.MarginLayoutParams) fabAdd.getLayoutParams();
            fabParams.bottomMargin = systemBars.bottom + (int) (24 * getResources().getDisplayMetrics().density);
            fabAdd.setLayoutParams(fabParams);

            return windowInsets;
        });

        // Navigation vers l'écran d'ajout
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPersonActivity.class);
            startActivity(intent);
        });

        // Configuration de la liste (RecyclerView)
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialisation de l'adaptateur avec un écouteur de clic pour voir les détails d'un profil
        adapter = new PersonAdapter(new ArrayList<>(), person -> {
            Intent intent = new Intent(MainActivity.this, PersonDetailsActivity.class);
            intent.putExtra("person", person); // Passage de l'objet Person via Serializable
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Observation des données de la base de données Room via LiveData.
        // Dès qu'une personne est ajoutée ou supprimée, la liste se met à jour automatiquement.
        AppDatabase.getDatabase(this).personDao().getAll().observe(this, persons -> {
            if (persons != null) {
                adapter.updateData(persons);
            }
        });

        // Animation du bouton flottant (réduction/extension) lors du défilement de la liste
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fabAdd.isExtended()) {
                    fabAdd.shrink(); // Réduit au scroll vers le bas
                } else if (dy < 0 && !fabAdd.isExtended()) {
                    fabAdd.extend(); // S'étend au scroll vers le haut
                }
            }
        });
    }
}
