/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lt.lb.neurevol.Evoliution.NEAT.Genome;
import lt.lb.neurevol.Evoliution.NEAT.interfaces.Fitness;
import lt.lb.neurevol.Misc.Pair;
import lt.lb.pongneat.pong.*;

/**
 *
 * @author Lemmin
 */
public class PongController {

    public static Map<String, PongFitness> fitnessMap = new ConcurrentHashMap<>();

    public static PongFitness getFitness(String id) {
        if (fitnessMap.containsKey(id)) {
            return fitnessMap.get(id);
        } else {
            PongFitness fit = new PongFitness();
            fit.genomeID = id;
            fitnessMap.put(id, fit);
            return fit;
        }
    }

    public Pair<Genome> genomes;
    public Pong game;

    public PongEngine getEngine() {
        return game.gameEngine;
    }

    public boolean isGameOver() {
        return this.getEngine().gameOver;
    }

    public void start() {
        this.getEngine().start();
    }

    public Fitness getFitness(boolean first) {
        if (first) {
            return this.genomes.g1.fitness;
        } else {
            return this.genomes.g2.fitness;
        }
    }

    public List<Genome> getGenomes() {
        ArrayList<Genome> list = new ArrayList<>();
        if (this.genomes.g1 != null) {
            list.add(this.genomes.g1);
        }
        if (this.genomes.g2 != null) {
            list.add(this.genomes.g2);
        }

        return list;
    }

    public void evaluateFitness() {
        int i = 0;
        for (Genome g : this.getGenomes()) {
//            this.evaluate(g, i++);
        }
    }

    private void evaluatePair() {
        Paddle p1 = this.getEngine().paddles.get(0);
        Paddle p2 = this.getEngine().paddles.get(1);

        PongFitness f1 = PongController.getFitness(this.genomes.g1.id);
        f1.score = p1.score;
        PongFitness f2 = PongController.getFitness(this.genomes.g2.id);
        f2.score = p2.score;

        if (p1.won) {
            f1.winSet.add(this.genomes.g2.id);
        } else {
            f2.winSet.add(this.genomes.g1.id);

        }
        this.genomes.g1.fitness = f1;
        this.genomes.g2.fitness = f2;

    }

    public void setFitness(boolean first, Fitness fit) {
        if (first) {
            this.genomes.g1.fitness = fit;
        } else {
            this.genomes.g2.fitness = fit;
        }
    }

    public double[] formatBoard(Integer[][] board) {
        double[] res = new double[board.length * board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                res[i * board[i].length + j] = board[i][j];
            }
        }
        return res;
    }

    public void advanceFrame() {
    }

//    public void makeMove() {
//
//        TetrisGame game = gm.game;
//        Integer[][] board;
//        board = game.getBoardNew();
//        double[] formatBoard = formatBoard(board);
//        double[] move = genomes.evaluate(formatBoard);
//        int regionSize = move.length / 4;
////        Log.print("In controller" +Arrays.toString(move));
//        int i = 0;
//        int max = 0;
//        double[] finalMove = new double[4];
//        for (; i < 4; i++) {
//            for (int j = regionSize * i; j < regionSize * (i + 1); j++) {
//                finalMove[i] += move[j];
//            }
//            finalMove[i] = finalMove[i] / regionSize;
//            if (finalMove[max] < finalMove[i]) {
//                max = i;
//            }
//        }
//        switch (max) {
//            case 0:
//                game.move(1);
//                break;
//            case 1:
//                game.move(-1);
//                break;
//            case 2:
//                game.dropDown();
//                game.score += 10;
//                break;
//            case 3:
//                game.rotate(1);
//                break;
//        }
//        game.dropDown();
//        game.score += 1;
//        movesMade++;
//
//    }
//
//    public double evaluateFitness() {
//        if (gm == null) {
//            return 0;
//        }
//        fitness = Math.max(fitness, game().score);
////        fitness /= Math.sqrt(movesMade);
//        genomes.fitness = new FloatFitness((float) fitness);
//        return fitness;
//    }
}