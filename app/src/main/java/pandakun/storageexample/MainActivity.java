package pandakun.storageexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    String LOG_TAG = MainActivity.class.getSimpleName();

    Button buttonUpload, buttonDownload;
    RadioGroup radioGroup;
    ImageView imageviewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonUpload = findViewById(R.id.upload_button);
        buttonDownload = findViewById(R.id.download_button);
        radioGroup = findViewById(R.id.radio_group);
        imageviewResult = findViewById(R.id.resultant_imageview);

        buttonUpload.setOnClickListener(view -> {
//            uploadImageByBytes();
            uploadImageByStream();
//            uploadImageByFile();
        });

        buttonDownload.setOnClickListener(view -> {
            // Comment and uncomment as needed
//            downloadImageByBytes();
            downloadImageByFile();
        });
    }

    private void uploadImageByBytes() {
        // Start by getting our StorageReference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();
        StorageReference bearRef = rootRef.child("images/bear.jpg");

        // Get the data from the image as bytes
        ImageView bearImage = getSelectedBearImage();
        bearImage.setDrawingCacheEnabled(true);
        bearImage.buildDrawingCache();
        Bitmap bitmap = bearImage.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload it to our reference
        UploadTask uploadTask = bearRef.putBytes(data);
        buttonDownload.setEnabled(false);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Log.e(LOG_TAG, "Upload image by bytes failed: " + exception.getMessage());
        }).addOnSuccessListener(taskSnapshot -> {
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
            Log.d(LOG_TAG, "Upload image by bytes successful: " + taskSnapshot.getDownloadUrl());
            buttonDownload.setEnabled(true);
        });

    }

    private void uploadImageByStream() {
        // Start by getting our StorageReference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();
        StorageReference bearRef = rootRef.child("images/bear.jpg");

        // Get the image bitmap
        ImageView bearImage = getSelectedBearImage();
        bearImage.setDrawingCacheEnabled(true);
        bearImage.buildDrawingCache();
        Bitmap bitmap = bearImage.getDrawingCache();

        // Get the input stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(bitmapdata);

        // Upload the stream to storage
        bearRef.putStream(bis).addOnSuccessListener(taskSnapshot -> {
            Log.d(LOG_TAG, "Upload image by stream successful: " + taskSnapshot.getDownloadUrl());
            buttonDownload.setEnabled(true);
        }).addOnFailureListener(e -> Log.e(LOG_TAG, "Upload image by stream failed: " + e.getMessage()));

    }

    private void uploadImageByFile() {
        // Start by getting our StorageReference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();
        StorageReference bearRef = rootRef.child("images/bear.jpg");

        // Prepare a file to write into
        File file = new File(getCacheDir(), "bear.jpg");
        try {
            file.createNewFile();

            // Get the image bitmap
            ImageView bearImage = getSelectedBearImage();
            bearImage.setDrawingCacheEnabled(true);
            bearImage.buildDrawingCache();
            Bitmap bitmap = bearImage.getDrawingCache();

            // Compress the bitmap into a file
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();

            // Write the bytes into the file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

            // Upload the file to Storage
            bearRef.putFile(Uri.fromFile(file)).addOnSuccessListener(taskSnapshot -> {
                Log.d(LOG_TAG, "Upload image by file successful: " + taskSnapshot.getDownloadUrl());
                buttonDownload.setEnabled(true);
            }).addOnFailureListener(e -> Log.e(LOG_TAG, "Upload image by file failed: " + e.getMessage()));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void downloadImageByBytes() {
        // Start by getting a reference to the same location we uploaded to
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();
        StorageReference bearRef = rootRef.child("images/bear.jpg");

        // Download our data with a max allocation of 1MB
        final long ONE_MEGABYTE = 1024 * 1024;
        bearRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert bytes to bitmap and call setImageBitmap
                Log.d(LOG_TAG, "Download by bytes successful");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageviewResult.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e(LOG_TAG, "Download by bytes failed: " + exception.getMessage());
            }
        });
    }

    private void downloadImageByFile() {
        // Start by getting a reference to the same location we uploaded to
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();
        StorageReference bearRef = rootRef.child("images/bear.jpg");

        // Create a new temporary file
        try {
            File outputFile = File.createTempFile("prefix", "extension", getCacheDir());
            // Download the image into the file
            bearRef.getFile(outputFile).addOnSuccessListener(taskSnapshot -> {
                // Image is now in the file
                Log.d(LOG_TAG, "Download by file successful");
                imageviewResult.setImageURI(Uri.fromFile(outputFile));
            }).addOnFailureListener(e -> Log.e(LOG_TAG, "Download by file failed: " + e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ImageView getSelectedBearImage() {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radio1:
                return findViewById(R.id.image_bear1);
            case R.id.radio2:
                return findViewById(R.id.image_bear2);
            case R.id.radio3:
                return findViewById(R.id.image_bear3);
            case R.id.radio4:
                return findViewById(R.id.image_bear4);
            default:
                return findViewById(R.id.image_bear1);
        }
    }

}
