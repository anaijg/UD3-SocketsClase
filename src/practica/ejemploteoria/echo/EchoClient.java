package practica.ejemploteoria.echo; // Define el paquete donde se encuentra la clase

// Importa las clases necesarias para la entrada y salida de datos
// Importa la clase Socket para la comunicación de red
// Importa la clase Scanner para la entrada de datos desde el usuario

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

// Define la clase EchoClient
public class EchoClient {
     // Dirección del servidor (localhost)
     private static final String SERVER_ADDRESS = "192.168.10.7";
     // Puerto del servidor
     private static final int PORT = 1432;

     // Método principal
     public static void main(String[] args) {
         // Crea un socket y se conecta al servidor
         try(Socket socket = new Socket(SERVER_ADDRESS, PORT);) {
             // Crea un flujo de entrada para recibir datos del servidor
             DataInputStream input = new DataInputStream(socket.getInputStream());
             // Crea un flujo de salida para enviar datos al servidor
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             // Crea un objeto Scanner para leer la entrada del usuario
             Scanner scanner = new Scanner(System.in);
             // Lee una línea de texto ingresada por el usuario
             String mensajeDesdeCliente = scanner.nextLine();
             // Envía el mensaje al servidor
             output.writeUTF(mensajeDesdeCliente);
             // Lee el mensaje recibido del servidor
             String mensajeDesdeServidor = input.readUTF();
             // Imprime el mensaje recibido del servidor
             System.out.println("Servidor: " + mensajeDesdeServidor);
             // Captura cualquier excepción de entrada/salida
             // Imprime la traza de la excepción

         } catch (IOException e) {
             System.out.println("Problema al conectar al servidor: " + e.getMessage());
             e.printStackTrace();
         }
     }

 }
