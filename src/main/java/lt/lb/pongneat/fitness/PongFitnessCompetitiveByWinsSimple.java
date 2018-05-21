/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat.fitness;

import lt.lb.neurevol.Evoliution.NEAT.interfaces.Fitness;

/**
 *
 * @author Lemmin
 */
public class PongFitnessCompetitiveByWinsSimple extends PongFitnessBase {

    @Override
    public int compareTo(Fitness t) {
        if (t instanceof PongFitnessBase) {
            PongFitnessBase p = (PongFitnessBase) t;
            if (winSet.size() == p.winSet.size()) {
                if (winSet.contains(p.genomeID)) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                if (winSet.size() > p.winSet.size()) {
                    return 1;
                } else {
                    return -1;
                }
            }

        }
        throw new UnsupportedOperationException("Supported only for PongFitness"); //To change body of generated methods, choose Tools | Templates.
    }

}
