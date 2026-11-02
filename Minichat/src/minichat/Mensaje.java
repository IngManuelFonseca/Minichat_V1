package minichat;

/** Un mensaje del chat: quién lo mandó, qué dice y cuándo. */
public class Mensaje {
    private final String remitente;
    private final String texto;
    private final long fecha; // milisegundos desde 1970 (System.currentTimeMillis)

    public Mensaje(String remitente, String texto, long fecha) {
        this.remitente = remitente;
        this.texto = texto;
        this.fecha = fecha;
    }

    public String getRemitente() { return remitente; }
    public String getTexto()     { return texto; }
    public long getFecha()       { return fecha; }
}
