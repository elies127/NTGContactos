package com.naranjatradicionaldegandia.elias.agregadordecontactosntg.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

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
        rellenaLista();
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
}