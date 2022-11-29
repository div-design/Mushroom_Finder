package com.example.mushroommanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AllMushroomsMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnMarkerDragListener {

    GoogleMap mGoogleMap;
    private FloatingActionButton btnLocation,mushroomLocation;
    private FusedLocationProviderClient mLocationClient;
    private Marker marker;
    double lat, lng;
    String mushroomAddress;
    liveMapDataModel viewModel;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map_all_mushrooms, container, false);
        btnLocation = (FloatingActionButton) view.findViewById(R.id.btnLocation);
        mushroomLocation =(FloatingActionButton) view.findViewById(R.id.mushroomLocation);
        initMap();


        mLocationClient = new FusedLocationProviderClient(getActivity());

        mushroomLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ExploreTickets.class);
                startActivity(intent);
            }
        });
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getCurrentLoc();
            }
        });


       /* SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.MY_MAP);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(latLng.latitude + " KG " + latLng.longitude);
                        googleMap.clear();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));
                        googleMap.addMarker(markerOptions);
                    }
                });
            }
        });*/
        return view;
    }


    private void initMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.ALL_MUSHROOMS_MAP);
        supportMapFragment.getMapAsync(this);
        putAllMarker();
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLoc() {
        mLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Location location = task.getResult();
                gotoLocation(location.getLatitude(), location.getLongitude());
            }
        });


    }

    private void gotoLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(latitude, longitude));
        markerOptions.title("mushroom");



        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        mGoogleMap.animateCamera(cameraUpdate);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }

    private void putAllMarker(){
        db.collection("Tickets")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                               MarkerOptions markerOptions = new MarkerOptions();
                               markerOptions.position(new LatLng(document.getDouble("mapLat"), document.getDouble("mapLng")));
                               markerOptions.title(document.getString("mushroomType"));
                               mGoogleMap.addMarker(markerOptions);
                            }
                        } else {
                            Toast.makeText(getActivity(), "something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void putMarker(double latitude, double longitude, String name) {

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(new LatLng(latitude, longitude));
        markerOptions.title(name);
        mGoogleMap.clear();
        lat = latitude;
        lng = longitude;
        mGoogleMap.addMarker(markerOptions);
        String data = latitude + "," + longitude;
        viewModel = new ViewModelProvider(requireActivity()).get(liveMapDataModel.class);
        viewModel.setData(data);

        /*Intent intent = new Intent(getActivity(),CreateTicket.class);
        intent.putExtra("fullAddress", getAddress(latitude, longitude));
        intent.putExtra("latitude",lat);
        intent.putExtra("longitude",lng);
        startActivity(intent);*/
        Toast.makeText(getActivity(), getAddress(latitude, longitude), Toast.LENGTH_SHORT).show();

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

        /*mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                putMarker(latLng.latitude, latLng.longitude);
            }
        });
*/
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        lat = marker.getPosition().latitude;
        lng = marker.getPosition().longitude;

        Toast.makeText(getActivity(), "Lat: " + lat + " ,Lng: " + lng, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    public String getAddress(double lat, double lng) {
        String address = "lat: " + lat + ", lng: " + lng;
        List<Address> addresses = new ArrayList<Address>();
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
           /* Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();*/

        } catch (IOException e) {
            Toast.makeText(getActivity(), "Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (addresses.size() > 0) {
            Address obj = addresses.get(0);
            address = obj.getAddressLine(0);

        }

        return address;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(liveMapDataModel.class);

    }
}