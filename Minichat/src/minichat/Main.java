package minichat;

/** Abre las dos ventanas de una sola vez (más cómodo para probar). */
public class Main {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            formulario01 f1 = new formulario01();
            formulario02 f2 = new formulario02();
            f1.setLocation(80, 80);
            f2.setLocation(540, 80);
            f1.setVisible(true);
            f2.setVisible(true);
        });
    }
}
