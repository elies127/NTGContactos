package com.naranjatradicionaldegandia.elias.agregadordecontactosntg;

import android.app.AlarmManager;
import android.app.IntentService;
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
import android.graphics.BitmapFactory;
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
import java.util.regex.Pattern;

import static android.Manifest.permission.WRITE_CONTACTS;
import static android.content.Context.NOTIFICATION_SERVICE;
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



public class ServicioAgregador extends IntentService {
    private static final String CHANNEL_ID = "nuevoContacto";
    NotificationManager notificationManager;

    static final String CANAL_ID = "contactos";
    static final int NOTIFICACION_ID_SERVICIO = 3;
    static final int NOTIFICACION_NUEVOCLIENTE_ID = 2;
    public Context context = this;
    public Handler handler = null;
    public BDAgenda bdAgenda;
    public static Runnable runnable = null;
    public Contacto contacto;
    public static Boolean estaEncendido = false;
    public static AsyncTask<Void, Void, String> correoNuevo;
    public static int nPedidosNuevosClientes = 0;
    public static int nPedidos = 0;
    public static String id_UltimoPedido;
    private Intent intent;
    NotificationCompat.Builder notificacion;
    private NotificationManager mManager;


    public ServicioAgregador() {
        super("ServicioAgregador");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    private void actualizarCorreo() throws ExecutionException, InterruptedException {

             correoNuevo = new LectorMail(context).execute();



    }

    public void startForeGround(){
        Log.d("SERVICIO AGREGADOR: ", "---------- INICIALIZANDO SERVICIO --------");
        //-- notis

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


    }

@RequiresApi(api = Build.VERSION_CODES.O)
@Override
    public void onHandleIntent(@Nullable Intent intent){

        if(estaEncendido == false){
            stopSelf();
        }

        bdAgenda = new BDAgenda(context);
        //--
        handler = new Handler();
        Log.d("SERVICIO AGREGADOR: ", "---------- COMIENZA EL BUCLE --------");


    intent = new Intent(context, MainActivity.class);



                try {Log.d("SERVICIO AGREGADOR: ", "INTENTANDO OBTENER NUEVO CORREO...");
                    actualizarCorreo();
                        Log.d("SERVICIO AGREGADOR: ", "CORREO OBTENIDO");
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
                    actualizarCorreo();
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


                } else if(finalString.equals("ERROR DE CONEXION")){

                    Toast.makeText(context, "Ha habido un error al intentar conectar con el correo", Toast.LENGTH_LONG);
                } else if (finalString.contains("Meeel! Hem aconseguit un nou client!")) {
                    String[] lines = finalString.split(System.getProperty("line.separator"));
                    String idpedido = null, numero = null, email = null, nombre = null;
                    for(int i = 0; i<lines.length;i++){
                        Log.d("LÍNEA PEDIDO:" , i+" -- " + lines[i]);
                        if(lines[i].contains("*Número de pedido:* ")){
                            idpedido = lines[i].replace("*Número de pedido:* ", "");
                        }
                        if(lines[i].contains("Teléfono Móvil: ")){
                            String s = lines[i].replace("Teléfono Móvil: ", "");
                             numero = s.substring(0, Math.min(s.length(), 9));

                        }
                        if(lines[i].contains("*Email:* ")){
                            email = lines[i].replace("*Email:* ", "");
                        }
                        if(i == 11){
                            if(!(tieneNumero(lines[i]))){

                                nombre = lines[i] + " NTGAutomatico";
                            }else {
                                nombre = lines[i+1] + " NTGAutomatico";
                            }

                        }
                    }
                Log.d("LINEA PEDIDO DATOS:" , numero);
                    Log.d("LINEA PEDIDO DATOS:" , email);
                    Log.d("LINEA PEDIDO DATOS:" , idpedido);
                    Log.d("LINEA PEDIDO DATOS:" , nombre);
                    contacto = new Contacto(nombre, numero, email); //Manualmente. Sustituir por los datos del mail.
                    //--- COMPROBADOR DE SI ES NUEVO CLIENTE O NO.

                    if (!(idpedido.equals(id_UltimoPedido))) { //Si es un pedido nuevo/un correo nuevo (Para no comprobar todo el rato si es un nuevo pedido)

                        id_UltimoPedido = idpedido;
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
                                createNotification("¡Nuevo cliente agregado!: " + contacto.getNombre(), "Nuevo Pedido de NTG");


                            } else {
                                Log.d("CONTACTOS: ", "El contacto ya existe, actualizando su pedido... : " + contacto.toString());
                                int id = bdAgenda.getIdporNombre(contacto.getNombre());
                                if(!(id==-1)){
                                    createNotification("¡Cliente repetidor!, no se ha agregado a nadie", "Nuevo Pedido de NTG");
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



        handler.postDelayed(runnable, 500);


    }
    public boolean tieneNumero( String s )
    {
        return Pattern.compile( "[0-9]" ).matcher( s ).find();
    }
    public void createNotification(String msg, String title) {
        Intent intent = null;

        intent = new Intent(this, MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel androidChannel = new NotificationChannel(CHANNEL_ID,
                    title, NotificationManager.IMPORTANCE_DEFAULT);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(Color.GREEN);

            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getManager().createNotificationChannel(androidChannel);
            Notification.Builder nb = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setTicker(title)
                    .setShowWhen(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                            R.mipmap.ic_launcher_round))
                    .setAutoCancel(true).setContentIntent(contentIntent);


            getManager().notify(101, nb.build());

        }
    }
    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(estaEncendido){

            AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
            alarm.set(
                    alarm.RTC_WAKEUP,
                    System.currentTimeMillis() + (10),
                    PendingIntent.getService(this, 0, new Intent(this, context.getClass()), 0)
            );
        }

    }





}
