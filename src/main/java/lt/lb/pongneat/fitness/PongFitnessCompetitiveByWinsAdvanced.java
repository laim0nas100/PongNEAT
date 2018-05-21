/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat.fitness;

import lt.lb.neurevol.Evoliution.NEAT.interfaces.Fitness;
import lt.lb.pongneat.PongControllerBase;

/**
 *
 * @author Lemmin
 */
public class PongFitnessCompetitiveByWinsAdvanced extends PongFitnessBase {

    public int weightedWins = -1;

    public int getWeightedWins() {
        if (weightedWins < 0) {
            weightedWins = this.winSet.size();
            for (String wonAgainst : this.winSet) {
                PongFitnessCompetitiveByWinsAdvanced get = (PongFitnessCompetitiveByWinsAdvanced) PongControllerBase.fitnessMap.get(wonAgainst);
                weightedWins += get.winSet.size();

            }

        }
        return weightedWins;
    }

    @Override
    public int compareTo(Fitness t) {
        if (t instanceof PongFitnessCompetitiveByWinsAdvanced) {
            PongFitnessCompetitiveByWinsAdvanced p = (PongFitnessCompetitiveByWinsAdvanced) t;

            if (this.score == p.score) {
                int myWins = this.getWeightedWins();
                int yourWins = p.getWeightedWins();
                return Integer.compare(myWins, yourWins);
            } else {
                return Integer.compare(this.score, p.score);
            }

        }

        throw new UnsupportedOperationException("Supported only for PongFitness"); //To change body of generated methods, choose Tools | Templates.
    }

    public String toString() {
        return this.getWeightedWins() + " " + this.score + " " + this.winSet.size();
    }

}
