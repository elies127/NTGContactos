package com.naranjatradicionaldegandia.elias.agregadordecontactosntg;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static com.naranjatradicionaldegandia.elias.agregadordecontactosntg.ServicioAgregador.nPedidos;
import static com.naranjatradicionaldegandia.elias.agregadordecontactosntg.ServicioAgregador.nPedidosNuevosClientes;


public class ServicioNotificacion extends Service {
    private static final String CANAL_ID = "notificacion_agregador";
    private static final String NOTIFICATION_CHANNEL_ID = "id_canal";
    private static final int NOTIFICATION_ID = 1;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *

     */
    NotificationCompat.Builder notificacion;
    public ServicioNotificacion() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startForeGround(){
        Log.d("SERVICIO AGREGADOR: ", "---------- INICIALIZANDO SERVICIO --------");
        //-- notis




    }

    @Override
    @RequiresApi(Build.VERSION_CODES.O)
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("ServicioNotificacion", "Lanzando servicio notificacion");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CANAL_ID)

                .setTicker("TICKER").setSubText("awds")
                .setContentIntent(pendingIntent).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("NTG Contactos")
                .setContentText("Nuevos clientes: " + nPedidosNuevosClientes + " - Pedidos totales: " + nPedidos);;
        Notification notification=builder.build();
        if(Build.VERSION.SDK_INT>=26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CANAL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notificaciones");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);


            startForeground(NOTIFICATION_ID, notification);
        }

        return START_STICKY;
    }

}
