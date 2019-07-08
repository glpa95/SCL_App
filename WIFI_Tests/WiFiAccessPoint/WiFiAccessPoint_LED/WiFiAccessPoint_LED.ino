//Bibliotheken
#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <ESP8266WebServer.h>


/* Netzwerkkonfiguration des AccesPoints */
const char *ssid = "ESPap";
const char *password = "123456789";

// Erzeugt webserver objekt, welches auf HTTP Anfragen 
ESP8266WebServer server(80);


// Funktionsprototypen
void handleRoot();             
void handleLED();
void handleNotFound();






void setup() 
{

  //Pinbelegung 
  pinMode(D3, OUTPUT);


  Serial.begin(115200);
  delay(500);
  Serial.println('\n');
  Serial.print("Configuring access point...");
  /* You can remove the password parameter if you want the AP to be open. */
  WiFi.softAP(ssid, password);

  IPAddress myIP = WiFi.softAPIP();
  Serial.print("AP IP address: ");                    // funktioniert in bad neustadt
  Serial.println(myIP);
  Serial.print("IP address of the webserver: ");      // funktioniert nicht
  Serial.println(WiFi.localIP());

 


    if (MDNS.begin("esp8266")) 
    {              
        // Start the mDNS responder for esp8266.local
        Serial.println("mDNS responder started");
    } 
    
    else 
    {
        Serial.println("Error setting up MDNS responder!");
    }

  server.on("/", HTTP_GET, handleRoot);     // Call the 'handleRoot' function when a client requests URI "/"
  server.on("/LED", HTTP_POST, handleLED);  // Call the 'handleLED' function when a POST request is made to URI "/LED"
  server.onNotFound(handleNotFound);        // When a client requests an unknown URI (i.e. something other than "/"), call function "handleNotFound"

  server.begin();                           // Actually start the server
  Serial.println("HTTP server started");
  
}




void loop() 
{
  server.handleClient();
}






void handleRoot() 
{
  server.send(200, "text/html", "<form action=\"/LED\" method=\"POST\"><input type=\"submit\" value=\"Toggle LED\"> </form>"                          // hier anstelle von spezifischem HTML code eine externe HTML Datei einlesen und verwenden???
                                 "<br/r>"
                                "<form action=\"/LED\" method=\"POST\"><input type=\"submit\" value=\"Toggle LED\"> </form>" 
  
  
  
  
  
  );
  
}






void handleLED() 
{                          // If a POST request is made to URI /LED
  digitalWrite(D3,!digitalRead(D3));      // Change the state of the LED
  server.sendHeader("Location","/");        // Add a header to respond with a new location for the browser to go to the home page again
  server.send(303);                         // Send it back to the browser with an HTTP status 303 (See Other) to redirect
}





void handleNotFound()
{
  server.send(404, "text/plain", "404: Not found"); // Send HTTP status 404 (Not Found) when there's no handler for the URI in the request
}
