package minichat;

public class formulario01 extends VentanaChat {

    public formulario01() {
        super("Usuario 1", 5000, 6000);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new formulario01().setVisible(true));
    }
}
