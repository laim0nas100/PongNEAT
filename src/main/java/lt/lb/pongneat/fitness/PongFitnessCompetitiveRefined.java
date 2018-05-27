/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat.fitness;

import lt.lb.neurevol.Evoliution.NEAT.interfaces.Fitness;
import lt.lb.pongneat.controllers.PongControllerBase;

/**
 *
 * @author Lemmin
 */
public class PongFitnessCompetitiveRefined extends PongFitnessCompetitiveByWinsAdvanced {

    @Override
    public int compareTo(Fitness t) {
        if (t instanceof PongFitnessCompetitiveRefined) {
            PongFitnessCompetitiveRefined p = (PongFitnessCompetitiveRefined) t;
            return Integer.compare(this.getWeightedWins(), p.getWeightedWins());

        }

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getWeightedWins() {
        if (this.weightedWins < 0) {
            this.weightedWins = this.score;
            for (String key : this.winSet) {
                PongFitnessBase get = PongControllerBase.fitnessMap.get(key);
                this.weightedWins += get.score;
            }

        }
        return this.weightedWins;
    }

}
