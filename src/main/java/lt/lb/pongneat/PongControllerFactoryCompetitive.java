/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat;

import java.util.*;
import lt.lb.commons.Containers.Value;
import lt.lb.commons.Threads.Promise;
import lt.lb.commons.UUIDgenerator;
import lt.lb.neurevol.Evoliution.Coevolution.*;
import lt.lb.neurevol.Evoliution.NEAT.*;
import lt.lb.neurevol.Evoliution.NEAT.interfaces.Pool;
import lt.lb.neurevol.Misc.Pair;
import lt.lb.pongneat.pong.*;

public class PongControllerFactoryCompetitive extends PongControllerFactory {

    public PongControllerFactoryCompetitive() {
        this.ballCount = 2;
    }

    PairingProducer pairing = new CompleteRelative();

    @Override
    public int getInputCount() {
        return 2 + ballCount * 2;
    }

    @Override
    public int getOutputCount() {
        return 3;
    }

    @Override
    public Genome produceBaseGenome() {
        return new Genome(this.getInputCount(), this.getOutputCount());
    }

    @Override
    public List<PongControllerBase> produceControllers(Pool pool) {
        ArrayList<PongControllerBase> contr = new ArrayList<>();
        pool.getPopulation().forEach(g -> {
            g.id = UUIDgenerator.nextUUID("G");
        });

        if (pool instanceof MultiPool) {
            MultiPool p = (MultiPool) pool;
            List<List<Genome>> subpopulations = pool.getSubpopulations();
            Integer[] sizes = new Integer[subpopulations.size()];
            for (int i = 0; i < sizes.length; i++) {
                sizes[i] = subpopulations.get(i).size();
            }
            Collection<Pair<PairingInfo>> producePairs = pairing.producePairs(sizes);

            for (Pair<PairingInfo> pair : producePairs) {
                Genome g1 = subpopulations.get(pair.g1.subpopulaionIndex).get(pair.g1.memberIndex);
                Genome g2 = subpopulations.get(pair.g2.subpopulaionIndex).get(pair.g2.memberIndex);
                Pair<Genome> genomePair = new Pair<>(g1, g2);
                contr.add(this.makeController(genomePair));
            }

        } else if (pool instanceof NeatPool) {
            NeatPool p = (NeatPool) pool;
            Collection<Pair<PairingInfo>> producePairs = pairing.producePairs(p.getPopulation().size());
            ArrayList<Genome> pop = new ArrayList<>(pool.getPopulation());
            for (Pair<PairingInfo> pair : producePairs) {
                Genome g1 = pop.get(pair.g1.memberIndex);
                Genome g2 = pop.get(pair.g2.memberIndex);
                Pair<Genome> genomePair = new Pair<>(g1, g2);
                contr.add(this.makeController(genomePair));
            }

        }

        return contr;
    }

    @Override
    public Pong makeGame() {
        Pong p = Pong.makeGame();
        PongEngine e = p.gameEngine;
        e.speedUp = true;

        int h = Pong.height;
        int w = Pong.width;
        e.initCalled = true;
        e.balls = new ArrayList<>();
        e.paddles = new ArrayList<>();

        for (int i = 0; i < ballCount; i++) {
            Ball b = new Ball();
            b.radius = 10;
            b.dX = 5;
            b.dY = 5;
            b.x = w / 2;
            b.y = h / 2 + (10 * i) % Pong.height;
            b.speed = 9d;
            b.speedIncrement = 0.5;
            e.balls.add(b);
        }
        {
            Paddle p1 = new Paddle();
            p1.size = e.paddleSize * ballCount;
            p1.width = 10;
            p1.x = 10;

            p1.y = Pong.height / 2;
            p1.frontX = p1.x + p1.width;
            e.paddles.add(p1);
        }
        {
            Paddle p2 = new Paddle();
            p2.size = e.paddleSize * ballCount;
            p2.width = 10;

            p2.x = w - p2.width - 25;
            p2.y = h / 2;
            p2.frontX = p2.x;
            e.paddles.add(p2);
        }

        return p;

    }

    public PongControllerCompetitive makeController(Pair<Genome> pair) {
        PongControllerCompetitive contr = new PongControllerCompetitive();
        contr.game = this.makeGame();
//        if (F.RND.nextBoolean()) {
        contr.genomes = pair;
//        } else {
//            contr.genomes = new Pair(pair.g2, pair.g1);
//        }

        return contr;
    }

    public Promise makeRunnable(PongControllerBase contr, Value<Integer> sleep) {
        return new Promise(() -> {
            contr.start();
//            contr.game.setVisible(true);
            while (!contr.isGameOver()) {
                contr.advanceFrame();
                int s = sleep.get();
                if (s > 0) {
                    Thread.sleep(s);
                }
//                Thread.sleep(1);
            }

            contr.game.dispose();
        });
    }

    @Override
    public Promise makeRunnable(PongControllerBase contr) {
        return this.makeRunnable(contr, new Value<>(0));
    }

    @Override
    public PongControllerBase remakeController(PongControllerBase ctrl) {
        if (ctrl instanceof PongControllerCompetitive) {
            PongControllerCompetitive ct = (PongControllerCompetitive) ctrl;
            PongControllerCompetitive newCT = new PongControllerCompetitive();
            newCT.game = this.makeGame();
            newCT.genomes = ct.genomes;
            return newCT;
        }
        throw new IllegalArgumentException("Expected " + PongControllerCompetitive.class + " got: " + ctrl.getClass());
    }

}
