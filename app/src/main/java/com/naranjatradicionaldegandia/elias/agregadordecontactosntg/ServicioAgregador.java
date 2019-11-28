package com.naranjatradicionaldegandia.elias.agregadordecontactosntg;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.WRITE_CONTACTS;
import static com.naranjatradicionaldegandia.elias.agregadordecontactosntg.Contacto.addContacto;

class Pedido{
    private String nombre;
    private String numero;
    private String id_pedido;
    private String email;

    private String esNuevoCliente;
    private String urlPedido;
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getId_pedido() {
        return id_pedido;
    }

    public void setId_pedido(String id_pedido) {
        this.id_pedido = id_pedido;
    }

    public String getEsNuevoCliente() {
        return esNuevoCliente;
    }

    public void setEsNuevoCliente(String esNuevoCliente) {
        this.esNuevoCliente = esNuevoCliente;
    }

    public String getUrlPedido() {
        return urlPedido;
    }

    public void setUrlPedido(String urlPedido) {
        this.urlPedido = urlPedido;
    }




    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}



public class ServicioAgregador extends Service{
    NotificationManager notificationManager;

    static final String CANAL_ID = "contactos";
    static final int NOTIFICACION_ID_SERVICIO = 1;
    static final int NOTIFICACION_NUEVOCLIENTE_ID = 2;
    public Context context = this;
    public Handler handler = null;
    public BDAgenda bdAgenda;
    public static Runnable runnable = null;
    public Contacto contacto;
    public static Boolean estaEncendido = false;
    public static AsyncTask<Void, Void, String> correoNuevo;
    public int nPedidosNuevosClientes = 0;
    public int nPedidos = 0;
    public String id_UltimoPedido;
    private Intent intent;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void actualizarCorreo() throws ExecutionException, InterruptedException {

             correoNuevo = new LectorMail(context).execute();



    }
    @Override

    public void onCreate() {
        Toast.makeText(this, "Servicio creado", Toast.LENGTH_LONG).show();
        Log.d("SERVICIO AGREGADOR: ", "---------- INICIALIZANDO SERVICIO --------");
        //-- notis
        intent = new Intent(context, MainActivity.class);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CANAL_ID, "Mis Notificaciones",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Descripcion del canal");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 100, 300, 100});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        final NotificationCompat.Builder notificacion =
                new NotificationCompat.Builder(ServicioAgregador.this, CANAL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("NTG Contactos")
                        .setContentText("Nuevos clientes: " + nPedidosNuevosClientes + " - Pedidos totales: " + nPedidos);


        startForeground(NOTIFICACION_ID_SERVICIO, notificacion.build());

        PendingIntent intencionPendiente = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class), 0);
        notificacion.setContentIntent(intencionPendiente);

