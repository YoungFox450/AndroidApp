package com.example.nox_group;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;

public class PersonDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuration forcée des barres système
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.bg_main));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_main));

        // Forcer les icônes blanches (Status Bar et Navigation Bar)
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        setContentView(R.layout.activity_person_details);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        ImageView ivProfilePicture = findViewById(R.id.ivProfilePicture);
        TextView tvFullName = findViewById(R.id.tvFullName);
        TextView tvSexe = findViewById(R.id.tvSexe);
        TextView tvClasse = findViewById(R.id.tvClasse);
        TextView tvDescription = findViewById(R.id.tvDescription);
        MaterialButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnDelete = findViewById(R.id.btnDelete);

        Person person = (Person) getIntent().getSerializableExtra("person");

        if (person != null) {
            tvFullName.setText(getString(R.string.full_name_format, person.prenom, person.nom, person.postNom));
            tvSexe.setText(person.sexe);
            tvClasse.setText(person.classe);
            tvDescription.setText(person.description);

            if (person.imagePath != null && !person.imagePath.isEmpty()) {
                File imgFile = new File(person.imagePath);
                if (imgFile.exists()) {
                    ivProfilePicture.post(() -> ImageUtils.loadResizedAndRotatedImage(person.imagePath, ivProfilePicture));
                } else {
                    int resId = getResources().getIdentifier(person.imagePath, "drawable", getPackageName());
                    if (resId != 0) {
                        ivProfilePicture.setImageResource(resId);
                    }
                }
            }

            btnDelete.setOnClickListener(v -> {
                new Thread(() -> {
                    AppDatabase.getDatabase(PersonDetailsActivity.this).personDao().delete(person);
                    
                    if (person.imagePath != null) {
                        File fileToDelete = new File(person.imagePath);
                        if (fileToDelete.exists() && person.imagePath.contains(getPackageName())) {
                            fileToDelete.delete();
                        }
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(PersonDetailsActivity.this, "Un batard a été supprimé", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }).start();
            });
        }

        btnBack.setOnClickListener(v -> finish());
    }
}
