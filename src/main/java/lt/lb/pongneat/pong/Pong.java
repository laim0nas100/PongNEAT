package lt.lb.pongneat.pong;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

public class Pong extends JFrame {

    public static class PongConfig {

        public boolean visible;
        public int ballCount = 2;

    }

    public PongEngine gameEngine;

    public static final int height = 600;
    public static final int width = 800;

    public Pong(boolean init) {

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        gameEngine.start();
                        break;
                    case KeyEvent.VK_UP:
                        gameEngine.moveB(-5);
                        break;
                    case KeyEvent.VK_DOWN:
                        gameEngine.moveB(5);
                        break;
                    case KeyEvent.VK_E:
                        gameEngine.moveA(-5);
                        break;
                    case KeyEvent.VK_D:
                        gameEngine.moveA(5);
                        break;
                }
            }
        });

    }

    public Pong() {

    }

    public static Pong makeGame() {
        Pong p = new Pong();
        p.gameEngine = new PongEngine();
        p.setTitle("Pong");
        p.setLocationRelativeTo(null);
//        p.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        p.setResizable(false);

        p.add(p.gameEngine);
        p.gameEngine.setSize(width, height);
        p.setSize(width, height);
        return p;
    }

//    public static void main(String[] args) {
//        JFrame mainWindow = new Pong();
//        mainWindow.setVisible(true);
//    }
}
