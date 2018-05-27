/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat.controllers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lt.lb.neurevol.Evoliution.NEAT.Genome;
import lt.lb.pongneat.fitness.PongFitnessBase;
import lt.lb.pongneat.pong.Pong;
import lt.lb.pongneat.pong.PongEngine;

/**
 *
 * @author Lemmin
 */
public abstract class PongControllerBase {

    long frame = 0;

    public int totalScore;

    public static Map<String, PongFitnessBase> fitnessMap = new ConcurrentHashMap<>();

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

    public abstract List<Genome> getGenomes();

    public abstract void evaluateFitness();

    public abstract void advanceFrame();

    public static enum PongMove {
        UP, DOWN, STAY
    }

    protected PongMove decideMove(Double[] eval) {

        Double max = eval[0];
        int index = 0;
        for (int i = 1; i < eval.length; i++) {
            double val = eval[i];
            if (max < val) {
                max = val;
                index = i;
            }
        }

        switch (index) {
            case 0:
                return PongMove.UP;
            case 1:
                return PongMove.DOWN;
            default:
                return PongMove.STAY;

        }
    }

}
