/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat.controllers;

import lt.lb.neurevol.Evoliution.NEAT.HyperNEAT.SubstrateLayer.SLayerType;
import lt.lb.neurevol.Evoliution.NEAT.HyperNEAT.*;
import lt.lb.neurevol.Evoliution.NEAT.HyperNEAT.imp.HyperSpaceToSubstrateLayerTransformerImpl;
import lt.lb.neurevol.Evoliution.NEAT.HyperNEAT.imp.SubstrateToNNInfoProducerImpl;
import lt.lb.neurevol.Neural.NeuronInfo;
import lt.lb.neurevol.Neural.Synapse;
import lt.lb.pongneat.pong.Pong;

/**
 *
 * @author Lemmin
 */
public class SubstrateProducer1 {

    public static String PADDLES = "paddles";
    public static String BALLS = "balls";
    public static String HIDDEN = "hidden";
    public static String OUTPUT = "output";

    public SubstrateToNNInfoProducer getSubstrateToNNInfoProducer() {
        SubstrateToNNInfoProducer prod = new SubstrateToNNInfoProducerImpl();
        return prod;
    }

    public Substrate getSubstrate() {

        HyperSpaceToSubstrateLayerTransformer hssl = new HyperSpaceToSubstrateLayerTransformerImpl();
        HyperSpace paddleSpace = new HyperSpace(2, 1, 1);
        HyperSpace ballSpace = new HyperSpace(Pong.width / 10, Pong.height / 10, 1);
        HyperSpace hiddenSpace = new HyperSpace(10, 10, 1);
        HyperSpace outputSpace = new HyperSpace(1, 3, 1);

        SubstrateNeuronLayer paddles = hssl.produce(paddleSpace);
        paddles.type = SLayerType.INPUT;
        paddles.ID = PADDLES;
        SubstrateNeuronLayer balls = hssl.produce(ballSpace);
        balls.type = SLayerType.INPUT;
        balls.ID = BALLS;
        SubstrateNeuronLayer hidden = hssl.produce(hiddenSpace);
        hidden.type = SLayerType.HIDDEN;
        hidden.ID = HIDDEN;
        SubstrateNeuronLayer output = hssl.produce(outputSpace);
        output.type = SLayerType.OUTPUT;
        output.ID = OUTPUT;

        paddles.connectTo(hidden);
        balls.connectTo(hidden);
        hidden.connectTo(output);

        Substrate subs = new Substrate(paddles, balls, hidden, output);
        subs.makeGlobalIDs();
        return subs;

    }

    public ConnectionProducer getConnectionProducer() {
        ConnectionProducer conProd = (NeuronInfo in, NeuronInfo to, Double[] weights) -> {
            HyperNeuron hIn = (HyperNeuron) in;
            HyperNeuron hTo = (HyperNeuron) to;

            int use = 0;
            String inID = hIn.substrateLayer.ID;

            if (inID.equals(PADDLES)) {
                use = 0;
            }
            if (inID.equals(BALLS)) {
                use = 1;
            }
            if (inID.equals(HIDDEN)) {
                use = 2;
            }
            double w = weights[use];

            if (Math.abs(w) > 0.2) {
                return new Synapse(hIn.id, hTo.id, w);
            }
            return null;

        };

        return conProd;
    }
}
