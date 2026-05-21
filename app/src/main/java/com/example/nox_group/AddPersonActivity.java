package com.example.nox_group;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activité permettant d'ajouter une nouvelle personne à la base de données.
 * 
 * Conception optimisée pour le confort utilisateur :
 * - Gestion adaptative du clavier : Les champs remontent au-dessus de la saisie.
 * - Navigation système "solide" : Opaque et intégrée au thème sombre.
 * - Capture photo : Caméra frontale privilégiée et correction d'orientation.
 * - Code documenté : Chaque étape est expliquée pour faciliter la relecture.
 */
public class AddPersonActivity extends AppCompatActivity {

    // Éléments de l'interface graphique
    private TextInputEditText etNom, etPostNom, etPrenom, etDescription;
    private Spinner spSexe, spPromotion, spFiliere;
    private ImageView ivProfilePreview;
    private NestedScrollView nestedScrollView;
    
    // Chemin vers le fichier image capturé
    private String currentPhotoPath;

    // Gestionnaire pour la permission de la caméra
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "Permission caméra refusée. Impossible de prendre une photo.", Toast.LENGTH_SHORT).show();
                }
            });

    // Gestionnaire pour le résultat de la capture d'image
    private final ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Photo prise avec succès, affichage avec correction d'orientation
                    displayProfilePicture();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Activation du mode immersif Edge-to-Edge.
        // On force la couleur @color/bg_main (#121212) sur les barres système.
        int bgColor = ContextCompat.getColor(this, R.color.bg_main);
        EdgeToEdge.enable(this, SystemBarStyle.dark(bgColor), SystemBarStyle.dark(bgColor));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        // 2. Initialisation des composants et gestion adaptative
        initViews();
        setupAdaptiveLayout(); // Gère le clavier et la barre de navigation
        setupSpinners();

        // Restaurer la photo si l'activité a été recréée
        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString("currentPhotoPath");
            if (currentPhotoPath != null) {
                ivProfilePreview.post(this::displayProfilePicture);
            }
        }

        // Configuration de la barre d'outils
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Actions des boutons (Photo et Enregistrement)
        findViewById(R.id.fabTakePhoto).setOnClickListener(v -> checkPermissionAndTakePhoto());
        findViewById(R.id.btnSave).setOnClickListener(v -> validateAndSave());
    }

    /**
     * Relie les variables Java aux éléments définis dans le layout XML.
     */
    private void initViews() {
        etNom = findViewById(R.id.etNom);
        etPostNom = findViewById(R.id.etPostNom);
        etPrenom = findViewById(R.id.etPrenom);
        etDescription = findViewById(R.id.etDescription);
        spSexe = findViewById(R.id.spSexe);
        spPromotion = findViewById(R.id.spPromotion);
        spFiliere = findViewById(R.id.spFiliere);
        ivProfilePreview = findViewById(R.id.ivProfilePreview);
        nestedScrollView = findViewById(R.id.nestedScrollView);
    }

    /**
     * Gère intelligemment l'espace de l'écran :
     * - Empêche le texte de défiler "derrière" les boutons de navigation (barre solide).
     * - Fait remonter TOUS les champs de saisie au-dessus du clavier lors de la frappe.
     */
    private void setupAdaptiveLayout() {
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime()); // Hauteur du clavier

            // On ajuste le haut pour éviter de chevaucher la barre de statut
            v.setPadding(0, systemBars.top, 0, 0);

            // On ajuste le padding du bas pour que le contenu ne soit pas caché par le clavier ou la nav bar.
            // clipToPadding="true" dans le XML permet à ce padding d'être opaque (couleur bg_main).
            int bottomGap = Math.max(systemBars.bottom, ime.bottom);
            nestedScrollView.setPadding(0, 0, 0, bottomGap);

            return WindowInsetsCompat.CONSUMED;
        });

        // Appliquer l'auto-scroll intelligent sur chaque champ de saisie
        View[] inputFields = {etNom, etPostNom, etPrenom, etDescription};
        for (View field : inputFields) {
            field.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    // On attend que le clavier soit bien ouvert pour scroller
                    nestedScrollView.postDelayed(() -> {
                        // On cherche le TextInputLayout parent pour scroller sur l'ensemble du champ (titre + saisie)
                        View targetView = v;
                        if (v.getParent() instanceof ViewGroup && v.getParent().getParent() instanceof TextInputLayout) {
                            targetView = (View) v.getParent().getParent();
                        }
                        // Demande au système de faire défiler pour que le champ soit bien visible
                        // avec une marge de confort de 200 pixels au-dessus du clavier.
                        Rect rect = new Rect(0, 0, targetView.getWidth(), targetView.getHeight() + 200);
                        targetView.requestRectangleOnScreen(rect, false);
                    }, 400);
                }
            });
        }
    }

    /**
     * Configure les adaptateurs pour les listes déroulantes (Spinners).
     */
    private void setupSpinners() {
        setupSpinner(spSexe, R.array.sexe_choices);
        setupSpinner(spPromotion, R.array.level_choices);
        setupSpinner(spFiliere, R.array.filiere_choices);
    }

    private void setupSpinner(Spinner spinner, int arrayId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                arrayId, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Valide les informations et enregistre le profil dans Room.
     */
    private void validateAndSave() {
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty()) {
            Toast.makeText(this, "Le nom et le prénom sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        // Création de l'entité Person
        Person person = new Person(
                nom,
                etPostNom.getText().toString().trim(),
                prenom,
                spSexe.getSelectedItem().toString(),
                spPromotion.getSelectedItem().toString() + " " + spFiliere.getSelectedItem().toString(),
                etDescription.getText().toString().trim(),
                currentPhotoPath
        );

        // Insertion en base sur un thread secondaire
        new Thread(() -> {
            AppDatabase.getDatabase(this).personDao().insert(person);
            runOnUiThread(() -> {
                // Toast pour confirmation
                Toast.makeText(AddPersonActivity.this, "Un batrd a bien été ajoutéé", Toast.LENGTH_SHORT).show();
                // Fermeture différée pour laisser le temps de voir le toast
                nestedScrollView.postDelayed(this::finish, 1000);
            });
        }).start();
    }

    /**
     * Vérifie la permission Caméra avant de lancer la prise de photo.
     */
    private void checkPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Prépare et lance l'appareil photo en forçant la caméra frontale si possible.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Paramètres pour forcer l'ouverture de la caméra frontale
        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        takePictureIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
        takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        takePictureIntent.putExtra("camera_facing", "front");

        try {
            File photoFile = createImageFile();
            Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureLauncher.launch(takePictureIntent);
        } catch (IOException ex) {
            Toast.makeText(this, "Erreur lors de la création du fichier photo", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Crée un fichier image temporaire sécurisé.
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profiles");
        if (!storageDir.exists()) storageDir.mkdirs();
        File image = File.createTempFile("IMG_" + timeStamp, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Affiche l'image dans l'aperçu en corrigeant les bugs de rotation (EXIF).
     */
    private void displayProfilePicture() {
        if (currentPhotoPath != null) {
            ImageUtils.loadResizedAndRotatedImage(currentPhotoPath, ivProfilePreview);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Sauvegarde du chemin pour ne pas le perdre en cas de rotation d'écran
        outState.putString("currentPhotoPath", currentPhotoPath);
    }
}
