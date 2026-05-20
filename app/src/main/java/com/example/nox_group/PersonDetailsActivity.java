package com.example.nox_group;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;

public class PersonDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    // Image capturée (chemin absolu)
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ivProfilePicture.setImageBitmap(myBitmap);
                } else {
                    // Image par défaut (nom de ressource dans drawable)
                    int resId = getResources().getIdentifier(person.imagePath, "drawable", getPackageName());
                    if (resId != 0) {
                        ivProfilePicture.setImageResource(resId);
                    }
                }
            }

            btnDelete.setOnClickListener(v -> {
                new Thread(() -> {
                    AppDatabase.getDatabase(PersonDetailsActivity.this).personDao().delete(person);
                    
                    // Optionnel : Supprimer le fichier image s'il s'agit d'une photo prise
                    if (person.imagePath != null) {
                        File fileToDelete = new File(person.imagePath);
                        if (fileToDelete.exists() && person.imagePath.contains(getPackageName())) {
                            fileToDelete.delete();
                        }
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(PersonDetailsActivity.this, "Un batard as bien été supprimé", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }).start();
            });
        }

        btnBack.setOnClickListener(v -> finish());
    }
}
