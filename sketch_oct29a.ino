#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <Ethernet.h>
#include <SPI.h>

String temp = "";
uint8_t pin_led=6;

WiFiServer server(80);
//GPS Satz ZuHause Leutenbach Seestraße 23,9m x 9m zentriert auf mein Schreibtisch
String w3w[]={"empty", "acres.bickered.magnifies", "waggles.budgeted.ivory", "newbie.imprecise.obliging","listened.implants.leaner", "covered.unoccupied.exasperated", "sunscreens.aggregation.require", "madcap.liquidity.dragon", "machining.congress.paddlers", "swooping.flounced.bobbled"};

//GPS Satz Feld Leutebach,
//String w3w[]={"empty", "lifelong.motion.miracle", "powerful.attends.buckets", "mondays.troubles.charted","amused.coasted.walks", "lure.parts.collect", "averages.gestures.briskly", "awaiting.powers.hired", "february.lawful.edges", "jump.geology.green"};
/*
w3w[4]= "alert.mammals.curtain"; 
w3w[5]= "tracking.held.forgot"; 
w3w[6]= "slurred.repaid.select"; 
w3w[7]= "rocks.inquest.upcoming"; 
w3w[8]= "takes.explores.moves"; 
w3w[9]= "fuel.breezy.embarks";    */
/*
IPAddress ip(192, 168, 0, 117);
IPAddress gateway(192, 168, 0, 1);
IPAddress subnet(255, 255, 255, 0);
*/
void setup() {
pinMode(pin_led, OUTPUT);
  
  WiFi.mode(WIFI_AP);             // Betrieb im AP-Modus
  WiFi.softAP("SCL_WiFi", "");    // Benamung/PW des AP 
server.begin();
  
  Serial.begin(115200);
  //WiFi.config(ip, gateway, subnet);
  //WiFi.begin("TP-LINK_A1B21C", "5991_29Pascal");
  /*while (WiFi.status() != WL_CONNECTED) {
    delay(100);
  }*/
  server.begin(); //Wartet auf eingehende Verbindungen
  Serial.println("Connected");
}

void loop() {
  WiFiClient client = server.available(); //Falls ein Client sich verbindet. gibt es Daten zu lesen
  if (!client) { //Die if-Abfrage verbessert Laufzeit
    return;
  }
  //Serial.println(client.readStringUntil('$'));
  temp = client.readStringUntil('$');
  Serial.println(temp);
  int z=0;
  int k=0;
  for(z=0; z<11; z=z+1)
  {
    if(temp == w3w[z])
    {
      digitalWrite(pin_led, 1);
      Serial.println("ANNNN");
      temp="";
  
      //Aktion
    }
  }

  for(k=0; k<11; k=k+1)
  {
    if(temp != "" && temp != w3w[k])
    {
      digitalWrite(pin_led, 0);
      Serial.println("AUZZZZZ");
      temp="";
  
      //Aktion
    }
  }

  
    
  //String x = "esp8266";
 // client.println(x);
 // delay(100);
 //client.flush(); //wartet bis alle herrausgehnde chars im Buffer gesendet wurden
}
