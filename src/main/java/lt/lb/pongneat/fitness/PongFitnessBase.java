/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat.fitness;

import java.util.HashSet;
import lt.lb.neurevol.Evoliution.NEAT.interfaces.Fitness;

/**
 *
 * @author Lemmin
 */
public abstract class PongFitnessBase implements Fitness {

    public String genomeID;
    public HashSet<String> winSet = new HashSet<>();
    public int score = 0;

}
