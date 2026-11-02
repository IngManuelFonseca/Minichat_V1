package minichat;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Ventana de chat con burbujas. Las dos ventanas (formulario01 y formulario02)
 * son esta misma clase con distinto nombre y puertos.
 */
public class VentanaChat extends JFrame implements Observer {

    // ---- Colores ----
    private static final Color VERDE_OSCURO = new Color(0x075E54);
    private static final Color FONDO_CHAT   = new Color(0xECE5DD);
    private static final Color BURBUJA_MIA  = new Color(0xDCF8C6);
    private static final Color BURBUJA_OTRO = Color.WHITE;
    private static final Color VERDE_BOTON  = new Color(0x128C7E);
    private static final Color GRIS_HORA    = new Color(0x7A7A7A);

    private static final Font FUENTE      = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FUENTE_HORA = new Font("SansSerif", Font.PLAIN, 10);
    private static final int ANCHO_MAX_BURBUJA = 260;

    private final String miNombre;
    private final int puertoDestino;
    private final BaseDatos bd = BaseDatos.getInstancia();
    // Un solo hilo para guardar en la BD, así los mensajes se guardan en orden
    private final ExecutorService guardador = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "guardador-mensajes");
        t.setDaemon(true);
        return t;
    });

    private final JPanel listaMensajes = new JPanel();
    private final JScrollPane scroll;
    private final JTextField txtTextoEnviar = new JTextField();
    private final JButton btnEnviar = new BotonRedondo("Enviar");

    public VentanaChat(String miNombre, int puertoPropio, int puertoDestino) {
        this.miNombre = miNombre;
        this.puertoDestino = puertoDestino;

        setTitle("Minichat - " + miNombre);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---- Encabezado ----
        JPanel encabezado = new JPanel(new GridLayout(2, 1));
        encabezado.setBackground(VERDE_OSCURO);
        encabezado.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel lblNombre = new JLabel(miNombre);
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel lblInfo = new JLabel("Los mensajes se guardan " + BaseDatos.HORAS_RETENCION + " horas");
        lblInfo.setForeground(new Color(0xCFE9E5));
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        encabezado.add(lblNombre);
        encabezado.add(lblInfo);
        add(encabezado, BorderLayout.NORTH);

        // ---- Zona de mensajes ----
        listaMensajes.setLayout(new BoxLayout(listaMensajes, BoxLayout.Y_AXIS));
        listaMensajes.setBackground(FONDO_CHAT);
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(FONDO_CHAT);
        contenedor.add(listaMensajes, BorderLayout.NORTH);
        scroll = new JScrollPane(contenedor);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // ---- Barra para escribir ----
        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.setBackground(new Color(0xF0F0F0));
        barra.setBorder(new EmptyBorder(8, 10, 8, 10));
        txtTextoEnviar.setFont(FUENTE);
        txtTextoEnviar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCCCCCC)),
                new EmptyBorder(6, 10, 6, 10)));
        btnEnviar.setPreferredSize(new Dimension(90, 36));
        btnEnviar.addActionListener(e -> enviar());
        barra.add(txtTextoEnviar, BorderLayout.CENTER);
        barra.add(btnEnviar, BorderLayout.EAST);
        add(barra, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnEnviar); // Enter envía

        setSize(420, 560);
        setMinimumSize(new Dimension(320, 300));

        // ---- Historial guardado en la base de datos ----
        List<Mensaje> historial = bd.cargarHistorial();
        for (Mensaje m : historial) {
            agregarBurbuja(m);
        }

        // ---- Empezar a escuchar al otro usuario ----
        Servidor s = new Servidor(puertoPropio);
        s.addObserver(this);
        Thread t = new Thread(s, "servidor-" + puertoPropio);
        t.setDaemon(true);
        t.start();
    }

    // ================= Enviar y recibir =================

    private void enviar() {
        String texto = txtTextoEnviar.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        Mensaje m = new Mensaje(miNombre, texto, System.currentTimeMillis());
        agregarBurbuja(m);
        txtTextoEnviar.setText("");
        txtTextoEnviar.requestFocusInWindow();

        guardador.execute(() -> bd.guardar(m));

        Runnable alFallar = () -> SwingUtilities.invokeLater(() ->
                agregarAviso("El otro usuario no está conectado. Verá el mensaje al abrir su ventana."));
        new Thread(new Cliente(puertoDestino, m, alFallar)).start();
    }

    /** Llega un mensaje del otro usuario (el que lo envió ya lo guardó en la BD). */
    @Override
    public void update(Observable o, Object arg) {
        Mensaje m = (Mensaje) arg;
        SwingUtilities.invokeLater(() -> agregarBurbuja(m));
    }

    // ================= Dibujar mensajes =================

    private void agregarBurbuja(Mensaje m) {
        boolean esMio = m.getRemitente().equals(miNombre);

        JPanel burbuja = new Burbuja(esMio ? BURBUJA_MIA : BURBUJA_OTRO);
        burbuja.setLayout(new BorderLayout(0, 2));
        burbuja.setBorder(new EmptyBorder(8, 12, 6, 12));

        JTextArea area = new JTextArea(m.getTexto());
        area.setFont(FUENTE);
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(null);
        int anchoTexto = Math.min(ANCHO_MAX_BURBUJA,
                area.getFontMetrics(FUENTE).stringWidth(m.getTexto()) + 6);
        area.setSize(new Dimension(Math.max(anchoTexto, 40), Short.MAX_VALUE));
        area.setPreferredSize(new Dimension(Math.max(anchoTexto, 40), area.getPreferredSize().height));
        burbuja.add(area, BorderLayout.CENTER);

        JLabel hora = new JLabel(formatearHora(m.getFecha()));
        hora.setFont(FUENTE_HORA);
        hora.setForeground(GRIS_HORA);
        hora.setHorizontalAlignment(SwingConstants.RIGHT);
        burbuja.add(hora, BorderLayout.SOUTH);

        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setBorder(new EmptyBorder(3, 10, 3, 10));
        fila.add(burbuja, esMio ? BorderLayout.EAST : BorderLayout.WEST);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));

        listaMensajes.add(fila);
        irAlFinal();
    }

    private void agregarAviso(String texto) {
        JLabel aviso = new JLabel(texto, SwingConstants.CENTER);
        aviso.setFont(new Font("SansSerif", Font.ITALIC, 11));
        aviso.setForeground(GRIS_HORA);
        aviso.setAlignmentX(Component.CENTER_ALIGNMENT);
        aviso.setBorder(new EmptyBorder(4, 10, 4, 10));
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(aviso, BorderLayout.CENTER);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
        listaMensajes.add(fila);
        irAlFinal();
    }

    private void irAlFinal() {
        listaMensajes.revalidate();
        listaMensajes.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar barra = scroll.getVerticalScrollBar();
            barra.setValue(barra.getMaximum());
        });
    }

    private static String formatearHora(long fecha) {
        Date d = new Date(fecha);
        String hoy = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String diaMensaje = new SimpleDateFormat("yyyyMMdd").format(d);
        // Si es de otro día, muestra también la fecha
        String formato = hoy.equals(diaMensaje) ? "HH:mm" : "dd/MM HH:mm";
        return new SimpleDateFormat(formato).format(d);
    }

    // ================= Componentes con estilo =================

    /** Panel con esquinas redondeadas (la burbuja del mensaje). */
    private static class Burbuja extends JPanel {
        private final Color color;

        Burbuja(Color color) {
            this.color = color;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Botón redondeado y de color (se ve igual en Windows, Mac y Linux). */
    private static class BotonRedondo extends JButton {
        BotonRedondo(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? VERDE_OSCURO : VERDE_BOTON);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
