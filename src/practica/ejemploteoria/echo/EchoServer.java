package practica.ejemploteoria.echo;
// Define el paquete donde se encuentra la clase

 // Importa las clases necesarias para la entrada y salida de datos
 // Importa las clases necesarias para la comunicación de red

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// Define la clase EchoServer
public class EchoServer {
    private static final int PORT = 1432;
    // Define el número de puerto en el que el servidor escuchará

 // Método principal
 public static void main(String[] args) {

     try {
         try (ServerSocket servidor = new ServerSocket(PORT)) {      // Crea un ServerSocket para escuchar conexiones en el puerto especificado
             System.out.println("Servidor levantado esperando conexiones.");
             while (true) {
                 // Bucle infinito para aceptar continuamente conexiones de clientes
                 Socket socket = servidor.accept();
                 System.out.println("Conexión desde cliente aceptada. ");
             // Espera una conexión de cliente y la acepta

             // Crea un flujo de entrada para recibir datos del cliente

                 DataInputStream input = new DataInputStream(socket.getInputStream());

                 // HASTA AQUÍ LA RECEPCIÓN DE DATOS
                 // Crea un flujo de salida para enviar datos al cliente
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             // Lee una cadena codificada en UTF-8 enviada por el cliente
                 String mensajeCliente = input.readUTF();
             // Envía el mensaje recibido de vuelta al cliente
                 output.writeUTF(mensajeCliente);
             }
         }
     } catch (IOException e) {
         System.out.println("Ha fallado la conexión." + e.getMessage());
     }



 // Captura cualquier excepción de entrada/salida
 // Imprime la traza de la excepción
 }
 }