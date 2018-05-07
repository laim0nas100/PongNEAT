/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat.pong;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class PongEngine extends JPanel {

    String message = "Press space to start";

    int ballCount = 1;

    /**
     * Size of the paddles
     */
    static int paddleSize = 80;

    /**
     * Are we running or not
     */
    public boolean running = false;

    public boolean gameOver = false;

    public ArrayList<Ball> balls = new ArrayList<>();

    public ArrayList<Paddle> paddles = new ArrayList<>();

    /**
     * Move player A up/down by d pixels
     */
    public void moveA(int d) {
        movePaddle(this.paddles.get(0), d);
    }

    /**
     * Move player B up/down by d pixels
     */
    public void moveB(int d) {
        movePaddle(this.paddles.get(1), d);
    }

    public void movePaddle(Paddle p, int d) {
        p.y += d;

        // Player B right paddle cannot leave the screen from top or bottom
        if (p.y <= p.size / 2) {
            p.y = p.size / 2;
        } else if (p.y + p.size >= getHeight() + p.size / 2) {
            p.y = getHeight() - p.size / 2;
        }
    }

    public PongEngine() {
        super();
//        // Compute the new ball coordinates
//        Timer timer = new Timer(50, (ActionEvent e) -> {
//            if (running) {
//                updateGame();
//            }
//        });
//        timer.start();
    }

    private void updateBall(Ball b) {

        double halfSize = paddleSize / 2;
        // Update ball coordinates
        b.x += b.dX;
        b.y += b.dY;

        // Check for out of bounds (y)
        if (b.y + b.radius > getHeight()) {
            double out = b.y + b.radius - getHeight();
            b.y = getHeight() - b.radius - out;
            b.dY = -b.dY;
        }
        if (b.y - b.radius < 0) {
            double out = b.radius - b.y;
            b.y = b.radius + out;
            b.dY = -b.dY;
        }

        Paddle p1 = this.paddles.get(0);
        Paddle p2 = this.paddles.get(1);
        for (Paddle p : this.paddles) {
            if (p.equals(b.lastTouched)) {
                continue;
            }
            // Check if left paddle hits ball
            if (Math.abs(b.x - p.frontX) < b.radius && (b.y < p.y + p.size / 2) && (b.y > p.y - p.size / 2)) {

                b.dX = -b.dX;

                double hitSpot = p.y - b.y;
                b.dY = -(hitSpot / halfSize) * b.speed;

                double x = Math.sqrt(Math.pow(b.speed, 2) - Math.pow(b.dY, 2));
                if (b.dX < 0) {
                    b.dX = -x;
                } else {
                    b.dX = x;
                }
                b.speed += b.speedIncrement;
                b.lastTouched = p;

            }

            if (b.x > getWidth()) {
                p1.score++;
                System.out.println("Player A: " + p1.score + " | Player B: " + p2.score);
                running = false;
                gameOver();
            }
            if (b.x <= 0) {
                p2.score++;
                System.out.println("Player A: " + p1.score + " | Player B: " + p2.score);
                running = false;
                gameOver();
            }
        }
    }

    public void gameOver() {
        this.gameOver = true;
        for (Runnable run : gameOverTasks) {
            run.run();
        }
    }

    public ArrayList<Runnable> gameOverTasks = new ArrayList<>();

    /**
     * Update the game
     */
    public void updateGame() {
        // Update ball coordinates
        for (Ball b : balls) {
            this.updateBall(b);
        }

        /**
         * Repaint after the move
         */
        repaint();

    }

    /**
     * Start the game
     */
    public void start() {
        if (!running) {
            running = true;
//            aY = bY = getHeight() / 2;

            this.balls = new ArrayList<>();
            this.paddles = new ArrayList<>();

            for (int i = 0; i < ballCount; i++) {
                Ball b = new Ball();
                b.radius = 10;
                b.dX = -5;
                b.dY = 5;
                b.x = this.getWidth() / 2 * Math.random();
                b.y = this.getHeight() / 2 * Math.random();
                b.speed = 9d;
                b.speedIncrement = 0.5;
                this.balls.add(b);
            }
            {
                Paddle p1 = new Paddle();
                p1.size = paddleSize;
                p1.width = 10;
                p1.x = 10;

                p1.y = getHeight() / 2;
                p1.frontX = p1.x + p1.width;
                this.paddles.add(p1);
            }
            {
                Paddle p2 = new Paddle();
                p2.size = paddleSize;
                p2.width = 10;

                p2.x = getWidth() - p2.width - 25;
                p2.y = getHeight() / 2;
                p2.frontX = p2.x;
                this.paddles.add(p2);
            }
            //            // Draw the paddles
//            g.setColor(Color.BLUE);
//            g.fillRect(5, aY - paddleSize / 2, 15, paddleSize);
//            g.setColor(Color.BLUE);
//            g.fillRect(getWidth() - 20, bY - paddleSize / 2, 15, paddleSize);

            repaint();
        }
    }

    private void drawBall(Ball b, Graphics g) {
        int x = (int) (b.x - b.radius);
        int y = (int) (b.y - b.radius);
        int size = (int) (2 * b.radius);
        g.setColor(Color.RED);
        g.fillOval(x, y, size, size);
    }

    private void drawPaddle(Paddle p, Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(p.x, p.y - (p.size / 2), p.width, p.size);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (running) {
            // Draw the ball
            for (Ball b : balls) {
                drawBall(b, g);
            }

            for (Paddle p : paddles) {
                drawPaddle(p, g);
            }

        } else {
            // If not running display a message
            g.setColor(Color.BLACK);
            int h = g.getFontMetrics().getHeight();
            int w = g.getFontMetrics().stringWidth(message);
            g.drawString(message, getWidth() / 2 - w / 2, getHeight() / 2 - h / 2);

        }
    }
}
