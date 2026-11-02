package minichat;

public class formulario02 extends VentanaChat {

    public formulario02() {
        super("Usuario 2", 6000, 5000);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new formulario02().setVisible(true));
    }
}
