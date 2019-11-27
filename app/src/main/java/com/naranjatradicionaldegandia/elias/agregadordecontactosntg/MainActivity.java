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
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;
import static com.naranjatradicionaldegandia.elias.agregadordecontactosntg.Contacto.addContacto;

public class MainActivity extends AppCompatActivity {

    private static final int SOLICITUD_PERMISO_READ_CONTACTS = 0;
    private AppBarConfiguration mAppBarConfiguration;
    private static final int SOLICITUD_PERMISO_WRITE_CONTACTS = 0;
    private Contacto contacto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //--Contacto objeto
        contacto = new Contacto("Alvaro Jose", "666555765","a@a");
        //--
        final Context context = this.getBaseContext();

        FloatingActionButton fab = findViewById(R.id.fab);


            fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){  Thread thread = new Thread(){
                    public void run(){
                        Looper.prepare();
                        Log.d("CONTACTOS: ","Intentando añadir contacto: " + contacto.toString());

                        if (ActivityCompat.checkSelfPermission(context,
                                WRITE_CONTACTS)
                                == PackageManager.PERMISSION_GRANTED) {
                            Log.d("CONTACTOS: ","Hay permisos para añadir contacto. Comparando si ya existe... : " + contacto.toString());
                            if(contacto.getNombrePorNumero(contacto.getNumero(), context) == null){
                                addContacto(context, contacto);
                            } else {
                                Log.d("CONTACTOS: ","Se ha inentado añadir un contacto que ya existe: " + contacto.toString());
                            }


                        } else {
                            Log.d("CONTACTOS: ","No hay permisos para añadir o ver contactos. Solicitando... : " + contacto.toString());

                            solicitarPermiso(WRITE_CONTACTS, "Sin el permiso"+
                                            " administrar llamadas no puedo borrar llamadas del registro.",
                                    SOLICITUD_PERMISO_WRITE_CONTACTS, MainActivity.this);
                            solicitarPermiso(READ_CONTACTS, "Sin el permiso"+
                                            " administrar llamadas no puedo borrar llamadas del registro.",
                                    SOLICITUD_PERMISO_READ_CONTACTS, MainActivity.this);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView contenido = findViewById(R.id.mensaje_correo);
                                Correo c = null;
                                try {
                                    c = new LectorMail(context).execute().get();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                contenido.setText(c.getContenido());

                            }
                        });



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


        Handler h2 = new Handler();
        Thread thread = new Thread(){
            public void run(){

                Looper.prepare();

                if (ActivityCompat.checkSelfPermission(context,
                        WRITE_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.d("CONTACTOS: ","Hay permisos para añadir contacto. Comparando si ya existe... : " + contacto.toString());
                    if(contacto.getNombrePorNumero(contacto.getNumero(), context) == null){
                        addContacto(context, contacto);
                    } else {
                        Log.d("CONTACTOS: ","Se ha inentado añadir un contacto que ya existe: " + contacto.toString());
                    }


                } else{
                    try{
                        Thread.sleep(6000);
                        // Then do something meaningful...
                        Log.d("CONTACTOS: ","No hay permisos para añadir o ver contactos. Solicitando... : " + contacto.toString());
                        solicitarPermiso(WRITE_CONTACTS, "Sin el permiso"+
                                        " administrar llamadas no puedo borrar llamadas del registro.",
                                SOLICITUD_PERMISO_WRITE_CONTACTS, MainActivity.this);
                        solicitarPermiso(READ_CONTACTS, "Sin el permiso"+
                                        " administrar llamadas no puedo borrar llamadas del registro.",
                                SOLICITUD_PERMISO_READ_CONTACTS, MainActivity.this);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView contenido = findViewById(R.id.mensaje_correo);
                        Correo c = null;
                        try {
                            c = new LectorMail(context).execute().get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        contenido.setText(c.getContenido());

                    }
                });



            }
        };

        thread.start();
    }





    public static void solicitarPermiso(final String permiso, String
            justificacion, final int requestCode, final Activity actividad) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(actividad,
                permiso)){
            new AlertDialog.Builder(actividad)
                    .setTitle("Solicitud de permiso")
                    .setMessage(justificacion)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ActivityCompat.requestPermissions(actividad,
                                    new String[]{permiso}, requestCode);
                        }})
                    .show();
        } else {
            ActivityCompat.requestPermissions(actividad,
                    new String[]{permiso}, requestCode);
        }
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
