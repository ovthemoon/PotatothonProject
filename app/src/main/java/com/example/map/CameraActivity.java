package com.example.map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import okhttp3.*;
public class CameraActivity extends AppCompatActivity {

    private Button btn_picture;
    private ImageView imageView;
    private static final int REQUEST_IMAGE_CODE=101;
    int value=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        takePicture();
    }
    public void takePicture(){
        Intent imageTakeIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(imageTakeIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(imageTakeIntent,REQUEST_IMAGE_CODE);
        }
    }
    public void onActivityResult(int requestCode,int resultCode,@Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQUEST_IMAGE_CODE&&resultCode==RESULT_OK){
            Bundle extras=data.getExtras();
            Bitmap imageBitmap=(Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

        }


    }
    public void uploadFile(String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    File file = new File(filePath);
                    RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file);

                    // URL 및 요청 구성
                    String url = "https://your-object-storage-endpoint/your-bucket/" + file.getName();
                    Request request = new Request.Builder()
                            .url(url)
                            .put(requestBody)
                            .build();

                    // 요청 실행
                    try (Response response = client.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        }
                        // 성공적인 응답 처리
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // 오류 처리
                }
            }
        }).start();
    }



}