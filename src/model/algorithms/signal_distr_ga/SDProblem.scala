package model.algorithms.signal_distr_ga

import java.io.PrintWriter
import java.util
import java.util.Random

import model._
import model.entities.Node
import model.general.{NodeOperatorContainerBasedScheduleImproved, NodeOperatorContainerBasedSchedule, Schedule, Context}
import model.utilities.Misc
import org.uncommons.maths.random.MersenneTwisterRNG

/**
  * Created by mikhail on 30.03.2017.
  */
object SDProblem {
  val rnd = new Random()
  var validSols = 0
  var invalidSols = 0

  def repairSolution[TSchedule <: Schedule](sol: SDSolution, ctx: Context[TSchedule]): SDSolution = {
    val validity = checkValidity[TSchedule](sol, ctx)
    val isValid = validity._1 && validity._2 && validity._3
    if (isValid) {
      validSols += 1
      return sol
    } else {
      invalidSols += 1
    }
    // join same operators
//    for (chrom <- sol.chromosomes) {
//      val node = ctx.env.resource(chrom.nodeId).get.asInstanceOf[Node]
//      val genes = chrom.genes
//      val genesGrouped = genes.groupBy(x => x.operatorId)
//      val joinedGenes = genesGrouped.map(x => new SDGene(x._1, x._2.map(y => y.dedicatedCpu).sum, x._2.map(y => y.sharedCpu).sum)).toList
//      chrom.genes = joinedGenes
//    }
    // remove overpartitioned
    val operators = ctx.workload.getOperators()
    var allGenes = sol.allGenes
    val operatorsCountMap = operators.map(x => x.id).map(x => (x, allGenes.count(g => g.operatorId == x)))
//    val invalidOperators = operatorsCountMap.filter(x => x._2 > ctx.workload.operatorMap.get(x._1).partitionsLimit)
//    for (item <- invalidOperators) {
//      for (i <- 0 until item._2 - ctx.workload.operatorMap.get(item._1).partitionsLimit) {
//        val opId = item._1
//        val opGenes = allGenes.filter(x => x.operatorId == opId)
//        val theChosenOne = Misc.getRandomElement(opGenes, rnd)
//        val chosenChroms = sol.chromosomes.filter(x => x.genes.contains(theChosenOne))
//        if (chosenChroms.isEmpty) {
//          throw new IllegalStateException("Gene for removing is not found in chromosomes")
//        }
//        if (chosenChroms.length > 1) {
//          throw new IllegalStateException("Gene for removing is found in several chromosomes")
//        }
//        val chosenChrom = chosenChroms.head
//        chosenChrom.genes = chosenChrom.genes.filter(x => x != theChosenOne)
//        allGenes = allGenes.filter(x => x != theChosenOne)
//      }
//    }

    // add empty operators
    val emptyOperators = operatorsCountMap.filter(x => x._2 == 0)
    for (item <- emptyOperators) {
      val opId = item._1
      val availableChroms = sol.chromosomes.filter(x => x.length < ctx.env.resource(x.nodeId).get.asInstanceOf[Node].containersLimit && availableCpuOnNode(x, ctx) > 0)
      if (availableChroms.isEmpty) {
        throw new IllegalStateException("Not enough cpu on nodes, during the generation of random solution")
      }
      val curChrom = Misc.getRandomElement[SDChromosome](availableChroms, rnd)
      val curNode = ctx.env.resource(curChrom.nodeId).get.asInstanceOf[Node]
      val availableCpu = curNode.cpuCount - curChrom.genes.foldLeft(0)((s, x) => s + x.sharedCpu + x.dedicatedCpu)
      val geneDedicatedCpu = 0 // rnd.nextInt(availableCpu) + 1
      var geneSharedCpu = 1
//      if (availableCpu - geneDedicatedCpu > 1) {
//        geneSharedCpu = rnd.nextInt(availableCpu - geneSharedCpu) + 1
//      }
      val newGene = new SDGene(opId, geneDedicatedCpu, geneSharedCpu)
      curChrom.genes :+= newGene
    }

    // repair nodes limits
//    for (chrom <- sol.chromosomes) {
//      val node = ctx.env.resource(chrom.nodeId).get.asInstanceOf[Node]
//      while (chrom.length > node.containersLimit) {
//        var chromIsMoved = false
//        val genesSize = chrom.length
//        var checkedGenes = List[SDGene]()
//        val availableCpusOnNodes = sol.chromosomes.filter(x => x.nodeId != node.id).map(x => (x, availableCpuOnNode[TSchedule](x, ctx)))
//        for (i <- 0 until genesSize) {
//          if (!chromIsMoved) {
//            val chosenGene = Misc.getRandomElement(chrom.genes.filter(x => !checkedGenes.contains(x)), rnd)
//            checkedGenes :+= chosenGene
////            val availableCpusOnNodes = sol.chromosomes.filter(x => x.nodeId != node.id).map(x => (x, availableCpuOnNode[TSchedule](x, ctx)))
//            val availableForGeneChroms = availableCpusOnNodes.filter(x => x._2 >= chosenGene.dedicatedCpu + chosenGene.sharedCpu && x._1.length < ctx.env.resource(x._1.nodeId).get.asInstanceOf[Node].containersLimit)
//            if (availableForGeneChroms.nonEmpty) {
//              val newChromForGene = Misc.getRandomElement(availableForGeneChroms, rnd)._1
//              newChromForGene.genes :+= chosenGene
//              chrom.genes = chrom.genes.filter(x => x!=chosenGene)
//              chromIsMoved = true
//            }
//          }
//        }
//        if (!chromIsMoved) {
//          val chosenGene = Misc.getRandomElement(chrom.genes, rnd)
//          val availableForGeneChroms = availableCpusOnNodes.filter(x => x._2 > 0 && x._1.length < ctx.env.resource(x._1.nodeId).get.asInstanceOf[Node].containersLimit)
//          if (availableForGeneChroms.isEmpty) {
//            throw new IllegalStateException("Not enough cpu on nodes to move container from node")
//          }
//          val chromForMove = availableForGeneChroms.head
//          val freeCpu = chromForMove._2
//          chrom.genes = chrom.genes.filter(x => x!=chosenGene)
//          val geneDedicatedCpu = 0 // rnd.nextInt(freeCpu) + 1
//          var geneSharedCpu = 1
////          if (freeCpu - geneDedicatedCpu > 1) {
////            geneSharedCpu = rnd.nextInt(freeCpu - geneDedicatedCpu) + 1
////          }
//          val newGene = new SDGene(chosenGene.operatorId, geneDedicatedCpu, geneSharedCpu)
//          chromForMove._1.genes :+= newGene
//        }
//      }
//    }
    // again join duplicates
//    for (chrom <- sol.chromosomes) {
//      val node = ctx.env.resource(chrom.nodeId).get.asInstanceOf[Node]
//      val genes = chrom.genes
//      val genesGrouped = genes.groupBy(x => x.operatorId)
//      val joinedGenes = genesGrouped.map(x => new SDGene(x._1, x._2.map(y => y.dedicatedCpu).sum, x._2.map(y => y.sharedCpu).sum)).toList
//      chrom.genes = joinedGenes
//    }

    val repairedValidity = checkValidity[TSchedule](sol, ctx)
    val repairedIsValid = repairedValidity._1 && repairedValidity._2 && repairedValidity._3
    if (!repairedIsValid) {
      println(s"Validity = $repairedValidity")
      println(sol.toString())
      throw new IllegalStateException("Solution repairing completed unsuccessfully")
    }
    sol
  }

