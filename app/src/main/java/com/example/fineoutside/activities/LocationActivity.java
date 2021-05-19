package com.example.fineoutside.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fineoutside.R;
import com.example.fineoutside.data.UserDetails;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("fine outside users");
    private final ActivityResultLauncher<String> permissionRequest =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) getLocationAndUpdateUser();
            else if (!getClass().getSimpleName().contains("MainMenu")) {
                Toast.makeText(this, "You can not use this feature without permit location", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    private boolean requestedPermission = false;

    @Override
    protected void onResume() {
        super.onResume();
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int permissionStatus = ContextCompat.checkSelfPermission(this, permission);
        if (permissionStatus != PackageManager.PERMISSION_GRANTED && !requestedPermission) {
            requestedPermission = true;
            permissionRequest.launch(permission);
        } else getLocationAndUpdateUser();
    }

    protected void initUI() {
        View hamburger_menu = findViewById(R.id.hamburger_menu);
        if (hamburger_menu != null) {
            ListPopupWindow listPopupWindow = new ListPopupWindow(this);
            listPopupWindow.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"Terms and conditions", "Privacy Policy", "Tutorial", "Contact Us", "About Us", "Settings", "Logout"}));
            listPopupWindow.setAnchorView(hamburger_menu);
            listPopupWindow.setWidth(650);
            listPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);
            listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
                listPopupWindow.dismiss();
                Intent intent = new Intent(this, InfoActivity.class);
                switch (position) {
                    case 0:
                        intent.putExtra("Title", "Terms and conditions");
                        intent.putExtra("Message", "This following document is to make sure that every user will know the right conditions if he wants to use this app.\n" +
                            "\n" +
                            "Ability to Accept. \n" +
                            "if you are using this app you affirm that you are going to act respectfully through all the other users in this app.\n" +
                            " \n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "orders :\n" +
                            "You can order services through this app.\n" +
                            "those services are ordering food with “ food delivery feature\n" +
                            "and order babysitting services through the “babysitting services “ feature.\n" +
                            "you are entitled to use any of the features of the app as you would like\n" +
                            "\n" +
                            "account:\n" +
                            "\n" +
                            "you need to sign-up to use the app.\n" +
                            "you need to choose a type of user for the account.\n" +
                            "you can delete the account any time.\n" +
                            "\n" +
                            "reports:\n" +
                            "you can report to out email\n" +
                            "fineoutsideapplication@gmail.com\n" +
                            "\n" +
                            "restrictions and rules\n" +
                            "communicate in a respectful manner\n" +
                            "don’t communicate with rude language \n" +
                            "you must approve your location");
                        startActivity(intent);
                        break;
                    case 1:
                        intent.putExtra("Title", "Privacy Policy");
                        intent.putExtra("Message", "This privacy policy governs how we, use, collect and store Personal Data we collect or receive from or about you  such as in the following use cases:\n" +
                            "\n" +
                            "1- you logged in to the app\n" +
                            "2- you contact us\n" +
                            "3 - you start chat with another user\n" +
                            "4- you want to order food\n" +
                            "5-you will create family profile\n" +
                            "6- you will contact us\n" +
                            "\n" +
                            "Of course we respect your privacy .\n" +
                            "\n" +
                            "Table of contents:\n" +
                            "1. What information we collect, why we collect it, and how it is used \n" +
                            "2. How we protect and retain your personal data \n" +
                            "3. How we share your personal data\n" +
                            "4. Additional information regarding transfers of personal data \n" +
                            "5. Your privacy rights \n" +
                            "6. Use by children \n" +
                            "7. Public information about your activity on the services \n" +
                            "8. How can I delete my account? \n" +
                            "9. Links to and interaction with third party products \n" +
                            "10. Log files \n" +
                            "11. Use of analytics tools \n" +
                            "12. How to contact us \n" +
                            "\n" +
                            "1. What information we collect, why we collect it, and how it is used \n" +
                            " what information:\n" +
                            "your email\n" +
                            "your phone number\n" +
                            "you personal information\n" +
                            "your family details\n" +
                            "your location\n" +
                            "\n" +
                            "why we collect it :\n" +
                            "to make the experience of the app the best it can be.\n" +
                            "\n" +
                            "how it is used:\n" +
                            "it used in the communications between the users that logged-in, and open the application on their phones.\n" +
                            "\n" +
                            "2. How we protect and retain your personal data \n" +
                            "all the information will be stored in a specific database on firebase platform.\n" +
                            "\n" +
                            "3. How we share your personal data\n" +
                            "we don’t share your personal information in the outside world. \n" +
                            "we do share your information with other users who are using this app just like you.\n" +
                            "\n" +
                            "4. Additional information regarding transfers of personal data\n" +
                            "no. there is no additional information that is been shared\n" +
                            "\n" +
                            "5.  Your privacy rights \n" +
                            "you can always contact us threw email.\n" +
                            "and you can always decide not tu use our app\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "6.use by children\n" +
                            "This app is appropriate for children use.\n" +
                            "There is no difference between adult use or child use.\n" +
                            "\n" +
                            "7.Public information about your activity on the services\n" +
                            "people around you and who use the app just as you, can see your activities in the app such as see your posts , see your messages and family profile.\n" +
                            "\n" +
                            "8. How can I delete my account?\n" +
                            "you can delete your account by clicking the “ delete account” button that is in the settings option .\n" +
                            "\n" +
                            "9.Links to and interaction with third party products\n" +
                            "there are no third parties as to now.\n" +
                            "\n" +
                            "10.Log files \n" +
                            "There is a main log file when you click in the logo on the main menu .\n" +
                            "In the log file there is some data and information that you can look at .\n" +
                            "\n" +
                            "11.Use of analytics tools\n" +
                            "We use the database in the firebase for analytics tools usage.\n" +
                            "\n" +
                            "12.how to contact us :\n" +
                            "there is a specific mail for this app\n" +
                            "the mail address is :\n" +
                            "fineoutsideapplication@gmail.com");
                        startActivity(intent);
                        break;
                    case 2:
                        intent.putExtra("Title", "Tutorial");
                        intent.putExtra("Message", "Hello. this is a tutorial for the this app.\n" +
                            "In this app there are 7 features, search engine and data system.\n" +
                            "\n" +
                            "search engine - to search and find a specific thing more quickly.\n" +
                            "you will see a search bar in the application\n" +
                            "\n" +
                            "data system - to inform the user with nice facts about the app.\n" +
                            "you can see the data when you click the logo on the main menu.\n" +
                            "\n" +
                            "the 7 features\n" +
                            "\n" +
                            "1 - parents community help.\n" +
                            "this is a feature that helps parents to help each other in a day to day situations.\n" +
                            "when you will click on that feature, you will see a list of requests from other users who needs help.\n" +
                            "you can publish a request by yourself and other people can see you issue as well.\n" +
                            "\n" +
                            "when you click on a request , a chat will open.you can talk, and even share location with google maps.\n" +
                            "\n" +
                            "in the end you if a user can document if people helped him or not.\n" +
                            "\n" +
                            "and you will see updates in the main menu if you miss some information.\n" +
                            "\n" +
                            "2 - emergency center \n" +
                            "this is just like parents community help, but only for emergencies.\n" +
                            "\n" +
                            "3 groups - \n" +
                            "in this feature you can create a group.\n" +
                            "you can write the name and the description of the group, and the group location.\n" +
                            "you can join or leave other groups as well, and of course see all the groups that you have joined to.\n" +
                            "\n" +
                            "4 - whiteboard.\n" +
                            "in the white board you can post or see other users posts by clicking that feature from the main menu.\n" +
                            "you need the write the subject and the description of the post, and in the before publishing it\n" +
                            "to choose the right groups that match that post.\n" +
                            "you can even delete a post the you yourself published if you see it as a necessity\n" +
                            "\n" +
                            "5 - meeting new people.\n" +
                            "the purpose of that feature is that people who have kids in certain age , who want to other people who also have kids in the same age , and never meet before.\n" +
                            "you only need to create a family profile , and other people can see your profile and of they want start a chat with you and share location.\n" +
                            "\n" +
                            "6 - order food.\n" +
                            "if you, or you and the people around you want to order food ,\n" +
                            "you can choose the “food delivery” option from the main menu.\n" +
                            "there are different kinds of foods for kids and adults.\n" +
                            "you just need to write your phone number, after that choose a business , and after that see the closest businesses in the area and choose the business that fits the most.\n" +
                            "\n" +
                            "If you are a business owner who want to sell on this app, you can open a business profile , give your location and good luck with the selling process.\n" +
                            "\n" +
                            "7 - babysitting services.\n" +
                            "if you need a babysitter or someone that will help you for a payment, you can absolutely do it by choosing the babysitting feature.\n" +
                            "as you click on that feature, you will see all the babysitters near you, and choose the one that you like the most and chat with him.");
                        startActivity(intent);
                        break;
                    case 3:
                        intent.putExtra("Title", "Contact Us");
                        intent.putExtra("Message", "contact us through email\n" +
                            "\n" +
                            "fineoutsideapplication@gmail.com");
                        startActivity(intent);
                        break;
                    case 4:
                        intent.putExtra("Title", "About Us");
                        intent.putExtra("Message", "FineOutside is an application that is very important for us to develop.\n" +
                            "it helps parents with their small children in public places.\n" +
                            "That application was designed  to create  a platform that can help the experience of the parents and their children as well.\n" +
                            "we hope you  will enjoy using our app");
                        startActivity(intent);
                        break;
                    case 5:
                        startActivity(new Intent(this, SettingsActivity.class));
                        break;
                    case 6:
                        auth.signOut();
                        Intent splashIntent = new Intent(this, SplashActivity.class);
                        startActivity(splashIntent);
                        finish();
                        break;
                }
            });
            hamburger_menu.setOnClickListener(v -> listPopupWindow.show());
        }
        ImageView feature_image = findViewById(R.id.feature_image);
        if (feature_image != null && getFeatureImage() != 0) {
            feature_image.setImageResource(getFeatureImage());
        }
    }

    private void getLocationAndUpdateUser() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    DatabaseReference ref = root.child(user.getUid());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            // Got last known location. In some rare situations this can be null.
                            if (userDetails != null && location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                getSharedPreferences("USER_LOCATION", MODE_PRIVATE).edit()
                                    .putString("Latitude", latitude + "")
                                    .putString("Longitude", longitude + "")
                                    .apply();
                                userDetails.setLatitude(latitude);
                                userDetails.setLongitude(longitude);
                            }
                            ref.setValue(userDetails);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        }
    }

    protected int getFeatureImage() {
        return 0;
    }
}
