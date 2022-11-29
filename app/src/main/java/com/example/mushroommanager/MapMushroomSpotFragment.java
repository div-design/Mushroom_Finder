package com.example.mushroommanager;


import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapMushroomSpotFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnMarkerDragListener{

    GoogleMap mGoogleMap;
    private FloatingActionButton btnLocation,mushroomLocation;
    private FusedLocationProviderClient mLocationClient;
    private Marker marker;
    double lat, lng;
    String mushroomAddress;
    liveMapDataModel viewModel;
    FirebaseFirestore fStore;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map_finding_spot,container,false);
        btnLocation = (FloatingActionButton) view.findViewById(R.id.btnLocation);
        mushroomLocation =(FloatingActionButton) view.findViewById(R.id.mushroomLocation);
        String actualTicketID = getArguments().getString("actualTicketID");
        mLocationClient = new FusedLocationProviderClient(getActivity());
        fStore= FirebaseFirestore.getInstance();
        initMap();
        DocumentReference documentReference = fStore.collection("Tickets").document(actualTicketID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                lat = value.getDouble("mapLat");
                lng = value.getDouble("mapLng");
                gotoLocation(lat,lng);
                putMarker(lat,lng);
            }
        });




        mushroomLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference documentReference = fStore.collection("Tickets").document(actualTicketID);
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        lat = value.getDouble("mapLat");
                        lng = value.getDouble("mapLng");
                        gotoLocation(lat,lng);
                        putMarker(lat,lng);
                    }
                });
            }
        });



        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLoc();

            }
        });







        return view;

    }

    private void initMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.MUSHROOM_SPOT_MAP);
        supportMapFragment.getMapAsync(this);
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

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        mGoogleMap.animateCamera(cameraUpdate);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }

    private void putMarker(double latitude, double longitude) {

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(new LatLng(latitude, longitude));
        markerOptions.title("mushroom");
        mGoogleMap.clear();
        lat = latitude;
        lng = longitude;
        mGoogleMap.addMarker(markerOptions);

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

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

            }
        });

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


}