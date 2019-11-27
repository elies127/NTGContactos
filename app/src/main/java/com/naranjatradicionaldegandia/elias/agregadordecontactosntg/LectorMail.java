package com.naranjatradicionaldegandia.elias.agregadordecontactosntg;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class LectorMail extends AsyncTask<Void, Void, String> {
    public AsyncResponse delegate = null;
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
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }
    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }

    @Override
    protected String doInBackground(Void... params) {

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
            String contentType = msg.getContentType();
            String messageContent="";

            if (contentType.contains("multipart")) {
                Multipart multiPart = (Multipart) msg.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    messageContent = part.getContent().toString();
                }
            }
            else if (contentType.contains("text/plain")
                    || contentType.contains("text/html")) {
                Object content = msg.getContent();
                if (content != null) {
                    messageContent = content.toString();
                }
            }
            System.out.println(" Message: " + messageContent);



            System.out.println("FECHA DE ENVIO:" + msg.getSentDate());
            System.out.println("ASUNTO:" + msg.getSubject());

            System.out.println("CONTENT CON mime:" + getTextFromMessage(msg));


           c = new Correo(msg.getSubject(), getTextFromMessage(msg), msg.getSentDate().toString(), msg.getFrom().toString());

           return c.getContenido();

        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return null;
    }
    protected void onPostExecute(String c) {
        super.onPostExecute(c);

        delegate.processFinish(c);
    }






}
