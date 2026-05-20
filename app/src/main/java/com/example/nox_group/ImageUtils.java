package com.example.nox_group;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.widget.ImageView;

import java.io.IOException;

public class ImageUtils {

    /**
     * Charge une image depuis un chemin, la redimensionne pour économiser de la mémoire
     * et la pivote correctement selon les métadonnées EXIF.
     */
    public static void loadResizedAndRotatedImage(String photoPath, ImageView imageView) {
        if (photoPath == null || photoPath.isEmpty()) return;

        // 1. Obtenir les dimensions de l'ImageView
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Si l'ImageView n'est pas encore mesurée, on utilise des valeurs par défaut raisonnables
        if (targetW <= 0) targetW = 1024;
        if (targetH <= 0) targetH = 1024;

        // 2. Obtenir les dimensions de l'image originale
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // 3. Déterminer le facteur de réduction
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

        // 4. Décoder l'image avec le facteur de réduction
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        if (bitmap == null) return;

        // 5. Gérer la rotation EXIF
        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

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
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }

            imageView.setImageBitmap(rotatedBitmap);
            
            // Si on a créé un nouveau bitmap pour la rotation, on peut recycler l'ancien
            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }

        } catch (IOException e) {
            e.printStackTrace();
            imageView.setImageBitmap(bitmap);
        }
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}
