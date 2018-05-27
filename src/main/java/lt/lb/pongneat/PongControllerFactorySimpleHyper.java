/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat;

import java.util.ArrayList;
import java.util.List;
import lt.lb.commons.Containers.Value;
import lt.lb.commons.Threads.Promise;
import lt.lb.commons.UUIDgenerator;
import lt.lb.neurevol.Evoliution.NEAT.Genome;
import lt.lb.neurevol.Evoliution.NEAT.HyperNEAT.*;
import lt.lb.neurevol.Evoliution.NEAT.interfaces.Pool;
import lt.lb.pongneat.controllers.*;
import lt.lb.pongneat.pong.*;

public class PongControllerFactorySimpleHyper extends PongControllerFactory {

    SubstrateProducer1 prod = new SubstrateProducer1();
    Substrate subs = prod.getSubstrate();
    SubstrateToNNInfoProducer nnInfoProd = prod.getSubstrateToNNInfoProducer();
    ConnectionProducer conProd = prod.getConnectionProducer();

    PongControllerFactorySimpleHyper() {
        ballCount = 2;

    }

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
        return new HGenome(4, this.getOutputCount(),
                           subs, nnInfoProd, conProd);
    }

    @Override
    public List<PongControllerBase> produceControllers(Pool pool) {
        ArrayList<PongControllerBase> contr = new ArrayList<>();
        pool.getPopulation().forEach(g -> {

            g.id = UUIDgenerator.nextUUID("G");
            contr.add(this.makeController(g));
        });
        return contr;
    }

    public PongControllerBase makeController(Genome g) {
        if (g instanceof HGenome) {
            PongControllerHyperSimple s = new PongControllerHyperSimple();
            s.game = this.makeGame();
            s.genome = (HGenome) g;
            return s;
        }
        throw new RuntimeException("Unsupported genome");

    }

    @Override
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
            p2.size = w;
            p2.width = 10;

            p2.x = w - p2.width - 25;
            p2.y = h / 2;
            p2.frontX = p2.x;
            e.paddles.add(p2);
        }

        return p;

    }

    @Override
    public PongControllerBase remakeController(PongControllerBase ctrl) {
        return this.makeController(ctrl.getGenomes().get(0));
    }

}
