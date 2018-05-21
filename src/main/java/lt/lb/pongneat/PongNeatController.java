
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.pongneat;

import com.google.gson.Gson;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lt.lb.commons.Containers.Value;
import lt.lb.commons.DelayedLog;
import lt.lb.commons.FX.SceneManagement.BaseController;
import lt.lb.commons.Log;
import lt.lb.commons.Threads.*;
import lt.lb.neurevol.Evoliution.Control.Config;
import lt.lb.neurevol.Evoliution.NEAT.HyperNEAT.HyperGenome;
import lt.lb.neurevol.Evoliution.NEAT.HyperNEAT.HyperNEATSpace;
import lt.lb.neurevol.Evoliution.NEAT.*;
import lt.lb.neurevol.Evoliution.NEAT.imp.*;
import lt.lb.neurevol.Evoliution.NEAT.interfaces.*;
import lt.lb.neurevol.Misc.F;

/**
 * FXML Controller class
 *
 * @author Lemmin
 */
public class PongNeatController implements BaseController {

    public static final String logFile = "file.txt";

    @FXML
    public Label enqueueLabel;
    @FXML
    public Label generationLabel;
    @FXML
    public Label speciesLabel;
    @FXML
    public Label bestScoreLabel;
    @FXML
    public Label bestGenerationScoreLabel;
    @FXML
    public Label progressStagnationLabel;

    @FXML
    public TextField enqueueText;
    @FXML
    public TextField populationText;
    @FXML
    public TextField learningDelayText;
    @FXML
    public TextField generationText;
    @FXML
    public TextField resetFromBestAfterText;
    @FXML
    public TextField seedText;
    @FXML
    public TextField genomeLoggingText;
    @FXML
    public TextField hyperLayers;
    @FXML
    public TextField speciesText;

    @FXML
    public CheckBox useHyperNEAT;
    @FXML
    public CheckBox displayWhileLearning;
    @FXML
    public ComboBox usablePieces;

    public Gson g = new Gson();
    public int[] dim = new int[]{22, 10, 2};
    public HyperNEATSpace space = new HyperNEATSpace(dim);
    public AtomicInteger leftToEnqueue = new AtomicInteger(0);
    public AtomicInteger leftExecuting = new AtomicInteger(0);
    public boolean running = false;
    public boolean DISPLAY_WHILE_LEARNING = false;
    public long LEARNING_STEP_DELAY = 0;
    public static final long BEST_STEP_DELAY = 50;
    public static final int THREAD_COUNT = 4;
    public static int resetFromBestAfter = 10;
    public static int progressStagnation = -1;

    public String genomeLoggingFilePrefix;

    public Pool pool;
    public DynamicTaskExecutor exe = new DynamicTaskExecutor();
    public Genome best;

    public double averageScore;

    public double averageExternalScore;

    public PongControllerBase bestCt;

    public PongControllerBase bestExternal;

    public PongControllerBase generationBest;

    public PongControllerFactory factory;

    public PongControllerFactorySimple simpleFac = new PongControllerFactorySimple();

    public DelayedLog dlog = new DelayedLog();

    @Override
    public void initialize() {
        factory = new PongControllerFactoryCompetitive();
    }

    public void reset() {
        stop();
        pool = null;
        progressStagnation = 0;

    }

    @Override
    public void exit() {
        leftToEnqueue.set(0);
        exe.shutdown();
        Log.close();
        System.exit(0);
//        F.executor.shutdown();
    }

    public void load() {

    }

