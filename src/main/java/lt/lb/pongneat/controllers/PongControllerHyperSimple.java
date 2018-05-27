/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat.controllers;

import java.util.*;
import lt.lb.neurevol.Evoliution.NEAT.Genome;
import lt.lb.neurevol.Evoliution.NEAT.HyperNEAT.*;
import lt.lb.neurevol.Misc.Pos;
import lt.lb.neurevol.Neural.NeuralNetwork;
import lt.lb.neurevol.Neural.Neuron;
import lt.lb.pongneat.fitness.PongFitnessByScore;
import lt.lb.pongneat.pong.*;

public class PongControllerHyperSimple extends PongControllerBase {

    public HGenome genome;
    public boolean print;

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
            this.net = this.genome.generateEvaluatingNetwork();
        }

        if (this.getEngine().running) {
            this.getEngine().updateGame();
        }

        Paddle paddle1 = this.getEngine().paddles.get(0);
        Paddle wall = this.getEngine().paddles.get(1);

        Map<Integer, HyperNeuron> produceInputMap = genome.subs.produceInputMap();

        Map<Integer, HyperNeuron> paddleInputs = new HashMap<>();

        Map<Integer, HyperNeuron> ballInputs = new HashMap<>();

        for (HyperNeuron n : produceInputMap.values()) {
            String layerID = n.substrateLayer.ID;
            if (layerID.equals(SubstrateProducer1.PADDLES)) {
                paddleInputs.put(n.id, n);
            } else if (layerID.equals(SubstrateProducer1.BALLS)) {
                ballInputs.put(n.id, n);
            }
        }

        Map<Integer, Double> inputMap = new HashMap<>();

        // get ball inputs
        for (HyperNeuron n : paddleInputs.values()) {
            Double paddleInput = (double) wall.y / Pong.height;
            if (n.position.get()[0] == 0) {
                paddleInput = (double) paddle1.y / Pong.height;
            }
            inputMap.put(n.id, paddleInput);
        }
        SubstrateNeuronLayer ballsLayer = (SubstrateNeuronLayer) this.genome.subs.layers.get(SubstrateProducer1.BALLS);

        for (Ball ball : this.getEngine().balls) {
            Pos p = new Pos((ball.x / 10), (ball.y / 10));
            HyperNeuron n = ballsLayer.getClosestNeuronByPosisition(p);
            inputMap.put(n.id, 1d);
        }

        Map<Integer, Neuron> g1Eval = net.evaluateByMap(inputMap);

        Double[] eval = new Double[g1Eval.size()];
        int i = 0;
        for (Neuron d : g1Eval.values()) {
            eval[i++] = d.value;
        }

        PongMove moveA = this.decideMove(eval);
        if (moveA == PongMove.UP) {
            this.getEngine().moveA(-10);

        } else if (moveA == PongMove.DOWN) {
            this.getEngine().moveA(10);
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
