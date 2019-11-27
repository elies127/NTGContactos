package com.naranjatradicionaldegandia.elias.agregadordecontactosntg;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class LectorMail extends AsyncTask<Void, Void, Correo> {
    private Context context;
    private String mensaje;
    private Correo c;
    public LectorMail(Context context){
        this.context=context;
    }
    protected void onPreExecute() {
        // Runs on the UI thread before doInBackground
        // Good for toggling visibility of a progress indicator


    }
    @Override
    protected Correo doInBackground(Void... params) {

        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", "elies1324@gmail.com", "mkihqysmmqedyayg");
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            javax.mail.Message msg = inbox.getMessage(inbox.getMessageCount());
            javax.mail.Address[] in = msg.getFrom();
            for (javax.mail.Address address : in) {
                System.out.println("FROM:" + address.toString());
            }
            Multipart mp = (Multipart) msg.getContent();
            BodyPart bp = mp.getBodyPart(0);
            System.out.println("FECHA DE ENVIO:" + msg.getSentDate());
            System.out.println("ASUNTO:" + msg.getSubject());
          //  System.out.println("CONTENT:" + bp.getContent());


           // context.startActivity(i);
           c = new Correo(msg.getSubject(), bp.getContent().toString(), msg.getSentDate().toString(), msg.getFrom().toString());

           return c;

        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return null;
    }
    protected void onPostExecute(Correo c) {
        super.onPostExecute(c);
    }
}
