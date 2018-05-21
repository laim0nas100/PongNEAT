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
import lt.lb.commons.Log;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class PongEngine extends JPanel {

    String message = "Press space to start";

    public int ballCount = 1;

    public boolean speedUp = true;
    public boolean standard = false;

    /**
     * Size of the paddles
     */
    public int paddleSize = 90;

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
        } else if (p.y + p.size * 2 / 3 >= Pong.height) {
            p.y = Pong.height - p.size * 2 / 3;
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

    public int winScore = 50;
    public Ball losingBall;

    private Number addAbsolute(Number n, Number toAdd) {
        toAdd = Math.abs(toAdd.doubleValue());
        if (n.doubleValue() < 0) {
            return n.doubleValue() - toAdd.doubleValue();
        } else {
            return n.doubleValue() + toAdd.doubleValue();
        }
    }

    private void updateBall(Ball b) {
        if (Math.abs(b.dX) < 1) {
            throw new IllegalStateException("Ball is not moving");
        }

        // Update ball coordinates
        b.x += b.dX;
        b.y += b.dY;

        // Check for out of bounds (y)
        if (b.y + (b.radius * 3) > (Pong.height - b.radius)) {
            b.y = Pong.height - b.radius * 5;
            b.dY = -b.dY;
        }
        if (b.y - b.radius < 0) {
            double out = b.radius - b.y;
            b.y = b.radius + out;
            b.dY = -b.dY;
        }

        Paddle p1 = this.paddles.get(0);
        Paddle p2 = this.paddles.get(1);

        if (b.x > Pong.width) {
//            p1.score++;
            running = false;
            this.losingBall = b;
            gameOver();
            return;
        }
        if (b.x <= 0) {
//            p2.score++;
            this.losingBall = b;
            running = false;
            gameOver();

            return;
        }
        for (Paddle p : this.paddles) {
            if (p.equals(b.lastTouched)) {
                continue;
            }
            // Check if left paddle hits ball
            if (Math.abs(b.x - p.frontX) < b.radius && (b.y < p.y + p.size / 2) && (b.y > p.y - p.size / 2)) {

                b.dX = -b.dX;
//                b.dY = -b.dY;

                double halfWidth = Pong.width / 2d;
                if (b.x > halfWidth) {
                    p2.score++;
                } else {
                    p1.score++;
                }
//
                if (!this.standard) {
                    double halfSize = p.size / 2;
                    double hitSpot = p.y - b.y;
                    b.dY = -(hitSpot / (halfSize * 1.3)) * b.speed;
                    if (b.dY > 0) {
                        b.dY = Math.max(b.dY, 0.5);
                    } else {
                        b.dY = Math.min(b.dY, -0.5);
                    }

                    double x = Math.sqrt(b.speed * b.speed - b.dY * b.dY);
//                x = Math.max(x, 3d);

                    if (b.dX < 0) {
                        b.dX = -x;
                    } else {
                        b.dX = x;
                    }

                } else if (this.speedUp) {
                    if (Math.abs(b.dX) + Math.abs(b.dY) < b.speed) {
                        b.dX = this.addAbsolute(b.dX, b.speedIncrement).doubleValue();
                        b.dY = this.addAbsolute(b.dY, b.speedIncrement).doubleValue();
                    }

                }

                if (this.speedUp) {
                    b.speed += b.speedIncrement;
                    b.speed = Math.min(b.speed, 15);
                }

                b.lastTouched = p;
            }

        }
    }

    public void gameOver() {
        Paddle p1 = this.paddles.get(0);
        Paddle p2 = this.paddles.get(1);
        this.gameOver = true;

        double halfWidth = (double) Pong.width / 2;
        String loser = "Loser A";
        if (this.losingBall.x > halfWidth) {
            loser = "Loser B";
        }
        Log.println("Player A: " + p1.score + " | Player B: " + p2.score + " " + loser);
        for (Runnable run : gameOverTasks) {
            run.run();
        }
        this.initCalled = false;
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

        for (Paddle p : this.paddles) {
            if (p.score > this.winScore) {
                gameOver();
                return;
            }
        }

        /**
         * Repaint after the move
         */
        repaint();

    }

    public boolean initCalled = false;

    public void init() {
        if (initCalled) {
            return;
        }

        initCalled = true;
        this.balls = new ArrayList<>();
        this.paddles = new ArrayList<>();

        for (int i = 0; i < ballCount; i++) {
            Ball b = new Ball();
            b.radius = 10;
            b.dX = -5;
            b.dY = 5;
            b.x = Pong.width / 2 + i * 10;
            b.y = Pong.height / 2 + i * 10;
            b.speed = 9d;
            b.speedIncrement = 0.5;
            this.balls.add(b);
        }
        {
            Paddle p1 = new Paddle();
            p1.size = paddleSize;
            p1.width = 10;
            p1.x = 10;

            p1.y = Pong.height / 2;
            p1.frontX = p1.x + p1.width;
            this.paddles.add(p1);
        }
        {
            Paddle p2 = new Paddle();
            p2.size = paddleSize;
            p2.width = 10;

            p2.x = Pong.width - p2.width - 25;
            p2.y = Pong.height / 2;
            p2.frontX = p2.x;
            this.paddles.add(p2);
        }
    }

    /**
     * Start the game
     */
    public void start() {
        if (!running) {
            running = true;
            this.init();
//            aY = bY = getHeight() / 2;

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
            g.drawString(message, Pong.width / 2 - w / 2, Pong.width / 2 - h / 2);

        }
    }
}
