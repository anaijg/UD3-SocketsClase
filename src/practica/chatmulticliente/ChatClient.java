package practica.chatmulticliente;
 // Paquete donde se encuentra la clase

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

// Clase principal que actúa como cliente del chat
public class ChatClient {
    private static final String SERVER = "127.0.0.1"; // Dirección del servidor (localhost)
    private static final int PORT = 12345; // Puerto del servidor

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER, PORT); // Se conecta al servidor
             DataInputStream input = new DataInputStream(socket.getInputStream()); // Flujo de entrada
             DataOutputStream output = new DataOutputStream(socket.getOutputStream()); // Flujo de salida
             Scanner scanner = new Scanner(System.in)) { // Escáner para leer la entrada del usuario

            // Solicita al usuario que introduzca su nombre
            System.out.print("Introduce tu nombre: ");
            String nombre = scanner.nextLine();
            output.writeUTF(nombre); // Envía el nombre al servidor

            // Hilo separado para recibir mensajes del servidor
            Thread receptorMensajes = new Thread(() -> {
                try {
                    while (true) {
                        String mensaje = input.readUTF(); // Recibe mensajes del servidor
                        System.out.println("\n" + mensaje); // Muestra el mensaje en la consola
                        System.out.print("> "); // Mantiene la línea de entrada limpia
                    }
                } catch (IOException e) {
                    System.out.println("Desconectado del servidor.");
                }
            });

            receptorMensajes.start(); // Inicia el hilo que escucha los mensajes del servidor

            // Bucle para enviar mensajes al servidor
            while (true) {
                System.out.print("> "); // Indicación para escribir un mensaje
                String mensajeAlServidor = scanner.nextLine(); // Captura el mensaje del usuario

                if (mensajeAlServidor.equalsIgnoreCase("salir")) { // Si el usuario escribe "salir", se desconecta
                    output.writeUTF("salir");
                    System.out.println("Has salido del chat.");
                    break;
                }

                output.writeUTF(mensajeAlServidor); // Envía el mensaje al servidor
            }
        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor.");
        }
    }
}
