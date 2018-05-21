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
public class PongFitnessByScore extends PongFitnessBase {

    @Override
    public int compareTo(Fitness t) {
        if (t instanceof PongFitnessBase) {
            PongFitnessBase p = (PongFitnessBase) t;
//            if (this.score > p.score) {
//                return -1;
//            }
//            if (this.score < p.score) {
//                return 1;
//            }
//            return 0;
            return Integer.compare(this.score, p.score);

        }
        throw new UnsupportedOperationException("Supported only for PongFitness"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return this.score + "";
    }

}
