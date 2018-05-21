/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.concurrent.*;
import javafx.application.Platform;
import lt.lb.commons.FX.SceneManagement.Frame;
import lt.lb.commons.FX.SceneManagement.MultiStageManager;
import lt.lb.commons.Log;
import lt.lb.pongneat.pong.Pong;
import lt.lb.pongneat.pong.PongEngine;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class PongNeat {

    public static int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    public static MultiStageManager stageManager = new MultiStageManager();

    /**
     * @param args the command line arguments
     */
    static ScheduledExecutorService exe = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("fxml/pongMenu.fxml");

        Log.print(resource);
        try {
            Frame newFrame = stageManager.newFrame(resource, "PongNEAT");

            Platform.runLater(() -> {
                newFrame.getStage().show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

//        // TODO code application logic here
        Pong makeGame = Pong.makeGame();
        makeGame.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                PongEngine gameEngine = makeGame.gameEngine;
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
        exe.scheduleAtFixedRate(() -> {
            if (makeGame.gameEngine.running) {
                makeGame.gameEngine.updateGame();
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
        makeGame.setVisible(true);
    }

}
