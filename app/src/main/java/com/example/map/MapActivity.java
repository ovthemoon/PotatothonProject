package com.example.map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.File;
import java.util.HashMap;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, Overlay.OnClickListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private FusedLocationProviderClient locationClient;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private NaverMap naverMap;
    LatLng coord = new LatLng(37.5670135, 126.9783740);
    HashMap<Marker, String> markerToPhotoMap = new HashMap<>();

// 마커와 사진을 매핑

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        NaverMapSdk.getInstance(this).setClient(new NaverMapSdk.NaverCloudPlatformClient("oruxyhexoz"));
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            NaverMapOptions options = new NaverMapOptions().locationButtonEnabled(false);
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        latitudeTextView = findViewById(R.id.latitude);
        longitudeTextView = findViewById(R.id.longitude);
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();
        File imgFile = new File("/path/to/image.jpg");
        ImageView myImage = findViewById(R.id.image_view);
        Glide.with(this).load(imgFile).into(myImage);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);
            }
        });
        mapFragment.getMapAsync(this);


    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show();
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        Toast.makeText(this, "wellDone", Toast.LENGTH_SHORT).show();
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        if (this.naverMap != null) {
            UiSettings uiSettings = this.naverMap.getUiSettings();
            uiSettings.setLocationButtonEnabled(true);
        }

        // 마커와 위치 초기화
        Marker[] markers = new Marker[4];
        LatLng[] latlngs = new LatLng[]{
                new LatLng(37.8884473, 127.7365171),
                new LatLng(37.8865127, 127.7365171),
                new LatLng(37.8865107, 127.7928492),
                new LatLng(37.8965117, 127.7365181)

        };

        for (int i = 0; i < markers.length; i++) {
            markers[i] = new Marker();
            markers[i].setPosition(latlngs[i]);
            markers[i].setMap(naverMap);
            markers[i].setOnClickListener(this); // 마커 클릭 리스너 설정

            String tmpName = "trash" + (i + 1) ;
            markerToPhotoMap.put(markers[i], tmpName);
        }

        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
    }

    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        if (overlay instanceof Marker) {
            Marker clickedMarker = (Marker) overlay;
            String photoPath = markerToPhotoMap.get(clickedMarker);
            if (photoPath != null) {
                displayPhoto(photoPath); // 사진 표시
            }
            return true;
        }
        return false;
    }

    private void displayPhoto(String photoPath) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image);

        // dialog_image.xml 레이아웃에 있는 ImageView 찾기
        ImageView imageView = dialog.findViewById(R.id.image_view);
        int resourceId = getResources().getIdentifier(photoPath, "drawable", getPackageName());
        // 이미지 로드 및 표시 (예: Glide 라이브러리 사용)
        Glide.with(this)
                .load(resourceId)
                .into(imageView);

        // Dialog 표시
        dialog.show();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            latitudeTextView.setText("Latitude: " + latitude);
                            longitudeTextView.setText("Longitude: " + longitude);
                        }
                    }
                });
    }

}