        Log.d("SERVICIO AGREGADOR: ", "INTENTANDO OBTENER NUEVO CORREO...");
        try {
            actualizarCorreo();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bdAgenda = new BDAgenda(context);
        //--
        handler = new Handler();
        Log.d("SERVICIO AGREGADOR: ", "---------- COMIENZA EL BUCLE --------");
        runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                if (estaEncendido == false) {
                    stopSelf();
                }
                try {
                    actualizarCorreo();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("SERVICIO AGREGADOR: ", "---------- COMIENZA EL BUCLE --------");
                // Toast.makeText(context, "El servicio sigue ejecutandose", Toast.LENGTH_LONG).show(); PARA TESTEAR SI EL SERVICIO SE HA BLOQUEADO
                /*String jsonString = "{\n" +
                        "  \"nombre\": \"nombre cliente\",\n" +
                        "  \"numero\": \"5555\",\n" +
                        "  \"id_pedido\": \"6456\",\n" +
                        "  \"esNuevoCliente\": \"si\",\n" +
                        "  \"urlPedido\": \"https://www.naranjatradicionaldegandia.com/user/27379/orders/143157\",\n" +
                        "  \"email\": \"emaildelcliente@mail.com\"\n" +
                        "}";*/
                String jsonString = null;
                try {

                    jsonString = correoNuevo.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Todas las modifricaciones al String se hacen para parsear el correo tipico de Meeel a un json. Los datos van a traves de Zapier.-----------
               /* String pedido = jsonString.replace("---------------------------------------------------------------------------\n" +
                        "Visit this link to stop these emails: http://zpr.io/tYMfW", "");
                String[] lines = pedido.split(System.getProperty("line.separator"));
                for(int i=0;i<lines.length;i++){
                    if(lines[i].startsWith("-") || lines[i].contains("http://zpr.io/tYMfW")){
                        lines[i]="";
                    }
                }

                StringBuilder finalStringBuilder= new StringBuilder("");
                for(String s:lines){
                    if(!s.equals("")){
                        finalStringBuilder.append(s).append(System.getProperty("line.separator"));
                    }
                }
                String finalString = finalStringBuilder.toString();
                System.out.println( "Final string: " + finalString);

                //Todas las modifricaciones al String se hacen para parsear el correo tipico de Meeel a un json. Los datos van a traves de Zapier.-----------
*/                  String finalString = jsonString; //Borrar esto si se va a parsear algo.



               show_Notification("test", "test", NOTIFICACION_NUEVOCLIENTE_ID);


                if (finalString.contains("\"id_pedido\":")) {
                    Gson gson = new Gson();
                    Pedido datos = gson.fromJson(finalString, Pedido.class);

                    //  System.out.println("/////////////" + datos.toString());
                    contacto = new Contacto(datos.getNombre(), datos.getNumero(), datos.getEmail()); //Manualmente. Sustituir por los datos del mail.
                    //--- COMPROBADOR DE SI ES NUEVO CLIENTE O NO.

                    if (!(datos.getId_pedido().equals(id_UltimoPedido))) { //Si es un pedido nuevo/un correo nuevo (Para no comprobar todo el rato si es un nuevo pedido)

                        id_UltimoPedido = datos.getId_pedido();
                        if (ActivityCompat.checkSelfPermission(context,
                                WRITE_CONTACTS)
                                == PackageManager.PERMISSION_GRANTED) {

                            Log.d("CONTACTOS", "GET NOMBRE POR NUMERO" + contacto.getNombrePorNumero(contacto.getNumero(), context));
                            Log.d("CONTACTOS: ", "Hay permisos para añadir contacto. Comparando si ya existe... : " + contacto.toString());
                            if (contacto.getNombrePorNumero(contacto.getNumero(), context) == "") {
                                Log.d("CONTACTOS: ", "¡¡NUEVO CONTACTO DETECTADO!! Agregando a...: " + contacto.toString());
                                Log.d("BASE DE DATOS: ", "Guardando a la base de datos NUEVO CONTACTO: " + contacto.getNombre());
                                bdAgenda.insertarContacto(contacto.getNombre(), contacto.getDireccion(), contacto.getNumero(), contacto.getCorreo());
                                addContacto(context, contacto);
                                nPedidosNuevosClientes++;


                                notificacion.setContentText("Nuevos clientes: " + nPedidosNuevosClientes + " - Pedidos totales: " + nPedidos);
                                startForeground(NOTIFICACION_ID_SERVICIO, notificacion.build());
                            } else {
                                Log.d("CONTACTOS: ", "El contacto ya existe, actualizando su pedido... : " + contacto.toString());
                                int id = bdAgenda.getIdporNombre(contacto.getNombre());
                                if(!(id==-1)){
                                    Contacto c = bdAgenda.recuperarContacto(id);
                                    bdAgenda.modificarContacto(id, c.getNombre(), c.getDireccion(), c.getNumero(), c.getCorreo(), c.getNpedidos()+1);
                                }
                            }


                        } else {

                            // Then do something meaningful...
                            Log.d("CONTACTOS: ", "No hay permisos para añadir o ver contactos. Solicitando... : " + contacto.toString());

                        }
                    } else {
                        Log.d("CONTACTOS", "No se ha detectado un pedido nuevo");
                    }


                }
                handler.postDelayed(runnable, 250);
            }
        };

        handler.postDelayed(runnable, 500);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void show_Notification(String titulo, String desc, int id){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.when);
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        String CHANNEL_ID="MYCHANNEL";

        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),1,intent,0);
        Notification notification=new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentText(titulo)
                .setContentTitle(desc)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.sym_action_chat,"Title",pendingIntent)
                .setChannelId(CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_action_chat)
                .build();

        NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,"name",NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setSound(sound, attributes);
        notificationChannel.enableVibration(true);
        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(id,notification);


    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        handler.removeCallbacks(runnable);
        estaEncendido = false;
        Log.d("SERVICIO AGREGADOR: ", "---------- SERVICIO FINALIZADO --------");
        Toast.makeText(this, "Servicio detenido", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Toast.makeText(this, "Servicio iniciado por el usuario.", Toast.LENGTH_LONG).show();
    }


}
