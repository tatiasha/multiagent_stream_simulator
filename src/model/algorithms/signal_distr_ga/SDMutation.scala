package model.algorithms.signal_distr_ga

import java.util
import java.util.Random

import model.ResourceId
import model.entities.Node
import model.general.{Schedule, Context, NodeOperatorContainerBasedSchedule}
import model.utilities.Misc
import org.uncommons.watchmaker.framework.EvolutionaryOperator
import scala.collection.JavaConversions._

/**
  * Created by mikhail on 29.03.2017.
  */
class SDMutation[TSchedule <: Schedule](ctx: Context[TSchedule]) extends EvolutionaryOperator[SDSolution] {
  override def apply(list: util.List[SDSolution], random: Random): util.List[SDSolution] = {
    val mutants = new util.ArrayList[SDSolution]()
    val muts = 2
    for (candidate <- list) {
      var mutant: SDSolution = candidate
      if (ctx.env.resources.size > 1) { // TODO check this inside mutations
        for (m <- 0 until muts) {
          if (random.nextBoolean()) {
            mutant = mutate(mutant, random)
          } else {
            mutant = swap_mutate(mutant, random)
          }
        }
      }
      mutants.add(mutant)
    }
    mutants
  }

  def mutate(sol: SDSolution, random: Random) = {
    val temp = new SDTempChrome(sol, ctx.env.resources.map(x=>x.id))
    val chosenGene = Misc.getRandomElement(temp.genes, random)
    temp.genes = temp.genes.filter(x => x != chosenGene)
    temp.genes :+= (chosenGene._1, Misc.getRandomElement(temp.allNodes.filter(x => x!=chosenGene._2), random))
    temp.toSDSol()
  }

  def swap_mutate(sol: SDSolution, random: Random) = {
    val temp = new SDTempChrome(sol, ctx.env.resources.map(x=>x.id))
    val g1 = Misc.getRandomElement(temp.genes, random)
    val g2 = Misc.getRandomElement(temp.genes.filter(x=>x!=g1), random)
    val g1m = (g1._1, g2._2)
    val g2m = (g2._1, g1._2)
    temp.genes = temp.genes.filter(x => x != g1 && x != g2)
    temp.genes :+= g1m
    temp.genes :+= g2m
    temp.toSDSol()
  }

//  def mutate(sol: SDSolution, random: Random) = {
//    val options = List[Int](666)
//    var usedOptions = List[Int]()
//    var mutated = false
//    for (i <- options.indices) {
//      if (!mutated) {
//        val chosenOption = Misc.getRandomElement(options.filter(x => !usedOptions.contains(x)), random)
//        usedOptions :+= chosenOption
//        mutated = chosenOption match {
////          case 1 => removeContainer(sol, random)
////          case 2 => addContainer(sol, random)
//          case 666 => adaptMut(sol, random)
////          case 3 => changeNode(sol, random)
////          case 4 => moveContainer(sol, random)
////          case 5 => swapContainers(sol, random)
//        }
////        println(s"Mutated $chosenOption is $mutated")
//      }
//    }
//
//    sol
//  }

  // #1
  def removeContainer(sol: SDSolution, random: Random): Boolean = {
    val operators = ctx.workload.getOperators()
    val allGenes = sol.chromosomes.foldLeft(List[(ResourceId, SDGene)]())((s, x) => s ++ x.genes.map(y => (x.nodeId, y)))
    val groupedGenes = allGenes.groupBy(x => x._2.operatorId)
    val notSingleGenes = groupedGenes.filter(x => x._2.size > 1)
    if (notSingleGenes.isEmpty) {
      return false
    }
    val chosenGeneGroup = Misc.getRandomElement(notSingleGenes.values.toList, random)
    val chosenGene = Misc.getRandomElement(chosenGeneGroup, random)
    val chrom = sol.chromosomes.filter(x => x.nodeId == chosenGene._1).head
    chrom.genes = chrom.genes.filter(x => x != chosenGene._2)
    true
  }
  // #2
  def addContainer(sol: SDSolution, random: Random): Boolean = {
    val operators = ctx.workload.getOperators()
    val allGenes = sol.allGenes
    val groupedGenes = allGenes.groupBy(x => x.operatorId)
    val genesCount = groupedGenes.map(x => (x._1, x._2.size))
    val availableOperators = genesCount.filter(x => x._2 < ctx.workload.operatorMap.get(x._1).partitionsLimit).keys.toList
    if (availableOperators.isEmpty) {
      return false
    }
    val chosenOp = Misc.getRandomElement(availableOperators, random)

    val availableChroms = sol.chromosomes.filter(x => x.length < ctx.env.resource(x.nodeId).get.asInstanceOf[Node].containersLimit && availableCpuOnNode(x) > 0)
    if (availableChroms.isEmpty) {
      return false
    }
    val curChrom = Misc.getRandomElement[SDChromosome](availableChroms, random)
    val curNode = ctx.env.resource(curChrom.nodeId).get.asInstanceOf[Node]
    val availableCpu = curNode.cpuCount - curChrom.genes.foldLeft(0)((s, x) => s + x.sharedCpu + x.dedicatedCpu)
    val geneDedicatedCpu = random.nextInt(availableCpu) + 1
    var geneSharedCpu = 0
    if (availableCpu - geneDedicatedCpu > 1) {
      geneSharedCpu = random.nextInt(availableCpu - geneDedicatedCpu) + 1
    }
    val newGene = new SDGene(chosenOp, geneDedicatedCpu, geneSharedCpu)
    curChrom.addGene(newGene)
    true
  }
  // #3
  def changeNode(sol: SDSolution, random: Random): Boolean = {
    val chosenChrom = Misc.getRandomElement(sol.chromosomes, random)
    val nodeId = chosenChrom.nodeId
    val node = ctx.env.resource(nodeId).get.asInstanceOf[Node]
    val len = chosenChrom.length
    var genes = chosenChrom.genes
    var newGenes = List[SDGene]()
    var availableCpu = node.cpuCount
    while (genes.nonEmpty) {
      val gene = Misc.getRandomElement(genes, random)
      genes = genes.filter(x => x != gene)
      val totalCpuForGene = random.nextInt(availableCpu - genes.size) + 1
      availableCpu -= totalCpuForGene
      val geneDedicatedCpu = random.nextInt(totalCpuForGene + 1)
      val geneSharedCpu = totalCpuForGene - geneDedicatedCpu
      newGenes :+= new SDGene(gene.operatorId, geneDedicatedCpu, geneSharedCpu)
    }
    chosenChrom.genes = newGenes
    true
  }
  // #4
  def moveContainer(sol: SDSolution, random: Random): Boolean = {
    true
  }
  // #5
  def swapContainers(sol: SDSolution, random: Random): Boolean = {
    true
  }

  // #666
  def adaptMut(sol:SDSolution, random: Random): Boolean = {
    val chosenChrom = Misc.getRandomElement(sol.chromosomes.filter(x => x.length > 0), random)
    val anotherChrom = Misc.getRandomElement(sol.chromosomes.filter(x => x!= chosenChrom).filter(x=>x.length < 5), random)
    val gene = Misc.getRandomElement(chosenChrom.genes, random)
    chosenChrom.genes = chosenChrom.genes.filter(x => x != gene)
    anotherChrom.genes = chosenChrom.genes :+ gene
    true
  }

  def availableCpuOnNode(chrom: SDChromosome): Int = {
    ctx.env.resource(chrom.nodeId).get.asInstanceOf[Node].cpuCount - chrom.genes.foldLeft(0)((s, x) => s + x.sharedCpu + x.dedicatedCpu)
  }
}
