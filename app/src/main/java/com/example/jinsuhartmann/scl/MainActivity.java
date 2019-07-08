package com.example.jinsuhartmann.scl;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    Button bStart, bStopp; //Deklarierung
    TextView textView; //Textfeld in der App um einen Text darstellen zu können


    //Der Broadcast Receiver verhält sich ähnlich wie der OnClickListener. Er wartet ständig darauf Daten zu empfangen und wenn er welche empfängt
    //Broadcast = Übertragung

    BroadcastReceiver broadcastReceiver; //Deklarierung des Broadcast Receiver für den Intent zur Übertragung von Daten
    @Override
    protected void onResume() //Hier steht drin was dauerhaft ausgeführt (bis ein bestimmtes Ereignis passiert z.B. Ausschalten des Bildschirms, Eingehender Anruf)
    {
        super.onResume();
        if(broadcastReceiver == null) //Falls noch kein Broadcast Receiver existiert wird ein Neuer erzeugt
       {
            broadcastReceiver = new BroadcastReceiver() { //Neuen Broadcast Receiver erstellen, ab jetzt wartet er, ähnlich wie der OnClickListener, darauf Datan zu empfangen und wenn er was empfängt wird der folgende Code ausgeführt
                @Override
                public void onReceive(Context context, Intent intent) { //Falls der BroadacstReceiver einen Broadcast empfängt, nimmt die onReceive-Methode den Broadcast entgegen
                    //Context=Benamung des "Ortes" wo die Übertragung empfangen wird (Context spricht den Ort an wo sich der Code aktuell befindet) ,Intent= Benamung des Intents von dem die Übertragung gesendet wird um auf Diese zugreifen zu können
                    textView.append("\n" +intent.getExtras().get("Koordinaten")); //Umgewandelte 3-Wort Adressen an das textView innerhalb des ScrollViews anhängen
                    //putExtra: In der Service-Klasse wird der Broadcast namens "i" an die MainActivity verschickt. Über "i.putExtra(name,inhalt)" kann ein String welcher in der String-Variable "inhalt" steht unter dem Namen/Identifier "name" übertragen werden
                    //getExtras: Über " lokalerIntentName.getExtras().get("Name/Identifier") " kann man auf den String-Inhalt aus dem Broadcast des Intents "lokalerIntentName" des Identifiers "Name/Identifier" zugreifen.
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update")); //IntentFilter = Zuordnen eines Identifiers damit der BroadcastReceiver nur bzw. gezielt Intents mit dem zugehörigen Identifier/Namen empfängt
    }

    public void onPause() { //onPause wird aufgerufen wenn der Benutzer die Seite wechselt oder eine PopUp-Benachrichtigung erhält die teilweise oder vollständig die eigentlicher Benutzeroberfläche abdeckt
        super.onPause();
        unregisterReceiver(broadcastReceiver); //Zuweisung des broadcastReceiver zum IntentFilter löschen um Speicher freizugeben
    }

    //Eventuell doch die onDestroy Methode aufrufen um den aktuellen BroadcastReceiver zu zerstören?
    //Es wird nach jedem Broadcast bei einem erneuten Broadcast, jedes Mal ein neuer BroadcastReveiver erstellt welcher der App so lange zugeordnet ist bis die Applikation deinstalliert wird (Maginaler Speicherverbrauch, trotzdem eine Optimierung)


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bStart = (Button) findViewById(R.id.btn1); //Zuordnen der deklarierten Buttons aus der Aktivität, mit denen des Layouts
        bStopp = (Button) findViewById(R.id.btn2);
        textView = (TextView) findViewById(R.id.tv1); //Zuordnen der deklarierten TextViews aus der Aktivität, mit dem des Layouts

        disablewifi(); //WiFi des Smartphones ausschalten (Wenn der Starte-Button gedrückt wird müssen zunächst Mobile Daten eingeschaltet sein um die 3-Wort Adresse des aktuellen Standortes über die API zu erhalten)
        SystemClock.sleep(500); //500ms warten bis das WiFi zuverlässig ausgeschaltet ist


        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); //Neues ConnectivityManager-Objekt welcher über den Inhalt "CONNECTIVITY_SERVICE" mit dem Befehl "objektname.getAllNetworkInfo()" sämtliche Verbindungsinformationen des Smarthpones aufrufen kann
        NetworkInfo[] networkInfo = conMan.getAllNetworkInfo(); //Alle Verbindungsinformationen über das Smartphone werden im Array "networkInfo" gespeichert welcher Daten vom Datentyp NetworkInfo speichert

        for (NetworkInfo netInfo : networkInfo) { // For-Schleife um jede Netzwerkinfo im Array abzugleichen ob diese dem Namen "MOBILE" entspricht, welche die Informationen zur mobilen Datenverbindung des Smartphones enthält.
            if ((netInfo.getTypeName().equalsIgnoreCase("MOBILE")) && (!(netInfo.isConnected()))) // Falls die Info mit dem Namen "MOBILE" in der Schleife gefunden wurde und diese nicht verbunden ist sollen das zugehörige Einstellungsmenü geöffnet werden (IgnoreCase bedeutet, dass beim Vergleich der Strings z.B. Goß/Kleinschreibung ignoriert wird, lediglich die Stringlänge und der Zeicheninhalt müssen übereinstimmen)
                gotosettings(); //Aufrufen der Netzwerkeinstellungen (Eigene Methode)
        }

        if(!runtime_permissions()) //Falls keine Standortberechtigungen mehr abgefragt werden müssen bzw. diese erteilt sind
        enable_buttons(); // Freischalten der Buttons
    }

    private void gotosettings() { //Funktion um den Intent der Netzwerkeinstellung zu öffnen
        Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS); // Neues Intent-Objekt erstellen namens "intent" mit dem Ziel der Netzwerk Einstellungen des Smartphones
        startActivity(intent); //Intent ausführen
    }

    private void disablewifi()//Funktion um das WiFi zu deaktivieren
    {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE); //Neues WiFi-Manager-Objekt namens "wifi" erstellen um auf WiFi-Aktivitäten des Smartphones zugreifen zu können
        wifi.setWifiEnabled(false); //WiFi des Smartphones ausschalten
    }

    private void enable_buttons() { //Methode welche die onClick Methode für die Buttons enthält

        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disablewifi();
                Intent i = new Intent(getApplicationContext(),SCL_Service.class); //Neues lokeles Intent-Objekt namens "i" der zur SCL_Service Klasse führt
                //Man kann hier für den Intent START-Ort nicht "this" benutzen weil man sich in einer lokalen Methode befindet und nicht global in der Klasse ist. Man kann nicht von der Methode aus in die Ziel-Klasse wechseln.
                startService(i); //Startet den Service über den Intent i (Service ist ein Prozess der im Hintergrund abläuft ohne eine Benutzerschnittstelle zu bieten wie bei einer Activity)
            }
        });
        bStopp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),SCL_Service.class);
                stopService(i); //SCL_Service stoppen
            }
        });
    }

    // Methoden die Berechtigungen abfragen. Ab einer SDK Version von 23 und höher benötigen die Apps gesonderte Zugriffsrechte um Dienste des Smarthpones wie z.B. den Standortdienst nutzen zu können.

    //Fall die SDK Version mindestens 23 ist und die Location_Berechtigungen nicht erteilt wurden
    private boolean runtime_permissions()  //Methode die abfragt ob Berechtigungen abgeprüft werden müssen
     {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) //Falls die Berechtigungen vorhanden sind wird "PERMISSION_GRANTED" zurückgegeben, wenn nicht dann wird "PERMISSION_DENIED" zurückgegeben und der Benutzer wird in der App aufgefordert diese Berechtigungen zu erteilen
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100); // Den Benutzer dazu auffordern die Berechtigungen zu erteilen(Request-Code 100), Sobald auf die Aufforderung reagiert wurde, wird die onRequestPermissionResult-Methode aufgerufen
            // In dem neuen String-Array werden die abzufragenden Berechtigungen gespeichert, an Stelle 0=ACCESS_FINE_LOCATION und an Stelle 1=ACCESS_COARSE_LOCATION
            return true; // Es müssen Berechtigungen abgeprüft werden
        }
        return false; // Es müssen keine Berechtigungen geprüft werden
    }

    @Override
    public void onRequestPermissionsResult(int  requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) //In dieser Methode wird geprüft ob der Anfrage-Code 100 ausgeführt wurde und die Berechtigungen aus der Anfrage erteilt sind
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); //Super immer benötigt bei vorgegeben Android Methoden damit der eigene Code ausgeführt wird
        // Falls Request Code = 100 (Für Anfragen der Berechtigungen) und alle Berechtigungen erteilt sind, dann werden die Buttons freigeschaltet
        if (requestCode==100)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) // Falls für beide Berechtigungen von ContextCompat.checkSelfPermission "PERSMISSION_GRANTED zurückgegeben wurde werden die Buttons freigeschaltet
            {
                enable_buttons(); //Freigabe der Buttons
            }
            else
            {
                runtime_permissions(); //Ansonsten werden die Berechtigungen erneut abgefragt
            }
        }


    }
}