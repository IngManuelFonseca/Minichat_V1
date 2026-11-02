package minichat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Envía un mensaje al otro usuario por socket. */
public class Cliente implements Runnable {
    private final int puerto;
    private final Mensaje mensaje;
    private final Runnable alFallar; // se ejecuta si el otro usuario no está conectado

    public Cliente(int puerto, Mensaje mensaje, Runnable alFallar) {
        this.puerto = puerto;
        this.mensaje = mensaje;
        this.alFallar = alFallar;
    }

    @Override
    public void run() {
        final String HOST = "127.0.0.1";
        try (Socket sc = new Socket()) {
            sc.connect(new InetSocketAddress(HOST, puerto), 2000);
            DataOutputStream out = new DataOutputStream(sc.getOutputStream());
            out.writeUTF(mensaje.getRemitente());
            out.writeUTF(mensaje.getTexto());
            out.writeLong(mensaje.getFecha());
        } catch (IOException ex) {
            if (alFallar != null) {
                alFallar.run();
            }
        }
    }
}
