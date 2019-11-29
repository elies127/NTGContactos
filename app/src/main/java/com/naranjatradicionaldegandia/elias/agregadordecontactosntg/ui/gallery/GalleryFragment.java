package com.naranjatradicionaldegandia.elias.agregadordecontactosntg.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.naranjatradicionaldegandia.elias.agregadordecontactosntg.Adaptador;
import com.naranjatradicionaldegandia.elias.agregadordecontactosntg.BDAgenda;
import com.naranjatradicionaldegandia.elias.agregadordecontactosntg.Contacto;
import com.naranjatradicionaldegandia.elias.agregadordecontactosntg.R;
import com.naranjatradicionaldegandia.elias.agregadordecontactosntg.VerContacto;

public class GalleryFragment extends ListFragment {
    private int iDAct = 0;
    private int numFilas;
    private int [] ids;
    private BDAgenda bdAgenda;
    private Adaptador adaptador;
    private GalleryViewModel galleryViewModel;
    private Handler m_Handler;
    private Runnable mRunnable;
Context context;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bdAgenda = new BDAgenda(this.getContext());
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        final TextView textView = root.findViewById(R.id.text_gallery);
        galleryViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
     //   if(!((bdAgenda.recuperarContacto(1)) == null)){

    //    }

        m_Handler = new Handler();
        mRunnable = new Runnable(){
            @Override
            public void run() {
                rellenaLista();
                m_Handler.postDelayed(mRunnable, 3000);// move this inside the run method
            }
        };
        mRunnable.run(); // missing

        return root;
    }


    public void verContacto(View vista) {
        Contacto contacto;
        Intent i = new Intent(this.getContext(), VerContacto.class);

        i.putExtra("ID",iDAct);

        contacto= bdAgenda.recuperarContacto(iDAct);

        i.putExtra("Nombre", contacto.getNombre());
        i.putExtra("Movil", contacto.getNumero());
        i.putExtra("Direccion", contacto.getDireccion());
        i.putExtra("Mail", contacto.getCorreo());
        i.putExtra("Npedidos", contacto.getNpedidos());


        startActivity(i);
    }
    public void rellenaLista(){
        // Obtiene el numero de fila de la BD
        numFilas=bdAgenda.numerodeFilas();

        // Para cada numero de fila recupera los ids
        if (numFilas> 0) {ids= bdAgenda.recuperaIds();}

        // Obtiene los contactos de la BD los pasa al adaptador par que los cree
        adaptador = new Adaptador(this.getContext(), bdAgenda.recuperarCursosdeContactos());
        // Establece el adaptador
        setListAdapter(adaptador);

    }

    public void onListItemClick(ListView lv, View view, int posicion, long id) {
        iDAct= ids[posicion];
        muestraDatosenBtSh();
    }
    private ImageView btsheet_low, btsheet_image_foto, btsheet_call;
    private TextView btsheet_nombre, btsheet_movil;
    private BottomSheetBehavior bSB;
    private SharedPreferences prefs;
    public static final String MyPREFERENCES = "MyPrefs";
    public String pref_movil = "movil";
    public String pref_nombre = "nombre";
    public String pNombre, pMovil;
    private LinearLayout btt_linear;

    private void muestraDatosenBtSh() {


        final Contacto contacto;

        bSB.setState(BottomSheetBehavior.STATE_EXPANDED);

        contacto = bdAgenda.recuperarContacto(iDAct);

        btsheet_nombre.setText(contacto.getNombre());
        btsheet_movil.setText(contacto.getNumero());


        bSB.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Cuando esta activo el bSB ocultamos el boton de añadir un nuevo contacto ya
                // que si pulsabamos donde esta en la vista se abriria, aunque este detras del BSB


            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }

        });

        // Cierra el btsheep cuando pulsamos el boton
        btsheet_low.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bSB.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // Abre el telefono para llamar al contacto
        btsheet_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse("tel:(+34)" + contacto.getNumero()));
                startActivity(i);
            }
        });

    }
    static Bitmap a;
    private void configuraBottonSheet(){

        btt_linear = getView().findViewById(R.id.btt_sh);
        bSB = BottomSheetBehavior.from(btt_linear);

        btsheet_low = (ImageView) getView().findViewById(R.id.btsheep_low);
        btsheet_image_foto = (ImageView) getView().findViewById(R.id.btsheep_image_foto);
        btsheet_call = (ImageView) getView().findViewById(R.id.btsheep_call);
        btsheet_nombre = (TextView) getView().findViewById(R.id.btsheep_nombre);
        btsheet_movil = (TextView) getView().findViewById(R.id.btsheep_movil);

        if(pNombre.length()>4 && pMovil.length()>4){
            btsheet_nombre.setText(pNombre);
            btsheet_movil.setText(pMovil);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Drawable drawable = getResources().getDrawable(R.drawable.personicon);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();




            btsheet_image_foto.setImageBitmap(bitmap);
        }
    }

}