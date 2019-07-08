//Dieser Service wird gestartet/beendet sobald der Button Starte/Stoppe gedrückt wurde

package com.example.jinsuhartmann.scl;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class SCL_Service extends Service { //Diese Klasse als Service deklarieren bzw. um die Service-Funktionen erweitern

    //Deklarierung der Objekte die für den Service benötigt werden
    private LocationListener listener; // Fragt Standortänderungen ab
    private LocationManager locationManager;// Für Zugang zu den Ortungsfunktionen des Smartphones

    public String bg;//Globale Variable für das Abspeichern vom Breitengrad des aktuellen Standortes
    public String lg;//Globale Variable für das Abspeichern vom Längengrad des aktuellen Standortes
    public String w3w;

    public String adress; //
    public String ssidname; //Speichert die SSID des Access Points mit dem das Handy aktuell verbunden ist

    /*Strings zum Abgleich des SSID Namens des AP des Chips zu dem das Handy aktuell verbunden ist um
    die IP Adresse und Port individuell einzustellen*/
    static String strg1 = "\"SCL_E1\"";
    static String strg2 = "\"SCL_E2\"";
    static String strg3 = "\"SCL_L1\"";
    static String strg4 = "\"SCL_E3\"";
    static String strg5 = "\"SCL_E4\"";
    static String strg6 = "\"SCL_L2\"";
    static String strg7 = "\"SCL_E5\"";
    static String strg8 = "\"SCL_E6\"";
    static String strg9 = "\"SCL_L3\"";
    static String strg10 = "\"SCL_E7\"";
    static String strg11 = "\"SCL_E8\"";

    WifiInfo wifiInfo; /* Ermöglicht die Abfrage von Statusinformationen vom AP mit dem das Smartphone aktuell verbunden ist.
                          (Wird hier benötigt um den Namen der SSID abzufragen und zu speichern) */

    String IpAddress; //Deklarierung der Ip Adresse des Servers zu dem sich das Smartphone verbinden soll
    int Port;         //Deklarierung des Serverports zu dem sich das Smartphone verbinden soll

    @Nullable //Die Methode darf "null" zurückgeben
    @Override
    public IBinder onBind(Intent intent) { //Diese Methode muss immer im Service implementiert werden, wird aber hier nicht benötigt somit soll Diese "null" zurückgeben
        return null;
    }

    //"final" weil sich der Wert der Variable nach einmaliger Zuordnung nie verändern soll
    static final String API_KEY = "O70UBGBH"; //Einzigartiger persönlicher Key der den Zugang zur API ermöglicht
    static final String API_URL = "https://api.what3words.com/v2/reverse?"; //URL für die API Request

    @Override
    public void onCreate() //onCreate-Methode (Methode die immer zu Anfang ausgeführt wird)
    {
        listener = new LocationListener() { //Initialisieren des LocationListener (Diese Methode wird aufgerufen wenn sich etwas im Location_Manager ändert)
            @Override
            public void onLocationChanged(Location location) { // Wird aufgerufen wenn sich der Standort geändert hat
                // Übertragen der Daten aus dieser Methode in die MainActivity
                Intent i = new Intent("location_update"); //Erstellen eines neuen Intents i mit dem Aktionsnamen "location_update"(Ziel des Intents ist die Standortaktualisierung) (Die Bezeichnung dient als Intent-Filter damit der BroadcastReceiver in der Main zuerst nach Intents mit diesem Namen abfrafgt)

                bg = Double.toString(location.getLatitude()); //Speichern des Breitengrad als String
                lg = Double.toString(location.getLongitude());//Speichern des Längengrad als String

                new RetrieveFeedTask().execute(); //Neue Klasse der Klasse RetrieveFeedTask erzeugen und ausführen

                if(w3w != null) {
                    i.putExtra("Koordinaten", w3w); // Anhängen von "Koordinaten"(=Bezeichnung des Anhangs) an den Intent (Koordinaten gleich Längengrad + Breitengrad)
                    sendBroadcast(i); //"Normale" Broadcast bzw. Übertragung (Intent i wird an alle Empfänger verschickt)


                    ////////////////////////
                    WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE); //"wifi" ist der Name des neuen WifiManagers

                    ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); //Neues ConnectivityManager-Objekt welcher über den Inhalt "CONNECTIVITY_SERVICE" mit dem Befehl "objektname.getAllNetworkInfo()" sämtliche Verbindungsinformationen des Smarthpones aufrufen kann
                    NetworkInfo[] networkInfo = conMan.getAllNetworkInfo();


                    wifi.setWifiEnabled(true);

                    SystemClock.sleep(2500); //Alternative zu Thread.sleep()

                    wifiInfo = wifi.getConnectionInfo();
                    ssidname = wifiInfo.getSSID();


                    if (ssidname.equals(strg1)) {
                        Port = 10;
                        IpAddress = "192.168.1.1";
                    }
                    else if (ssidname.equals(strg2)) {
                        Port = 20;
                        IpAddress = "192.168.1.2";
                    }
                    else if (ssidname.equals(strg3)) {
                        Port = 30;
                        IpAddress = "192.168.1.3";
                    }
                    else if (ssidname.equals(strg4)) {
                        Port = 40;
                        IpAddress = "192.168.1.4";
                    }
                    else if (ssidname.equals(strg5)) {
                        Port = 50;
                        IpAddress = "192.168.1.5";
                    }
                    else if (ssidname.equals(strg6)) {
                        Port = 60;
                        IpAddress = "192.168.1.6";
                    }
                    else if (ssidname.equals(strg7)) {
                        Port = 70;
                        IpAddress = "192.168.1.7";
                    }
                    else if (ssidname.equals(strg8)) {
                        Port = 80;
                        IpAddress = "192.168.1.8";
                    }
                    else if (ssidname.equals(strg9)) {
                        Port = 90;
                        IpAddress = "192.168.1.9";
                    }
                    else if (ssidname.equals(strg10)) {
                        Port = 100;
                        IpAddress = "192.168.2.1";
                    }
                    else if (ssidname.equals(strg11)) {
                        Port = 110;
                        IpAddress = "192.168.2.2";
                    }
                    else
                    {
                        ;
                    }



                    ssidname = ""; //Zurücksetzen


                    if (adress != null) {
                        MyClientTask myClientTask = new MyClientTask(adress); //welcomeMsg.getText().toString()
                        myClientTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }

                    SystemClock.sleep(1000);
                    wifi.setWifiEnabled(false);
                }
                w3w=null;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            //Bisher wird immer nur genau einmal abgefragt wenn der Start Button gedrückt wird (Wenn man aber den Standort in dem Fenster nicht einschaltet und wieder zurückklickt und erneut auf Start drückt, wird das Einstellungsfenster nicht mehr geöffnet)
            public void onProviderDisabled(String provider) { //Falls GPS deaktiviert ist
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS); //Neuer Intent zu Standorteinstellungen des Handies
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Das Setzen des Flags lässt diesen Intent eine neue Task starten d.h. wenn man von den Standorteinstellungen wieder zurück geht in die App, wird einem die App im selben Zustand gezeigt wie sie vorher war
                startActivity(i); //Starte die Aktivität aus dem Intent i (Standorteinstellungen des Smartphones)
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE); //Initialisieren des Location Managers mit der getSystemService-Methode (Welcher Service wird benötigt ? Location Service.)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, listener); //Abfrage des GPS_Provider alle 3000ms, GPS_PROVIDER = Name des GPS Standortproviders, MinDistance: Min. Distanz zu vorherigem Standort damit Standort aktualisiert wird(falls 0 dann wird nur die minTime berücksichtigt) ,listener = Name des Listeners
        // Rot unterstrichen weil nach den Berechtigungen gefragt wird, jedoch wurde das bereits in der MainActivity geprüft (somit kann die Warnung ignoriert werden)

    }

    public void onDestroy() //Methode die den Service "zerstört"/beendet wenn die Taste "Stoppe" gedrückt wurde und der Service somit beendet wird
    {
        super.onDestroy();
        if (locationManager != null) //Solange noch etwas im locationManager
        {
            locationManager.removeUpdates(listener); //Löscht die Informationen im LocationListener und zudem werden nach dem Befehl auch keine neuen Updates(=holen aktueller Informationen) mehr ausgeführt
        }
        ;
    }


    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... urls) { //Was soll im Hintergrund der AsyncTask ablaufen

            try {
                URL url = new URL(API_URL + "coords=" + bg + "%2C" + lg + "&key=" + API_KEY); //Zusammenstellen der URL die als API Request gesendet werden soll: https://api.what3words.com/v2/reverse?coords=bg%2Clg&key=O70UBGBH
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(); //Verbindung zu der URL herstellen und eine neue Instanz der Klasse HttpURLConnection erstellen die weitere Funktionalitäten ermöglicht

                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())); //Die neue Instanz der Klasse BufferedReader dient als Zwischenspeicher welcher den eingehenden Datenfluss der URL Verbindung zwischenspeichert
                    StringBuilder stringBuilder = new StringBuilder(); //Neuen Instanz der Klasse StringBuilder initialisieren (Tut das was der Name sagt, man kann mithilfe des append-Befehls schrittweise seinen String aufbauen)
                    String line;
                    while ((line = bufferedReader.readLine()) != null) { // Die zusätzliche Zwischenvariable line speichert den Zeilennhalt des bufferedReader zwischen und hängt ihn an der Instanz des StringBuilders an sofern noch was im bufferedReader drin steht
                        stringBuilder.append(line).append("\n"); //String Builder fügt die API-Antwort Zeile für Zeile zusammen
                    }
                    bufferedReader.close(); //Schließen des BufferedReaders
                    return stringBuilder.toString(); //Zurückgeben des Inhalts des StringBuilders als String(Die gesamte Antwort der API), Umwandlung als String notwendig weil das Rückgabeformat JSON, GeoJSON oder XML ist
                } finally { //finally ist ein Programmabschnitt der unabhängig von der try-catch Auswertung immer ausgeführt wird
                    urlConnection.disconnect(); //Nachdem die Antwort gelesen wurde wird die bestehende Verbindung zur URL geschlossen
                }

            } catch (Exception e) { //Fehlerbehandlung, falls die Anweisungen in try fehlgeschlagen ist wird der catch-Block ausgeführt
                //Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }


        protected void onPostExecute(String response) { //Was soll nach der doInBackground-Methode ausgeführt werden
            if (response == null) { //Falls die Anweisungen im äußeren try-Block der doInBackground Methode fehlschlägt wird folgende Fehlermeldung ausgegeben
                response = "Verbindung zur API URL fehlgeschlagen";
            }

            try { // Herrausnehmen einzelner Objekte aus der API Response
                JSONObject object = (JSONObject) new JSONTokener(response).nextValue(); //Zum Parsen des JSON-Strings, die gesamte API Antwort von W3W ist ein Wert somit wird mit nextValue DIESER Wert bzw die Antwort in "object" gespeichert
                adress = object.getString("words"); //"words" ist der Abschnitt der API Antwort der die 3-Wort Adresse enthält

                w3w = adress; //Das TextView w3w mit der 3-Wort Adresse adress füllen
            }
            catch (JSONException e) { } //Fehlerbehandlung

        }
    }


    @SuppressLint("StaticFieldLeak")
    public class MyClientTask extends AsyncTask<Void, Void, Void> {  //Klasse um die 3-Wort Adresse an den ESP AP zu senden, Die Methode soll nichts zurückgeben
        //String response = "";
        String msgToServer;

        MyClientTask(String msgTo) { //Methode um den Übergabewert msgTo in der Variable msgToServer zu speichern
            msgToServer = msgTo;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Socket socket = null;
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;


            try {
                socket = new Socket(IpAddress, Port);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                if (!msgToServer.equals(""))
                    dataOutputStream.writeBytes(msgToServer + "$");


            }
            catch (IOException e) { }

            finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                    }
                }
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //textResponse.setText(response);
            super.onPostExecute(result);
        }
    }
}