    @Override
    public void update() {
        int targetSpecies = 5;
        try {
            targetSpecies = Integer.parseInt(speciesText.getText());
            if (pool instanceof NeatPool) {
                ((NeatPool) pool).distinctSpecies = targetSpecies;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> {

            if (leftToEnqueue.get() < 0) {
                leftToEnqueue.set(0);
            }
            enqueueLabel.setText("" + leftToEnqueue.get());
            if (pool == null) {
                generationLabel.setText("0");
                speciesLabel.setText("0");
            } else {
                generationLabel.setText(pool.getGeneration() + "");
                if (pool instanceof NeatPool) {
                    speciesLabel.setText(((NeatPool) pool).species.size() + "");
                }

            }
            LEARNING_STEP_DELAY = Integer.parseInt(learningDelayText.getText());
            resetFromBestAfter = Integer.parseInt(resetFromBestAfterText.getText());
            if (progressStagnation >= 0) {
                progressStagnationLabel.setText(progressStagnation + "");
            }
            if (this.bestExternal != null) {
                this.bestScoreLabel.setText(bestExternal.totalScore + "");
            }
        });
    }

    public void init() throws Exception {
        Log.print("Init start");
        Promise p = new Promise(() -> {
            trueInit();
        });
        new Thread(p).start();
        p.get();
        Log.print("INIT DONE");
    }

    public void externalFitness() {
        Promise p = new Promise(() -> {

            PongControllerBase remade = simpleFac.remakeController(this.bestExternal);
            remade.game.setVisible(true);
            factory.makeRunnable(remade, new Value(20)).run();

        });
        new Thread(p).start();

    }

    public void trueInit() {

        int seed = 0;
        try {
            seed = Integer.parseInt(this.seedText.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }

        F.RND = new SecureRandom();
        F.RND.setSeed(seed);

        final int generationSize = Integer.parseInt(populationText.getText());
        int layers = 0;
        try {
            layers = Integer.parseInt(this.hyperLayers.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (layers < 2) {
            layers = 2;
        }
        final int fLayers = layers;
        GenomeMaker hyperNeatMaker = () -> {
            ArrayList<Genome> genomes = new ArrayList<>();
            dim = new int[]{dim[0], dim[1], fLayers};
            space = new HyperNEATSpace(dim);
            for (int i = 0; i < generationSize; i++) {
                HyperGenome genome = new HyperGenome(dim);
                genome.space = space;
                genomes.add(genome);
            }
            return genomes;
        };

        Log.print("Generation size:" + generationSize);
        GenomeMaker neatMaker = () -> {
            ArrayList<Genome> genomes = new ArrayList<>();
            for (int i = 0; i < generationSize / 2; i++) {
                Genome g = this.factory.produceBaseGenome();
                genomes.add(g);
                Log.print("Add genome " + i);
            }
            return genomes;
        };

        DefaultGenomeBreeder breeder = new DefaultGenomeBreeder();

        DefaultNEATMutator mutator = new DefaultNEATMutator();
        mutator.MUT_LINK = 0.8;
        mutator.MUT_NODE = 0.5;
        DefaultGenomeSimilarityEvaluator sim = new DefaultGenomeSimilarityEvaluator();
        DefaultGenomeSorter sorter = new DefaultGenomeSorter();
        DisposableExecutor determ = new DisposableExecutor(1);
        DisposableExecutor mainExe = new DisposableExecutor(4);

        Config conf = new Config() {

            @Override
            public Map<String, Double> getMap() {
                return new HashMap<>();
            }

            @Override
            public Pool getPool() {
                return pool;
            }

            @Override
            public GenomeMaker getGenomeMaker() {
                if (useHyperNEAT.isSelected()) {
                    return hyperNeatMaker;
                } else {
                    return neatMaker;
                }
            }

            @Override
            public GenomeBreeder getGenomeBreeder() {
                return breeder;
            }

            @Override
            public GenomeMutator getGenomeMutator() {
                return mutator;
            }

            @Override
            public GenomeSorter getGenomeSorter() {
                return sorter;
            }

            @Override
            public Species newSpecies() {
                Species spec = new Species();
                spec.conf = this;
                return spec;
            }

            @Override
            public Executor getSequentialExecutor() {
                return determ;
            }

            @Override
            public Executor getExecutor() {
                return mainExe;
            }

            @Override
            public GenomeSimilarityEvaluator getGenomeSimilarityEvaluator() {
                return sim;
            }
        };

        Pool p1 = new NeatPool(conf);
        Pool p2 = new NeatPool(conf);
        pool = new MultiPool(p1, p2);
//        pool = p1;
        this.exe.setRunnerSize(4);
//        pool = new Pool(22*10, 4, Integer.parseInt(populationText.getText()));

//        pool.stats.POPULATION = Integer.parseInt(populationText.getText());
//        pool.stats.INPUTS = 22*10;
//        pool.stats.OUTPUTS = 4;
//        pool.initializePool();
//        controllers = createControllers(pool);
//        learn(controllers);
        update();

    }

    public void enqueue() throws Exception {

        Log.print("Enqueue");
        int enq = Integer.parseInt(enqueueText.getText());

        this.enqueue(enq);
//        for (Genome g : pool.getPopulation()) {
//            Log.print(g.fitness);
//        }

    }

    public List<PongControllerBase> externalMakeControllers(List<PongControllerBase> contr) {
        List<PongControllerBase> controllers = new ArrayList<>();
        HashMap<String, Genome> genomes = new HashMap<>();

        for (PongControllerBase ctr : contr) {
            for (Genome g : ctr.getGenomes()) {
                if (!genomes.containsKey(g.id)) {
                    genomes.put(g.id, g);
                }
            }
        }
        for (Genome g : genomes.values()) {
            controllers.add(this.simpleFac.makeController(g));
        }
        return controllers;
    }

    public void enqueue(int amount) {

        this.leftToEnqueue.addAndGet(amount);
        if (this.running) {

            return;
        }

        this.exe.setRunnerSize(PongNeat.CORE_COUNT);
        this.running = true;
        Promise promise = new Promise(() -> {
            while (this.leftToEnqueue.decrementAndGet() > 0) {
                List<PongControllerBase> neat = neat();
                learn(neat);
                List<PongControllerBase> externalCheck = this.externalMakeControllers(neat);
                this.externalTests(externalCheck);
                this.externalLearn(externalCheck);

                this.dlog.log(logFile, this.pool.getGeneration() + ";" + this.averageScore + ";" + this.averageExternalScore + ";"
                              + this.generationBest.totalScore + ";" + this.bestCt.totalScore + ";"
                              + this.bestExternal.totalScore);

                update();
            }
        });

        Promise wait = new Promise(() -> {
            this.running = false;
        }).waitFor(promise);
        new Thread(promise).start();
        new Thread(wait).start();

    }

    public void learn(List<PongControllerBase> contr) {
        Log.print("Learn init ", contr.size());
        PongControllerBase bCt = contr.get(0);
        this.averageScore = 0d;
        for (PongControllerBase con : contr) {
            if (con.totalScore > bCt.totalScore) {
                bCt = con;
            }
            this.averageScore += con.totalScore;
        }
        this.generationBest = bCt;
        this.averageScore /= contr.size();

        progressStagnation++;
        if (this.bestCt == null || this.bestCt.totalScore < bCt.totalScore) {
            bestCt = bCt;
            progressStagnation = 0;

            Log.print("Assign best");
        }

        if (resetFromBestAfter <= 0) {
            return;
        } else {
            if (progressStagnation >= resetFromBestAfter) {
                progressStagnation = 0;
                this.resetFromBestNoStop();
            }
        }
    }

    public void externalTests(List<PongControllerBase> controllers) throws Exception {
        PongControllerBase.fitnessMap.clear();
        ArrayList<Promise> run = new ArrayList<>();
//        PongControllerSimple get = (PongControllerSimple) controllers.get(0);
//        get.print = true;
        for (PongControllerBase i : controllers) {
            i.game.setVisible(false);
            Promise execute = factory.makeRunnable(i).collect(run).execute(exe);
        }
        Promise wait = new Promise().waitFor(run).execute(exe);
        wait.get();
        run.clear();
        for (PongControllerBase i : controllers) {
            Promise execute = new Promise(() -> {
                i.evaluateFitness();
            }).collect(run).execute(exe);
        }
        wait = new Promise().waitFor(run).execute(exe);
        wait.get();
    }

    public void externalLearn(List<PongControllerBase> contr) {
        Log.print("Learn init ", contr.size());
        PongControllerBase bCt = contr.get(0);
        this.averageExternalScore = 0d;
        for (PongControllerBase con : contr) {
            if (con.totalScore > bCt.totalScore) {
                bCt = con;
            }
            this.averageExternalScore += con.totalScore;
        }
        this.averageExternalScore /= contr.size();

        if (this.bestExternal == null || this.bestExternal.totalScore < bCt.totalScore) {
            bestExternal = bCt;

            Log.print("Assign best");
        }
    }

    public List<PongControllerBase> neat() throws Exception {

        PongControllerBase.fitnessMap.clear();
        ArrayList<Promise> run = new ArrayList<>();
        List<PongControllerBase> controllers = factory.produceControllers(pool);
//        PongControllerSimple get = (PongControllerSimple) controllers.get(0);
//        get.print = true;
        for (PongControllerBase i : controllers) {
            i.game.setVisible(false);
            Promise execute = factory.makeRunnable(i).collect(run).execute(exe);
        }
        Promise wait = new Promise().waitFor(run).execute(exe);
        wait.get();
        run.clear();
        for (PongControllerBase i : controllers) {
            Promise execute = new Promise(() -> {
                i.evaluateFitness();
            }).collect(run).execute(exe);
        }
        wait = new Promise().waitFor(run).execute(exe);
        wait.get();
        pool.newGeneration();

        return controllers;

    }

    public void playBest() {
        Promise p = new Promise(() -> {
//            if (factory instanceof PongControllerFactorySimple) {
//                PongControllerFactorySimple fac = (PongControllerFactorySimple) factory;
//                PongControllerBase makeController = fac.makeController(best);
//                makeController.game.setVisible(true);
//                if (makeController instanceof PongControllerSimple) {
//                    ((PongControllerSimple) makeController).print = true;
//                }
//                Promise makeRunnable = fac.makeRunnable(makeController, new Value(50));
//                makeRunnable.run();
//            } else if (factory instanceof PongControllerFactoryCompetitive) {
//                PongControllerFactoryCompetitive fac = (PongControllerFactoryCompetitive) factory;
//                PongControllerCompetitive ctrl = fac.makeController(new Pair(this.best, this.secondBest));
//                ctrl.game.setVisible(true);
//                fac.makeRunnable(ctrl, new Value(50)).run();
//
//            }

            PongControllerBase remade = factory.remakeController(bestCt);
            remade.game.setVisible(true);
            factory.makeRunnable(remade, new Value(20)).run();

        });
        new Thread(p).start();

    }

    public void stop() {
        running = false;
        leftToEnqueue.set(0);
        leftExecuting.set(0);
        exe.stopEverything(false);
        update();
    }

    public void resetFromBest() {
        stop();
//        pool.newGeneration(pool.allTimeBest);
        resetFromBestNoStop();
    }

    public void resetFromBestNoStop() {
//        pool.newGeneration((Genome) pool.allTimeBest.clone());
    }

    public void save() throws FileNotFoundException, UnsupportedEncodingException {
        save(this.generationText.getText());
    }

    public void save(String where) throws FileNotFoundException, UnsupportedEncodingException {
//        pool.prepareToSerialize();
//        for(Genome genome:pool.getPopulation()){
//            genome.generateNetwork();
//        }
//        Log.print("All time best:" + pool.allTimeBest.fitness);
        String toJson = g.toJson(pool);
        lt.lb.commons.FileManaging.FileReader.writeToFile(where, Arrays.asList(toJson));
        Log.print("Saved as:" + where);
//        pool.restoreAfterSerialize();
    }

}
