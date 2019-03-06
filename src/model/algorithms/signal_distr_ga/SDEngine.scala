package model.algorithms.signal_distr_ga

import java.io.PrintWriter
import java.util.Random

import model.general.{Schedule, OperatorInstanceBasedSchedule, Context}
import model.scheduler.AppStats
import model.utilities.ExpProps
import org.uncommons.maths.random.MersenneTwisterRNG
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection
import org.uncommons.watchmaker.framework._
import java.util

import org.uncommons.watchmaker.framework.termination.GenerationCount

/**
  * Created by mikhail on 29.03.2017.
  */
class SDEngine[TSchedule <: Schedule] {
  var engine: EvolutionEngine[SDSolution] = null
  var fitOutFile: PrintWriter = null

  def build(ctx: Context[TSchedule], stats: AppStats) = {
    val factory: SDFactory[TSchedule] = new SDFactory[TSchedule](ctx, stats)
    val operators: util.ArrayList[EvolutionaryOperator[SDSolution]] = new util.ArrayList[EvolutionaryOperator[SDSolution]]()
    operators.add(new SDCrossover(ctx))
    operators.add(new SDMutation(ctx))
    val pipeline: EvolutionaryOperator[SDSolution] = new EvolutionPipeline[SDSolution](operators)
    val fitnessEvaluator: FitnessEvaluator[SDSolution] = new SDFitnessEvaluator(ctx, stats)
    val selector: SelectionStrategy[Object] = new RouletteWheelSelection()
    val rng: Random = new Random()

    engine = new GenerationalEvolutionEngine[SDSolution](factory, pipeline, fitnessEvaluator, selector, rng)
//    engine = new SteadyStateEvolutionEngine[SDSolution](factory, pipeline, fitnessEvaluator, selector, 5, false, rng)

    val addObserver = true


    if (addObserver) {
      engine.addEvolutionObserver(new EvolutionObserver[SDSolution]() {
        def populationUpdate(data: PopulationData[_ <: SDSolution]) = {
          if (ExpProps.filepath != null) {
            if (data.getGenerationNumber == 0) {
              val init_best = data.getBestCandidate
              solutinToData(init_best, s"${ExpProps.filepath}\\init_solution.out") // TODO write the intial solution to file
            }
          }

          // TODO log details of best fitness
          try {
            if (data.getGenerationNumber == ExpProps.iterations - 1) {
              fitnessEvaluator.asInstanceOf[SDFitnessEvaluator[TSchedule]].isLastBest = true
            }
          } catch {
            case _: Exception =>
          }

          val best = data.getBestCandidate
//          val validity = SDProblem.checkValidity(best, ctx)
//          val isValid = validity._1 && validity._2 && validity._3
          val bestMakespan = fitnessEvaluator.getFitness(best, null)
          if (ExpProps.filepath != null) {
            fitOutFile.write(s"${data.getGenerationNumber}\t$bestMakespan\n") // TODO write fitness value into a file
          }

//          println(s"Generation ${data.getGenerationNumber}: fitness: $bestMakespan; valid: $isValid;\n\tvalid-invalid solutions: ${SDProblem.validSols}-${SDProblem.invalidSols}")
          println(s"Generation ${data.getGenerationNumber}: fitness: $bestMakespan")
        }
      })
    }
  }

  def run(popSize: Int, iterationCount: Int): SDSolution = {
    val seeds: util.ArrayList[SDSolution] = new util.ArrayList[SDSolution]()
    if (ExpProps.filepath != null) {
      println("wtf")
      fitOutFile = new PrintWriter(s"${ExpProps.filepath}\\ga_fitness.out") // todo write fitness in file
    }
    val result = engine.evolve(popSize, 1, seeds, new GenerationCount(iterationCount))
    if (ExpProps.filepath != null) {
      fitOutFile.close()
    }
    if (ExpProps.filepath != null) {
      solutinToData(result, s"${ExpProps.filepath}\\result_sol.out") // todo write solution to file
    }
    result
  }

  def solutinToData(sol: SDSolution, out: String) = {
    val filePrinter = new PrintWriter(out)
    for (chrom <- sol.chromosomes) {
      filePrinter.write(chrom.nodeId)
      for (gene <- chrom.genes) {
        filePrinter.write("\t" + gene.operatorId)
      }
      filePrinter.write("\n")
    }
    filePrinter.close()
  }
}
