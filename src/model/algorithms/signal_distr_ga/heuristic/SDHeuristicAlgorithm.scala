package model.algorithms.signal_distr_ga.heuristic

import java.util.Random

import model.entities.Node
import model.utilities.{ExpProps, Misc}
import model.{OperatorId, ResourceId}
import model.algorithms.signal_distr_ga.{SDGene, SDChromosome, SDSolution}
import model.general.{Context, NodeOperatorContainerBasedScheduleImproved}
import model.scheduler.AppStats
import java.util
import scala.collection.JavaConversions._

/**
  * Created by mikhail on 07.04.2017.
  */
object SDHeuristicAlgorithm {
  def schedule(ctx: Context[NodeOperatorContainerBasedScheduleImproved], stats: AppStats): SDSolution = {
    val rnd = new Random()
    val nodesIds = ctx.env.resources.map(x => x.id)
    var operators = ctx.workload.getOperators().map(x => x.id)
    val schedMap = new util.HashMap[ResourceId, List[OperatorId]]()
    for (n <- nodesIds) {
      schedMap.put(n, List[OperatorId]())
    }
    while (operators.nonEmpty) {
      val chosenOps = List[Int](1, 2, 3, 4).take(operators.size).map(x => Misc.getRandomElement(operators, rnd))
      var estList = List[(OperatorId, ResourceId, Double)]()
      for (op <- chosenOps) {
        val availNodes = availableNodes(stats, schedMap)
        val nodesEstimations = availNodes.map(x => (x, estimatePlacement(x, op, schedMap, stats)))
        val minNode = nodesEstimations.minBy(x => x._2)
        estList :+=(op, minNode._1, minNode._2)
      }
      val minEstimation = estList.minBy(x => x._3)
      schedMap.put(minEstimation._2, schedMap.get(minEstimation._2) :+ minEstimation._1)
      operators = operators.filter(x => x != minEstimation._1)
    }
    var chromosomes = List[SDChromosome]()
    for (n <- nodesIds) {
      chromosomes :+= new SDChromosome(n, schedMap.get(n).map(x => new SDGene(x, 0, 1)))
    }
    val sol = new SDSolution(chromosomes)
    sol
  }

  def estimatePlacement(nodeId: ResourceId, op: OperatorId, map: util.HashMap[ResourceId, List[OperatorId]], stats: AppStats): Double = {
    val opStats = stats.opStats
    val timeBound = stats.opStats.head._2.size

    var freeCpu = 0.0
    var overCpu = 0.0
    val nodeTuplesBound = 500.0 // todo parameterize

    val placedOps = map.get(nodeId)

    val mergedSignal = Array.fill[Double](timeBound) {
      0.0
    }
    for (pOp <- placedOps :+ op) {
      val opStat = opStats(pOp)
      for (i <- 0 until timeBound) {
        val st = opStat(i)
        val thr = st.inputThroughput.toDouble
        mergedSignal(i) += thr
      }
    }
    for (i <- 0 until timeBound) {
      val thr = mergedSignal(i)
      val sharedDiff = nodeTuplesBound - thr
      if (sharedDiff >= 0) {
        freeCpu += sharedDiff
      } else {
        overCpu = -sharedDiff
      }
    }
    val maxMinDiff = mergedSignal.max - mergedSignal.min

    val result = freeCpu + overPenalty(overCpu) + diffPenalty(maxMinDiff)
    if (placedOps.isEmpty) {
      return result * 0.7
    }
    result
  }

  def availableNodes(stats: AppStats, sched: util.HashMap[ResourceId, List[OperatorId]]): List[ResourceId] = {
    val timeBound = stats.opStats.head._2.size
    val nodeTuplesBound = 500.0
    var resList = List[ResourceId]()
    for (key <- sched.keySet) {
      val nodeOps = sched.get(key)
      val mergedSignal = Array.fill[Double](timeBound) {0.0}
      for (pOp <- nodeOps) {
        val opStat = stats.opStats(pOp)
        for (i <- 0 until timeBound) {
          val st = opStat(i)
          val thr = st.inputThroughput.toDouble
          mergedSignal(i) += thr
        }
      }
      val avg = mergedSignal.sum / mergedSignal.length
      if (avg < nodeTuplesBound * 0.8) {
        resList :+= key
      }
    }
    resList
  }

  def overPenalty(over: Double) = {
    over * 10000
  }

  def diffPenalty(maxMinDiff: Double) = {
    maxMinDiff * 0.01
  }
}
