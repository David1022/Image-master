package edu.uoc.android.imageapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int DEGREES_TO_ROTATE = 270;
    // Request code
    private final int REQUEST_PERMISSION_STORAGE_SAVE = 101;
    private final int REQUEST_PERMISSION_STORAGE_DELETE = 102;
    private final int REQUEST_PERMISSION_STORAGE_OPEN = 103;
    // Views
    private Button buttonOpenImage;
    private ImageView imageView;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set views
        buttonOpenImage = findViewById(R.id.image_app_btn_capture);
        imageView = findViewById(R.id.image_app_iv_picture);
        tvMessage = findViewById(R.id.image_app_tv_message);

        // Set listeners
        buttonOpenImage.setOnClickListener(this);

        if (!hasPermissionsToWrite()) {
            // request permissions
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                              REQUEST_PERMISSION_STORAGE_OPEN);
        } else {
            // TODO(DONE): show the image from external storage if exists
            Bitmap bm = FileManager.getSavedFile();
            if (bm != null) {
                showImage(bm);
            } else {
                tvMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showImage(Bitmap bm) {
        if (shouldRotateImage(bm)) {
            bm = rotateImage(bm);
        }
        imageView.setImageBitmap(bm);
        tvMessage.setVisibility(View.GONE);
    }

    private boolean shouldRotateImage(Bitmap image) {
        return image.getHeight() > image.getWidth();
    }

    private Bitmap rotateImage(Bitmap image) {
        Matrix matrix = new Matrix();
        matrix.postRotate(DEGREES_TO_ROTATE);

        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }

    private void hideImage() {
        imageView.setImageBitmap(null);
        tvMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            onDeleteMenuTap();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            onSaveMenuTap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onDeleteMenuTap() {
        // check permissions
        if (!hasPermissionsToWrite()) {
            // request permissions
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                              REQUEST_PERMISSION_STORAGE_DELETE);
        } else {
            // TODO(DONE): show dialog if image file exists and delete the image if the user accepts
            showConfirmationDialog();
        }
    }

    private void showConfirmationDialog() {
        if (FileManager.existsSavedPhoto()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("¿Seguro que deseas borrar la foto?")
                    .setMessage("Si aceptas la foto se borrará definitivamente y no se podrá volver a recuperar")
                    .setPositiveButton("Borrar", getPositiveClickListener())
                    .setNegativeButton("Cancelar", getNegativeClickListener())
                    .create()
                    .show();
        } else {
            Toast.makeText(this, "No hay ninguna foto a para borrar", Toast.LENGTH_SHORT).show();
        }
    }

    private DialogInterface.OnClickListener getNegativeClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
    }

    private DialogInterface.OnClickListener getPositiveClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (FileManager.deletePhoto()) {
                    hideImage();
                    Toast.makeText(MainActivity.this, "Archivo borrado correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }


    private void onSaveMenuTap() {
        // check permissions
        if (!hasPermissionsToWrite()) {
            // request permissions
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                              REQUEST_PERMISSION_STORAGE_SAVE);
        } else {
            // TODO(DONE): save the image if image is displayed
            if (FileManager.savePhoto(((BitmapDrawable)imageView.getDrawable()).getBitmap())) {
                Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean hasPermissionsToWrite() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onClick(View v) {
        if (v == buttonOpenImage) {
            // TODO(DONE): launch an intent to get an image in thumbnail from camera app
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ((requestCode == REQUEST_IMAGE_CAPTURE) && (resultCode == RESULT_OK)) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            showImage(imageBitmap);
        } else if (!FileManager.existsSavedPhoto()) {
            tvMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_STORAGE_DELETE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // show dialog if image file exists
                    // TODO(DONE): show dialog if image file exists and delete the image if the user accepts
                    showConfirmationDialog();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO(DONE): show toast message
                    Toast.makeText(this, "Error al borrar: Para poder borrar el archivo debe " +
                            "autorizar permisos de escritura en disco", Toast.LENGTH_SHORT).show();

                }
            }
            case REQUEST_PERMISSION_STORAGE_SAVE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // save the image file
                    // TODO(DONE): save the image if image is displayed
                    FileManager.savePhoto(((BitmapDrawable)imageView.getDrawable()).getBitmap());
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO(DONE): show toast message
                    Toast.makeText(this, "Error al guardar: Para poder guardar el archivo debe " +
                            "autorizar permisos de escritura en disco", Toast.LENGTH_SHORT).show();
                }
            }
            case REQUEST_PERMISSION_STORAGE_OPEN: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO(DONE): show the image from external storage if exists
                    Bitmap bm = FileManager.getSavedFile();
                    if (bm != null) {
                        showImage(bm);
                    } else {
                        tvMessage.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
}
