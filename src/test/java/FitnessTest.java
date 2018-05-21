/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import lt.lb.commons.Log;
import lt.lb.pongneat.fitness.PongFitnessByScore;
import org.junit.*;

/**
 *
 * @author Lemmin
 */
public class FitnessTest {

    public FitnessTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void testFitness() {

        PongFitnessByScore f1 = new PongFitnessByScore();
        f1.score = 10;
        PongFitnessByScore f2 = new PongFitnessByScore();
        f2.score = 5;

        Log.print(f1.compareTo(f2));
    }
}
