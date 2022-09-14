package simulate;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;

import parser.Values;
import parser.ast.Expression;
import parser.ast.ModulesFile;

import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import prism.PrismPrintStreamLog;

import simulator.SimulatorEngine;


/**
Based in large part on github/prismmodelchecker/prism-api/src/SimulateModel.java
*/

public class GetProbability
{
  public static void main(String[] args)
  {
    new GetProbability().run();
  }

  public void run()
  {
    try {

      int pathCount = 0;
      double recordHigh = 0.0f;
      int invalidSince = -2;

      boolean newInit = false;

      // give PRISM output a log file
      PrismLog mainLog = new PrismDevNullLog();

      // initialise (with an s) PRISM engine
      Prism prism = new Prism(mainLog);
      prism.initialise();

      // parse the prism model
      // For now, model.sm is the model file.
      ModulesFile modulesFile = prism.parseModelFile(new File("_FILEPATH_"));
      System.out.println("\n\n\n********************************\n");
      System.out.println("WORKING ON _FILEPATH_");
      System.out.println("\n********************************");
      // ModulesFile modulesFile = prism.parseModelFile(new File("../model.sm"));

      // load the prism model
      prism.loadPRISMModel(modulesFile);

      // if the model has unknown constant values, deal with that here.
      // for now, we assume NO UNDEFINED CONSTANTS

      // load the model into the simulator
      prism.loadModelIntoSimulator();
      SimulatorEngine sim = prism.getSimulator();

      // initialize total model probability (lower bound) to zero
      double totalProbability = 0.0;
		
      // read the CSL property for making sure we end up in a target state
      // FileReader fr_p = new FileReader("DonovanYeastPolarization/yeastPolarization.sm");
      // BufferedReader br_p = new BufferedReader(fr_p);
      String x_p = "_PROPERTY_";
      // x_p = br_p.readLine();
      Expression target = prism.parsePropertiesString(x_p).getProperty(0);

      // Read in the traces
			// FileReader fr = new FileReader("trace_list.txt");
			// BufferedReader br = new BufferedReader(fr);
			// String x;

      // set up a placeholder, temporary index
      int index;

      int totalLength = 0;

      int countPathsUntil = 5000;

      int numberOfPaths = 0;

      for (int iii = 0; iii < 5000; iii++) {
        
        System.out.printf("Path %d - ", iii);
        
        // create a new path
        sim.createNewPath();
        sim.initialisePath(null);
        // sim.createNewOnTheFlyPath(); // recommended for efficiency but can break things
        
        
        // Take each transition and collect the rates
        double pathProbability = 1.0;
        double totalRate = 0.0;
        int pathLength = 0;

        // System.out.printf("%d length\n", tr_st.length);
        
        // Loop through each transition
        
        boolean keepGoing = true;

        while (!target.evaluateBoolean(sim.getCurrentState())) {

          totalRate = 0.0;
          
          for (int idx=0; idx < sim.getNumTransitions(); idx++) {
            totalRate += sim.getTransitionProbability(idx);
          }

          int desired = (int) (Math.random() * sim.getNumTransitions());

          // System.out.printf("desired %d out of %d\n", desired, sim.getNumTransitions());

          String tran_name = sim.getTransitionActionString(desired);
          // System.out.printf("%s\t", tran_name);

          double transition_probability = sim.getTransitionProbability(desired) / totalRate;
          pathProbability *= transition_probability;

          sim.manualTransition(desired);
          pathLength++;
          
        }
        System.out.printf("Length %d\n", pathLength);
        numberOfPaths += 1;
        totalProbability += pathProbability;
        totalLength += pathLength;
      }

      System.out.printf("Total Path Count: %d\n",numberOfPaths);
      System.out.printf("Total Path Length Sum: %d\n",totalLength);
      System.out.printf("Average Path Length: %f\n", (float) totalLength / countPathsUntil);
      System.out.printf("Total Probability: %e\n",totalProbability);

      System.out.println("\n\n\n");
      // close PRISM
      prism.closeDown();

    } 
    catch (FileNotFoundException e) {
			System.out.println("FileNotFound Error: " + e.getMessage());
			System.exit(1);
		} 
    catch (PrismException e) {
			System.out.println("PrismException Error: " + e.getMessage());
			System.exit(1);
		}
  }
}