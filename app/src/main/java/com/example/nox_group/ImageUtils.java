package com.example.nox_group;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Classe utilitaire pour la manipulation d'images.
 * Gère le chargement, le redimensionnement et la correction d'orientation des photos.
 */
public class ImageUtils {

    /**
     * Charge une image depuis un chemin local, l'optimise pour la mémoire (downsampling)
     * et applique les rotations nécessaires basées sur les métadonnées EXIF.
     *
     * @param photoPath Chemin absolu du fichier image sur le disque.
     * @param imageView L'ImageView dans laquelle afficher l'image traitée.
     */
    public static void loadResizedAndRotatedImage(String photoPath, ImageView imageView) {
        if (photoPath == null || photoPath.isEmpty()) return;

        // 1. Obtenir les dimensions cibles (celles de la vue d'affichage)
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Si la vue n'est pas encore mesurée, on utilise une taille par défaut sécurisée
        if (targetW <= 0) targetW = 1024;
        if (targetH <= 0) targetH = 1024;

        // 2. Analyser le fichier sans charger les données de pixels en mémoire
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // 3. Calculer le facteur de réduction (inSampleSize)
        // Permet de ne pas charger une image de 12MP dans une vue de 500px
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

        // 4. Charger l'image réelle avec le facteur de réduction
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        if (bitmap == null) return;

        // 5. Gérer la rotation et les effets de miroir via les tags EXIF
        // Indispensable car beaucoup d'appareils enregistrent les photos "physiquement" de travers
        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Bitmap rotatedBitmap;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    rotatedBitmap = flipImage(bitmap, true, false);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    rotatedBitmap = flipImage(bitmap, false, true);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    rotatedBitmap = rotateImage(flipImage(bitmap, true, false), 270);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    rotatedBitmap = rotateImage(flipImage(bitmap, true, false), 90);
                    break;
                default:
                    rotatedBitmap = bitmap;
            }

            imageView.setImageBitmap(rotatedBitmap);
            
            // Libérer la mémoire du bitmap original s'il a été transformé (copié)
            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }

        } catch (IOException e) {
            e.printStackTrace();
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * Effectue une rotation de l'image.
     */
    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Effectue un effet miroir (horizontal ou vertical).
     */
    private static Bitmap flipImage(Bitmap source, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}
