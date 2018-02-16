package pandakun.storageexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

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

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadImage();
            }
        });
    }

    private void uploadImage() {
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
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.w(LOG_TAG, "Upload failed: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(LOG_TAG, "Download Url: " + downloadUrl);
                buttonDownload.setEnabled(true);
            }
        });

    }

    private void downloadImage() {
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
                Log.d(LOG_TAG, "Download successful");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageviewResult.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.w(LOG_TAG, "Download failed: " + exception.getMessage());
            }
        });
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
