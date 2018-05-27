/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat;

import lt.lb.pongneat.controllers.PongControllerBase;
import java.util.HashMap;
import java.util.List;
import lt.lb.commons.Containers.BasicProperty;
import lt.lb.commons.Containers.Value;
import lt.lb.commons.Threads.Promise;
import lt.lb.neurevol.Evoliution.NEAT.Genome;
import lt.lb.neurevol.Evoliution.NEAT.interfaces.Pool;
import lt.lb.pongneat.pong.Pong;

/**
 *
 * @author Lemmin
 */
public abstract class PongControllerFactory {

    public HashMap<String, Object> params;

    public int ballCount = 1;
    public BasicProperty<Boolean> visible = new BasicProperty<>(true);

    public abstract int getInputCount();

    public abstract int getOutputCount();

    public abstract Genome produceBaseGenome();

    public abstract List<PongControllerBase> produceControllers(Pool pool);

    public abstract PongControllerBase remakeController(PongControllerBase ctrl);

    public abstract Pong makeGame();

    public abstract Promise makeRunnable(PongControllerBase contr);

    public abstract Promise makeRunnable(PongControllerBase contr, Value<Integer> sleep);

}
