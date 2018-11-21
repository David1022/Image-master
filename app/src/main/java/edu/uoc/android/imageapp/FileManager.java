package edu.uoc.android.imageapp;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

public class FileManager {

    static final String FILE_NAME = "imageapp.jpg";
    static final String SAVING_PATH = "/UOCImageApp";

    public static boolean existsSavedPhoto() {
        String path = Environment.getExternalStorageDirectory().toString();
        path += SAVING_PATH;
        File dir = new File(path);
        File imageFile = new File(dir, FILE_NAME);

        return imageFile.exists();
    }

    public static boolean deletePhoto() {
        // TODO: Se debe ejecutar en una clase independiente
        String path = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(path + SAVING_PATH);
        myDir.mkdirs();
        File file = new File(myDir, FILE_NAME);
        if (file.exists()) {
            if (file.delete()) {
                return true;
            }
        }
        return false;
    }

    public static boolean savePhoto(Bitmap bm) {
        // TODO: Se debe ejecutar en una clase independiente
        boolean isSaved = false;
        String path = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(path + SAVING_PATH);
        myDir.mkdirs();
        File file = new File(myDir, FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            isSaved = true;
        } catch (Exception e) {
            isSaved = false;
        }

        return isSaved;
    }

    public static Bitmap getSavedFile() {
        String path = Environment.getExternalStorageDirectory().toString();
        path += SAVING_PATH;
        File dir = new File(path);
        File imageFile = new File(dir, FILE_NAME);
        if (imageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
            return bitmap;
        }
        return null;
    }

}
