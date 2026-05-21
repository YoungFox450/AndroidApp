package com.example.nox_group;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddPersonActivity extends AppCompatActivity {

    private TextInputEditText etNom, etPostNom, etPrenom, etDescription;
    private Spinner spSexe, spPromotion, spFiliere;
    private ImageView ivProfilePreview;
    private String currentPhotoPath;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setPic();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Forcer la couleur des barres système et les icônes blanches
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.bg_main));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_main));

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(false); // Icônes claires
        controller.setAppearanceLightNavigationBars(false); // Icônes claires

        setContentView(R.layout.activity_add_person);

        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString("currentPhotoPath");
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etNom = findViewById(R.id.etNom);
        etPostNom = findViewById(R.id.etPostNom);
        etPrenom = findViewById(R.id.etPrenom);
        etDescription = findViewById(R.id.etDescription);
        spSexe = findViewById(R.id.spSexe);
        spPromotion = findViewById(R.id.spPromotion);
        spFiliere = findViewById(R.id.spFiliere);
        ivProfilePreview = findViewById(R.id.ivProfilePreview);
        FloatingActionButton fabTakePhoto = findViewById(R.id.fabTakePhoto);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        if (currentPhotoPath != null) {
            ivProfilePreview.post(this::setPic);
        }

        ArrayAdapter<CharSequence> adapterSexe = ArrayAdapter.createFromResource(this,
                R.array.sexe_choices, R.layout.spinner_item);
        adapterSexe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSexe.setAdapter(adapterSexe);

        ArrayAdapter<CharSequence> adapterPromotion = ArrayAdapter.createFromResource(this,
                R.array.level_choices, R.layout.spinner_item);
        adapterPromotion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPromotion.setAdapter(adapterPromotion);

        ArrayAdapter<CharSequence> adapterFiliere = ArrayAdapter.createFromResource(this,
                R.array.filiere_choices, R.layout.spinner_item);
        adapterFiliere.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiliere.setAdapter(adapterFiliere);

        fabTakePhoto.setOnClickListener(v -> checkPermissionAndTakePhoto());

        btnSave.setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String postNom = etPostNom.getText().toString().trim();
            String prenom = etPrenom.getText().toString().trim();
            String sexe = spSexe.getSelectedItem() != null ? spSexe.getSelectedItem().toString() : "";
            
            String level = spPromotion.getSelectedItem() != null ? spPromotion.getSelectedItem().toString() : "";
            String filiere = spFiliere.getSelectedItem() != null ? spFiliere.getSelectedItem().toString() : "";
            String fullClasse = level + " " + filiere;
            
            String description = etDescription.getText().toString().trim();

            if (nom.isEmpty() || prenom.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            Person person = new Person(nom, postNom, prenom, sexe, fullClasse, description, currentPhotoPath);
            
            new Thread(() -> {
                AppDatabase.getDatabase(AddPersonActivity.this).personDao().insert(person);
                runOnUiThread(() -> {
                    Toast.makeText(AddPersonActivity.this, "Le profil a bien été créé", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentPhotoPath", currentPhotoPath);
    }

    private void checkPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        takePictureIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
        takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        takePictureIntent.putExtra("camera_facing", "front");
        
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Erreur lors de la création du fichier", Toast.LENGTH_SHORT).show();
        }
        
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureLauncher.launch(takePictureIntent);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profiles");
        
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        if (currentPhotoPath != null) {
            ImageUtils.loadResizedAndRotatedImage(currentPhotoPath, ivProfilePreview);
        }
    }
}
