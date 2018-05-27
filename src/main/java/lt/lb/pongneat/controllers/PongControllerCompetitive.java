/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat.controllers;

import java.util.ArrayList;
import java.util.List;
import lt.lb.neurevol.Evoliution.NEAT.Genome;
import lt.lb.neurevol.Misc.Pair;
import lt.lb.neurevol.Neural.NeuralNetwork;
import lt.lb.pongneat.fitness.PongFitnessBase;
import lt.lb.pongneat.fitness.PongFitnessByScore;
import lt.lb.pongneat.pong.*;

/**
 *
 * @author Lemmin
 */
public class PongControllerCompetitive extends PongControllerBase {

    public Pair<Genome> genomes;

    public NeuralNetwork net1, net2;

    @Override
    public void evaluateFitness() {
        Paddle p1 = this.getEngine().paddles.get(0);
        Paddle p2 = this.getEngine().paddles.get(1);

        PongFitnessBase f1 = new PongFitnessByScore();
        PongFitnessBase f2 = new PongFitnessByScore();

        f1.genomeID = this.genomes.g1.id;
        f2.genomeID = this.genomes.g2.id;

        if (PongControllerBase.fitnessMap.containsKey(f1.genomeID)) {
            f1 = PongControllerBase.fitnessMap.get(f1.genomeID);
        }
        if (PongControllerBase.fitnessMap.containsKey(f2.genomeID)) {
            f2 = PongControllerBase.fitnessMap.get(f2.genomeID);
        }

        if (p1.won) {
            f1.winSet.add(f2.genomeID);
        } else {
            f2.winSet.add(f1.genomeID);

        }
        f1.score += p1.score;
        f2.score += p2.score;
        this.genomes.g1.fitness = f1;
        this.genomes.g2.fitness = f2;

        PongControllerBase.fitnessMap.put(f1.genomeID, f1);
        PongControllerBase.fitnessMap.put(f2.genomeID, f2);

        this.totalScore = p1.score + p2.score;

    }

    @Override
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

    @Override
    public void advanceFrame() {
        if (net1 == null) {
            net1 = this.genomes.g1.generateNetwork();
        }
        if (net2 == null) {
            net2 = this.genomes.g2.generateNetwork();
        }

        Paddle paddle1 = this.getEngine().paddles.get(0);
        Paddle paddle2 = this.getEngine().paddles.get(1);

        Double[] ap = new Double[2 + this.getEngine().balls.size() * 2];
        Double[] bp = new Double[ap.length];

        double ay = (double) paddle1.y / Pong.height;
        double by = (double) paddle2.y / Pong.height;

        ArrayList<Pair<Double>> ballCoords = new ArrayList<>();
        for (Ball ball : this.getEngine().balls) {
            ballCoords.add(new Pair(ball.x / Pong.width, ball.y / Pong.height));
        }

        //A perspective
        ap[0] = ay;
        ap[1] = by;

        bp[0] = by;
        bp[1] = ay;

        int i = 2;
        for (Pair<Double> pair : ballCoords) {
            double x = pair.g1;
            double y = pair.g2;

            ap[i] = x;
            ap[i + 1] = y;
            bp[i] = 1 - x;//mirror X
            bp[i + 1] = y;
            i += 2;
        }

        Double[] g1Eval = net1.evaluate(ap);
        Double[] g2Eval = net2.evaluate(bp);

        PongMove moveA = this.decideMove(g1Eval);
        if (moveA == PongMove.UP) {
            this.getEngine().moveA(-10);

        } else if (moveA == PongMove.DOWN) {
            this.getEngine().moveA(10);
        }

        PongMove moveB = this.decideMove(g2Eval);
        if (moveB == PongMove.UP) {
            this.getEngine().moveB(-10);

        } else if (moveB == PongMove.DOWN) {
            this.getEngine().moveB(10);
        }

        if (this.getEngine().running) {
            this.getEngine().updateGame();
        }

        //my Y
        //enemy Y
        //ball 1 y
        //ball 1 x
        //ball 2 y
        //ball 2 x
        //ball n y
        //ball n x
        //moves nothing, left, right
    }

    @Override
    protected PongMove decideMove(Double[] eval) {
        return super.decideMove(eval);
    }

}
