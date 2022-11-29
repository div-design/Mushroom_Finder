package com.example.mushroommanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class CreateTicket extends AppCompatActivity {

    private static final int PICK_IMAGE = 101;
    private static final int CAM_PERM_CODE = 102;
    private static final int CAMERA_REQ_CODE = 103;

    String userId, image1 = "", image2 = "", currentDate, fullAddress, fullLatLng;
    Double lat, lng;
    String uniqueID = UUID.randomUUID().toString();

    TextView mushroomLocation;
    ImageButton photoCreateBtn;

    Set<String> reduceList = new HashSet<String>();
    HashMap<String, Object> ticketData = new HashMap<>();
    ImageView mushroomImage1, mushroomImage2;
    Button cancelBtn;
    Button saveBtn;
    Button uploadBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseFirestore db;

    FloatingActionButton imageDelete1, imageDelete2;


    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private ProgressDialog mLoadingBar;
    AutoCompleteTextView mushroomType;

    ArrayList<Uri> imageList = new ArrayList<Uri>();
    private Uri ImageUri1, ImageUri2;

    private liveMapDataModel viewModell;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ticket);


        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        imageDelete1 = findViewById(R.id.imageDelete1);
        imageDelete2 = findViewById(R.id.imageDelete2);
        photoCreateBtn = findViewById(R.id.photoCreateBtn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        db = FirebaseFirestore.getInstance();

        mushroomImage1 = findViewById(R.id.mushroomImage1);
        mushroomImage2 = findViewById(R.id.mushroomImage2);
        mushroomLocation = findViewById(R.id.mushroomLocation);
        //autocompleteType
        mushroomType = findViewById(R.id.inputMushroomType);




        //view model logic
        viewModell = new ViewModelProvider(this).get(liveMapDataModel.class);
        viewModell.getLiveMapData().observe(this, data -> {
            fullLatLng = data;
            lat = Double.parseDouble(fullLatLng.split(",")[0]);
            lng = Double.parseDouble(fullLatLng.split(",")[1]);
            fullAddress = getAddress(lat, lng);
        });


        imageDelete1.setVisibility(View.GONE);
        imageDelete2.setVisibility(View.GONE);

        Fragment fragment = new MapCreateFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
        mLoadingBar = new ProgressDialog(CreateTicket.this);


        checkPermission();
        autoCorrectTextviewData();

        //BUTTONS
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        photoCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCamreaPermission();
            }
        });

        imageDelete1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUri1 = null;
                Picasso.get().load(R.drawable.ticket_upload_placeholder).into(mushroomImage1);
                imageDelete1.setVisibility(View.GONE);
            }
        });

        imageDelete2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUri2 = null;
                Picasso.get().load(R.drawable.ticket_upload_placeholder).into(mushroomImage1);
                imageDelete2.setVisibility(View.GONE);

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fStore.collection("Tickets").document(uniqueID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
                Intent intent = new Intent(CreateTicket.this, UserDashboardActivity.class);
                startActivity(intent);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTicket();
            }
        });
    }






    private void checkPermission() {


    }
    private void listInSuccessSet(Set<String> list){
        reduceList.addAll(list);
    }

    private void autoCorrectTextviewData(){

        InputStream inputStream = getResources().openRawResource(R.raw.gombafajtak);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line="";
        Set<String> mushrooms = new HashSet<>();
        String[] record = null;
        while (true){
            try {
                if (!((line = reader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            mushrooms.add(line.replace("\n","").replace("-","").replace(" ",""));
        }
        reduceList.addAll(mushrooms);
        String autoData[] = new String[reduceList.size()];
        reduceList.toArray(autoData);
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1,autoData);
        mushroomType.setThreshold(1);
        mushroomType.setAdapter(adapter);

        /*fStore.collection("Tickets").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotsList = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot: snapshotsList){
                    String str = snapshot.getString("mushroomType");
                    temp.add(str);
                    listInSuccessSet(temp);
                }
                String autoData[] = new String[reduceList.size()];
                reduceList.toArray(autoData);
                for (String alma: autoData) {
                    System.out.println(alma);
                }
                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1,autoData);
                mushroomType.setThreshold(1);
                mushroomType.setAdapter(adapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateTicket.this, "onFailure", Toast.LENGTH_SHORT).show();
            }
        });*/


      //  return autoData;
    }

    private void createTicket() {
        userId = fAuth.getCurrentUser().getUid();
        Intent intentMaps = getIntent();
        DocumentReference documentReference = fStore.collection("Users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                String uid = fAuth.getUid();
                // String uniqueID = UUID.randomUUID().toString();

                ArrayList<String> refuseList = new ArrayList<String>();
                ArrayList<String> confirmList = new ArrayList<String>();


                ticketData.put("creatorID", uid);
                ticketData.put("mushroomType", mushroomType.getText().toString().toLowerCase());
                ticketData.put("dateCreated", getDate());
                ticketData.put("confirmList", confirmList);
                ticketData.put("refuseList", refuseList);
                ticketData.put("fullAddress", fullAddress);
                ticketData.put("mapLat", lat);
                ticketData.put("mapLng", lng);


                ticketData.put("creatorName", value.getString("username"));

                db.collection("Tickets").document(uniqueID).set(ticketData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                mLoadingBar.dismiss();
                                Toast.makeText(CreateTicket.this, "Ticket Created", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CreateTicket.this, UserDashboardActivity.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mLoadingBar.dismiss();
                                Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        return map.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private String getDate() {
        String part1, part2;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        part1 = formatter.format(date).toString().split(" ")[0];
        part2 = formatter.format(date).toString().split(" ")[2];
        currentDate = part1 + " " + part2;
        return currentDate;

    }

    @Override
    public void onBackPressed() {

        fStore.collection("Tickets").document(uniqueID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
        Intent intent = new Intent(CreateTicket.this, UserDashboardActivity.class);
        startActivity(intent);
    }

    public String getAddress(double lat, double lng) {
        String address = "lat: " + lat + ", lng: " + lng;
        List<Address> addresses = new ArrayList<Address>();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

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
            Toast.makeText(this, "Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (addresses.size() > 0) {
            Address obj = addresses.get(0);
            address = obj.getAddressLine(0);

        }

        return address;
    }

    public void askCamreaPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAM_PERM_CODE);
        } else {
            openCamera();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAM_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera Permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQ_CODE);
    }

    private Uri ConverterBitToUri(Bitmap image, Context context) {

        File imagesFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        File file;
        if (ImageUri1 == null) {
            file = new File(imagesFolder, "captured_image.jpg");
        } else {
            file = new File(imagesFolder, "captured_image2.jpg");
        }
        try {
            imagesFolder.mkdirs();


            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.example.mushroommanager" + ".provider", file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // CAMERA MAKE PIC
        if (requestCode == CAMERA_REQ_CODE) {

            if (data != null) {

                Bitmap image = (Bitmap) data.getExtras().get("data");

                if (ImageUri1 == null) {

                    ImageUri1 = ConverterBitToUri(image, this);
                    Picasso.get().load(ImageUri1).resize(500, 500).into(mushroomImage1);
                    imageDelete1.setVisibility(View.VISIBLE);

                    StorageReference file1 = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(ImageUri1));

                    file1.putFile(ImageUri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            file1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    image1 = uri.toString();
                                    ticketData.put("image1", image1);
                                    db.collection("Tickets").document(uniqueID).set(ticketData)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    mLoadingBar.dismiss();

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    mLoadingBar.dismiss();
                                                    Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    Toast.makeText(CreateTicket.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                else if (ImageUri2 == null) {
                    ImageUri2 = ConverterBitToUri(image, this);
                    Picasso.get().load(ImageUri2).resize(500, 500).into(mushroomImage2);
                    imageDelete2.setVisibility(View.VISIBLE);

                    StorageReference file2 = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(ImageUri2));

                    file2.putFile(ImageUri2).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            file2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    image2 = uri.toString();
                                    ticketData.put("image2", image2);
                                    db.collection("Tickets").document(uniqueID).set(ticketData)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    mLoadingBar.dismiss();
                                                    Toast.makeText(CreateTicket.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    mLoadingBar.dismiss();
                                                    Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    Toast.makeText(CreateTicket.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                else {
                    Toast.makeText(this, "You can choose only 2 image.", Toast.LENGTH_SHORT).show();
                }
            }
        }


        // UPLOAD PIC FROM GALLERY

        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.getClipData() != null) {
                        int countClipData = data.getClipData().getItemCount();

                        if (countClipData > 2) {
                            Toast.makeText(this, "You can choose only 2 image.", Toast.LENGTH_SHORT).show();
                        }


                        if (ImageUri1 == null && ImageUri2 == null) {
                            ImageUri1 = data.getClipData().getItemAt(0).getUri();
                            ImageUri2 = data.getClipData().getItemAt(1).getUri();
                            imageDelete1.setVisibility(View.VISIBLE);
                            imageDelete2.setVisibility(View.VISIBLE);

                            Picasso.get().load(ImageUri1).resize(500, 500).into(mushroomImage1);
                            Picasso.get().load(ImageUri2).resize(500, 500).into(mushroomImage2);

                            StorageReference file1 = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(ImageUri1));
                            StorageReference file2 = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(ImageUri2));

                            file1.putFile(ImageUri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    file1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            image1 = uri.toString();
                                            ticketData.put("image1", image1);
                                            db.collection("Tickets").document(uniqueID).set(ticketData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            mLoadingBar.dismiss();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            mLoadingBar.dismiss();
                                                        }
                                                    });
                                            Toast.makeText(CreateTicket.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            file2.putFile(ImageUri2).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    file2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            image2 = uri.toString();
                                            ticketData.put("image2", image2);
                                            db.collection("Tickets").document(uniqueID).set(ticketData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            mLoadingBar.dismiss();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            mLoadingBar.dismiss();

                                                        }
                                                    });
                                            Toast.makeText(CreateTicket.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        else if (ImageUri1 != null && ImageUri2 == null) {

                            ImageUri2 = data.getClipData().getItemAt(0).getUri();
                            Picasso.get().load(ImageUri2).resize(500, 500).into(mushroomImage1);
                            StorageReference file2 = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(ImageUri2));
                            imageDelete2.setVisibility(View.VISIBLE);

                            file2.putFile(ImageUri2).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    file2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            image2 = uri.toString();
                                            ticketData.put("image2", image2);
                                            db.collection("Tickets").document(uniqueID).set(ticketData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            mLoadingBar.dismiss();

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            mLoadingBar.dismiss();
                                                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            Toast.makeText(CreateTicket.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else if (ImageUri1 == null && ImageUri2 != null) {
                            ImageUri1 = data.getClipData().getItemAt(0).getUri();
                            Picasso.get().load(ImageUri1).resize(500, 500).into(mushroomImage1);
                            StorageReference file1 = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(ImageUri1));
                            imageDelete1.setVisibility(View.VISIBLE);

                            file1.putFile(ImageUri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    file1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            image1 = uri.toString();
                                            ticketData.put("image1", image1);
                                            db.collection("Tickets").document(uniqueID).set(ticketData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            mLoadingBar.dismiss();

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            mLoadingBar.dismiss();
                                                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            Toast.makeText(CreateTicket.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else {
                            Toast.makeText(this, "You can choose only 2 image.", Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        if (ImageUri1 == null) {
                            ImageUri1 = data.getData();
                            Picasso.get().load(ImageUri1).resize(500, 500).into(mushroomImage1);
                            StorageReference file1 = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(ImageUri1));
                            imageDelete1.setVisibility(View.VISIBLE);
                            file1.putFile(ImageUri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    file1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            image1 = uri.toString();
                                            ticketData.put("image1", image1);
                                            db.collection("Tickets").document(uniqueID).set(ticketData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            mLoadingBar.dismiss();

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            mLoadingBar.dismiss();
                                                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            Toast.makeText(CreateTicket.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        else if (ImageUri2 == null) {

                            ImageUri2 = data.getData();
                            Picasso.get().load(ImageUri2).resize(500, 500).into(mushroomImage2);
                            StorageReference file2 = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(ImageUri2));
                            imageDelete2.setVisibility(View.VISIBLE);
                            file2.putFile(ImageUri2).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    file2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            image2 = uri.toString();
                                            ticketData.put("image2", image2);
                                            db.collection("Tickets").document(uniqueID).set(ticketData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            mLoadingBar.dismiss();

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            mLoadingBar.dismiss();
                                                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            Toast.makeText(CreateTicket.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateTicket.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else {
                            Toast.makeText(this, "You can choose only 2 image.", Toast.LENGTH_SHORT).show();
                        }
                    }


                }
            }

        }

    }
}