package minichat;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

/** Escucha en un puerto y avisa (Observer) cada vez que llega un mensaje. */
public class Servidor extends Observable implements Runnable {
    private final int puerto;

    public Servidor(int puerto) {
        this.puerto = puerto;
    }

    @Override
    public void run() {
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor iniciado en el puerto " + puerto);
            while (true) {
                try (Socket sc = servidor.accept()) {
                    DataInputStream in = new DataInputStream(sc.getInputStream());
                    String remitente = in.readUTF();
                    String texto = in.readUTF();
                    long fecha = in.readLong();
                    this.setChanged();
                    this.notifyObservers(new Mensaje(remitente, texto, fecha));
                } catch (IOException ex) {
                    System.err.println("Error leyendo mensaje: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            System.err.println("No se pudo abrir el puerto " + puerto
                    + " (¿ya hay otra ventana usándolo?): " + ex.getMessage());
        }
    }
}