  def checkValidity[TSchedule <: Schedule](sol: SDSolution, ctx: Context[TSchedule]): (Boolean, Boolean, Boolean) = {
    var nodeValidity = true
    var allOperatorsValidity = true
    var partitionsValidity = true
    val operators = ctx.workload.getOperators()
    var usedOperators = List[OperatorId]()
    for (chrom <- sol.chromosomes) {
      val node = ctx.env.resource(chrom.nodeId).get.asInstanceOf[Node]
      if (chrom.length > node.containersLimit) {
        nodeValidity = false
      }
    }
    val allGenes = sol.allGenes
    val operatorsCountMap = operators.map(x => x.id).map(x => allGenes.count(g => g.operatorId == x))
    val operatorsCountMapLimit = operators.map(x => (x.partitionsLimit, allGenes.count(g => g.operatorId == x.id)))
    if (operatorsCountMap.min == 0) {
      allOperatorsValidity = false
    }
    if (operatorsCountMapLimit.map(x=> x._1 - x._2).min < 0) {
      partitionsValidity = false
    }
    (nodeValidity, allOperatorsValidity, partitionsValidity)
  }

  def availableCpuOnNode[TSchedule <: Schedule](chrom: SDChromosome, ctx: Context[TSchedule]): Int = {
    ctx.env.resource(chrom.nodeId).get.asInstanceOf[Node].cpuCount - chrom.genes.foldLeft(0)((s, x) => s + x.sharedCpu + x.dedicatedCpu)
  }

  def solutionToSchedule(resultSolution: SDSolution): NodeOperatorContainerBasedSchedule = {
    val schedule = new NodeOperatorContainerBasedSchedule()
    val nodesMapping = new util.HashMap[ResourceId, List[(AppId, Int, Int)]]()
    for (item <- resultSolution.chromosomes) {
      val nodeId = item.nodeId
      val genes = item.genes
      nodesMapping.put(nodeId, genes.map(x => (x.operatorId, x.dedicatedCpu, x.sharedCpu)))
    }
    schedule.nodesMapping = nodesMapping
    schedule
  }

  def solutionToScheduleImproved(resultSolution: SDSolution, ctx: Context[NodeOperatorContainerBasedScheduleImproved]): NodeOperatorContainerBasedScheduleImproved = {
    val schedule = new NodeOperatorContainerBasedScheduleImproved()
    val nodesMapping = new util.HashMap[ResourceId, List[(AppId, ContainerId, Int, Int)]]()

    val prevSched = ctx.schedule.nodesMapping

    for (item <- resultSolution.chromosomes) {
      val nodeId = item.nodeId
      val node = ctx.env.resource(nodeId).get.asInstanceOf[Node]
      val prevSchedOps = prevSched.get(nodeId)
      val genes = item.genes
      var contList = List[(AppId, ContainerId, Int, Int)]()
      for (gene <- genes) {
        val appId = gene.operatorId
        var contId = NodeOperatorContainerBasedScheduleImproved.CREATABLE_CONTAINER_PREFIX
        var dedicatedCpu = 0
        // TODO ! Set required sharedCpu!!!
        var sharedCpu = node.cpuCount
        if (prevSchedOps != null) {
          if (prevSchedOps.exists(x => appId.contains(x._1))) {
            val foundObj = prevSchedOps.filter(x => appId.contains(x._1)).head
            contId = foundObj._2
            dedicatedCpu = foundObj._3
            sharedCpu = foundObj._4
          }
        }

        val realAppId = ctx.workload.operatorMap.get(gene.operatorId).appId
        contList :+= (realAppId, contId, dedicatedCpu, sharedCpu)
      }
      nodesMapping.put(nodeId, contList)
    }
    schedule.nodesMapping = nodesMapping
    schedule
  }

}
