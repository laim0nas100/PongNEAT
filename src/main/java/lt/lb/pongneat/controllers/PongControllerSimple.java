/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat.controllers;

import java.util.*;
import lt.lb.commons.Log;
import lt.lb.neurevol.Evoliution.NEAT.Genome;
import lt.lb.neurevol.Misc.Pair;
import lt.lb.neurevol.Neural.NeuralNetwork;
import lt.lb.neurevol.Neural.Neuron;
import lt.lb.pongneat.fitness.PongFitnessByScore;
import lt.lb.pongneat.pong.*;

public class PongControllerSimple extends PongControllerBase {

    public Genome genome;

    public NeuralNetwork net;

    @Override
    public List<Genome> getGenomes() {
        List l = new ArrayList<>();
        l.add(this.genome);
        return l;
    }

    @Override
    public void evaluateFitness() {
        Paddle p1 = this.getEngine().paddles.get(0);
        Paddle p2 = this.getEngine().paddles.get(1);

        PongFitnessByScore f = new PongFitnessByScore();
        f.genomeID = genome.id;
        f.score = p1.score;

        PongControllerBase.fitnessMap.put(f.genomeID, f);

        genome.fitness = f;
        this.totalScore = p1.score + p2.score;
    }

    @Override
    public void advanceFrame() {
        this.frame++;

        if (this.net == null) {
            this.net = this.genome.generateNetwork();
        }

        if (this.getEngine().running) {
            this.getEngine().updateGame();
        }

        Paddle paddle1 = this.getEngine().paddles.get(0);
        Paddle wall = this.getEngine().paddles.get(1);

        Double[] ap = new Double[2 + this.getEngine().balls.size() * 2];

        double ay = ((double) paddle1.y / Pong.height);
        double wallY = (double) wall.y / Pong.height;
        ArrayList<Pair<Double>> ballCoords = new ArrayList<>();
        for (Ball ball : this.getEngine().balls) {
            Pair pair = new Pair((ball.x / Pong.width), (ball.y / Pong.height));
            ballCoords.add(pair);
        }

        //A perspective
        ap[0] = ay;
        ap[1] = wallY;

        int i = 2;
        for (Pair<Double> pair : ballCoords) {
            double x = pair.g1;
            double y = pair.g2;

            ap[i] = x;
            ap[i + 1] = y;
            i += 2;
        }

        Map<Integer, Double> inputMap = new HashMap<>();
        for (int j = 0; j < ap.length; j++) {
            inputMap.put(j, ap[j]);
        }

        Map<Integer, Neuron> evaluateByMap = net.evaluateByMap(inputMap);
        Double[] g1Eval = new Double[evaluateByMap.size()];
        for (Neuron n : evaluateByMap.values()) {
            g1Eval[n.ID - net.inputs] = n.value;
        }

//        Double[] g1Eval = net.evaluate(ap);
        PongMove moveA = this.decideMove(g1Eval);
        if (moveA == PongMove.UP) {
            this.getEngine().moveA(-10);

        } else if (moveA == PongMove.DOWN) {
            this.getEngine().moveA(10);
        }

        if (print) {
            Log.print(Arrays.toString(ap) + " -> " + Arrays.toString(g1Eval));
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

//        Double value = eval[0];
//
//        if (value > 0.3) {
//            return PongMove.UP;
//        }
//        if (value <= -0.3) {
//            return PongMove.DOWN;
//        }
//        return PongMove.STAY;
    }

}
