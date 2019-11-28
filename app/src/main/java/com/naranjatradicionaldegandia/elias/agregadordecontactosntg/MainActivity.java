package com.naranjatradicionaldegandia.elias.agregadordecontactosntg;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;
import static com.naranjatradicionaldegandia.elias.agregadordecontactosntg.Contacto.addContacto;

public class MainActivity extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfiguration;
    public String correoNuevo;
    private Contacto contacto;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        final Context context = this.getBaseContext();



//---timer


        //--Contacto objeto
        contacto = new Contacto("Alvaro Junaaa", "666555766","a@a");
        //--


        if (!(ActivityCompat.checkSelfPermission(context,
                WRITE_CONTACTS)
                == PackageManager.PERMISSION_GRANTED)) {
            insertDummyContactWrapper();
        }




        FloatingActionButton fab = findViewById(R.id.fab);


            fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){  Thread thread = new Thread(){
                    public void run(){
                        if(ServicioAgregador.estaEncendido){
                            ServicioAgregador.estaEncendido = false;
                            stopService(new Intent(context, ServicioAgregador.class));
                        } else {
                            ServicioAgregador.estaEncendido = true;
                            startService(new Intent(context, ServicioAgregador.class));
                        }

                      }
                };

        thread.start();
        }});
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);











    }





    private void insertDummyContactWrapper() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsNeeded = new ArrayList<String>();

            final List<String> permissionsList = new ArrayList<String>();
            if (!addPermission(permissionsList, Manifest.permission.INTERNET))
                permissionsNeeded.add("Internet.");

            if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
                permissionsNeeded.add("Leer contactos.");
            if (!addPermission(permissionsList, Manifest.permission.WRITE_CONTACTS))
                permissionsNeeded.add("Crear contactos.");

            if (permissionsList.size() > 0) {
                if (permissionsNeeded.size() > 0) {
                    // Need Rationale
                    String message = "Necesitas añadir permisos para " + permissionsNeeded.get(0);
                    for (int i = 1; i < permissionsNeeded.size(); i++)
                        message = message + ", " + permissionsNeeded.get(i);
                    showMessageOKCancel(message,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                return;
            }
        }
        startApp();

    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
                // Check for Rationale Option
                if (!shouldShowRequestPermissionRationale(permission))
                    return false;
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Vale", okListener)
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.INTERNET, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_NETWORK_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED

                        && perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    startApp();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Has rechazado algunos permisos, por favor. Aceptalos todos para poder continuar.", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void startApp(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
