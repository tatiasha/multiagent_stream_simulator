package itmo.escience.streamsimulator.algorithms.signal_distr_ga

import java.util.Random

import itmo.escience.streamsimulator.entities.Node
import itmo.escience.streamsimulator.general.{Schedule, Context}
import itmo.escience.streamsimulator.scheduler.AppStats
import itmo.escience.streamsimulator.utilities.Misc
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory

/**
  * Created by mikhail on 29.03.2017.
  */
class SDFactory[TSchedule <: Schedule](ctx: Context[TSchedule], stats: AppStats) extends AbstractCandidateFactory[SDSolution] {
  override def generateRandomCandidate(random: Random): SDSolution = {
//    val partitionLimit = 3 // todo get from global context
//    val containerLimit = 4
    val nodes = ctx.env.resources
    val operators = ctx.workload.getOperators()
    var chroms = List[SDChromosome]()
    for (node <- nodes) {
      chroms :+= new SDChromosome(node.id, List[SDGene]())
    }
    for (op <- operators) {
      val opId = op.id
      val partitions = 1//random.nextInt(op.partitionsLimit) + 1
      for (p <- 0 until partitions) {
//        val availableChroms = chroms.filter(x => x.length < ctx.env.resource(x.nodeId).get.asInstanceOf[Node].containersLimit && availableCpuOnNode(x) > 0)
        val availableChroms = chroms//.filter(x => availableCpuOnNode(x) > 0)
//        if (availableChroms.isEmpty) {
//          throw new IllegalStateException("Not enough cpu on nodes, during the generation of random solution")
//        }
        val curChrom = Misc.getRandomElement[SDChromosome](availableChroms, random)
        val curNode = ctx.env.resource(curChrom.nodeId).get.asInstanceOf[Node]
        val availableCpu = curNode.cpuCount - curChrom.genes.foldLeft(0)((s, x) => s + x.sharedCpu + x.dedicatedCpu)
        val geneDedicatedCpu = 0 // random.nextInt(availableCpu) + 1
        var geneSharedCpu = 1
//        if (availableCpu - geneDedicatedCpu > 1) {
//          geneSharedCpu = random.nextInt(availableCpu - geneDedicatedCpu) + 1
//        }
        val newGene = new SDGene(opId, geneDedicatedCpu, geneSharedCpu)
        curChrom.addGene(newGene)
      }
    }
    new SDSolution(chroms)
  }

  def availableCpuOnNode(chrom: SDChromosome): Int = {
    ctx.env.resource(chrom.nodeId).get.asInstanceOf[Node].cpuCount - chrom.genes.foldLeft(0)((s, x) => s + x.sharedCpu + x.dedicatedCpu)
  }
}
