package branko.trifkovic.nutriento;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Permission;
import java.security.Permissions;

public class MainActivity extends AppCompatActivity {
    private static int CAMERA_PERMISSION_REQUEST_CODE = 100;
    Button buttonLoadImage;
    ImageView imageView;
    TextView caloriesTextView;
    TextView proteinTextView;
    TextView carbsTextView;
    TextView fatTextView;
    TextView classNameTextView;
    LinearLayout tableLayout;
    private Bitmap bitmap = null;
    private Module module = null;
    DbHelper dbHelper;
    private String DB_NAME = "macronutrients.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create database
        dbHelper = new DbHelper(this, DB_NAME, null, 1);

        // Find all visual and design objects
        buttonLoadImage = (Button) findViewById(R.id.button1);
        imageView = (ImageView) findViewById(R.id.imageView1);
        caloriesTextView = (TextView) findViewById(R.id.caloriesTextView);
        proteinTextView = (TextView) findViewById(R.id.proteinTextView);
        carbsTextView = (TextView) findViewById(R.id.carbsTextView);
        fatTextView = (TextView) findViewById(R.id.fatTextView);
        classNameTextView = (TextView) findViewById(R.id.classNameTextView);
        tableLayout = (LinearLayout) findViewById(R.id.tableLayout);

        // Set macronutrient table invisible
        tableLayout.setVisibility(View.INVISIBLE);

        // Set listener on "Take a picture" button
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                checkCameraPermission();
            }
        });

    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // You have the camera permission. You can proceed to use the camera.
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(i, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. You can use the camera.
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //This functions return the selected image from gallery
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK && null != data) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);

            detectObject();
        }


    }

    private void detectObject() {

        //Getting the image from the image view
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);

        try {
            //Read the image as Bitmap
            bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

            //Here we reshape the image into 224*224
            bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

            //Loading the model file.
            module = Module.load(assetFilePath(MainActivity.this, "model_mobile.pt"));

        } catch (IOException e) {
            Log.e("MainActivity", "Module error");
            finish();
        }

        //Input Tensor
        final Tensor input = TensorImageUtils.bitmapToFloat32Tensor(
                bitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
        );

        //Calling the forward of the model to run our input
        final Tensor output = module.forward(IValue.from(input)).toTensor();

        float[] score_arr = output.getDataAsFloatArray();

        // Preform softmax on scores
        Softmax s = new Softmax(score_arr);
        score_arr = s.softmax();

        // Fetch the index of the value with maximum score
        float max_score = -Float.MAX_VALUE;
        int class_id = -1;
        for (int i = 0; i < score_arr.length; i++) {
            Log.d("DetectionResults", Float.toString(score_arr[i]));
            if (score_arr[i] > max_score) {
                max_score = score_arr[i];
                class_id = i;
            }
        }


        // Print calories
        tableLayout.setVisibility(View.VISIBLE);
        if(max_score > 0.4) {
            ModelClass mc = dbHelper.readClass(class_id);

            classNameTextView.setText(mc.getmDescription());
            caloriesTextView.setText(Double.toString(mc.getmCalories()));
            proteinTextView.setText(Double.toString(mc.getmProteins()) + " grams");
            carbsTextView.setText(Double.toString(mc.getmCarbs()) + " grams");
            fatTextView.setText(Double.toString(mc.getmFats()) + " grams");
        } else {
            classNameTextView.setText("Class unknown");
            caloriesTextView.setText("");
            proteinTextView.setText("");
            carbsTextView.setText("");
            fatTextView.setText("");
        }


    }


    public static String assetFilePath(Context context, String fileName) throws IOException {
        File modelFile = new File(context.getFilesDir(), fileName);

        if(modelFile.exists() && modelFile.length() > 0){
            return modelFile.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(fileName)) {
            try (OutputStream os = new FileOutputStream(modelFile)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return modelFile.getAbsolutePath();
        }

    }

}