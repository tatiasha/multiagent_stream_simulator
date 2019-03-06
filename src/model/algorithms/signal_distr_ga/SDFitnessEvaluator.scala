package model.algorithms.signal_distr_ga

import java.util

import model._
import model.entities.Node
import model.experiments.baseintegration.sd.SinusSignalDistributionExp
import model.general.{NodeOperatorContainerBasedScheduleImproved, Schedule, Context}
import model.scheduler.{OperatorStats, AppStats}
import model.utilities.{ExpProps, Misc}
import org.uncommons.watchmaker.framework.FitnessEvaluator

/**
  * Created by mikhail on 28.03.2017.
  */
class SDFitnessEvaluator[TSchedule <: Schedule](ctx: Context[TSchedule], appStats: AppStats) extends FitnessEvaluator[SDSolution]{
  val tuplesPerCore = ExpProps.tuplesPerCore

  var actStats: AppStats = appStats
  if (appStats == null) {
    actStats = defaultAppStat()
  }
  var isLastBest = false

  override def isNatural: Boolean = false

  override def getFitness(t: SDSolution, list: util.List[_ <: SDSolution]): Double = {

    val allGenes = t.allGenes
//    val partitionsCount = allGenes.groupBy(x => x.operatorId).map(x => (x._1, x._2.size))
    val opStats = actStats.opStats
    val timeBound = actStats.opStats.map(x=>x._2.size).min

    var solFreeCpu = 0
    var solOverCpu = 0
    var solMaxMinDiff = 0

    for (chrom <- t.chromosomes) {
      if (chrom.length > 0) {
        var chromFreeCpu = 0
        var chromOverCpu = 0
        var chromMin = Int.MaxValue
        var chromMax = 0

        val node = ctx.env.resource(chrom.nodeId).get.asInstanceOf[Node]
        val genes = chrom.genes
//        val totalSharedCpu = genes.map(x => x.sharedCpu).sum
        val totalSharedCpu = node.cpuCount

        for (i <- 0 until timeBound) {
          var timeFreeCpu = 0
          var timeOverCpu = 0
          var timeSharedUsage = 0

          for (gene <- genes) {
            val opId = gene.operatorId
            var originalValues: List[OperatorStats] = null
            if (opStats.contains(opId)) {
              originalValues = opStats(opId)
            } else {
              originalValues = opStats(opStats.keySet.head)
            }
//            val geneThr = (originalValues(i).inputThroughput / partitionsCount.get(opId).get).toDouble
            val geneThr = originalValues(i).inputThroughput.toDouble
//            val usedCpu = math.ceil(geneThr / tuplesPerCore).toInt
            val usedCpu = math.round(geneThr / tuplesPerCore).toInt
//            val dedicatedFree = math.max(gene.dedicatedCpu - usedCpu, 0)
//            var dedicatedOver = 0
//            if (dedicatedFree == 0) {
//              dedicatedOver = usedCpu - gene.dedicatedCpu
//            }
//            timeFreeCpu += dedicatedFree
//            timeSharedUsage += dedicatedOver
            timeSharedUsage += usedCpu
          }

          chromMax = math.max(chromMax, timeSharedUsage)
          chromMin = math.min(chromMin, timeSharedUsage)

          val sharedDiff = totalSharedCpu - timeSharedUsage
          if (sharedDiff >= 0) {
            timeFreeCpu += sharedDiff
          } else {
            timeOverCpu = -sharedDiff
          }

          chromFreeCpu += timeFreeCpu
          chromOverCpu += timeOverCpu
        }

        solFreeCpu += chromFreeCpu
        solOverCpu += chromOverCpu

        val maxMinDiff = chromMax - chromMin
        solMaxMinDiff += maxMinDiff
      }
    }

    val schedContainersDiff = prevSchedDifference(t)

    var result = 0.0
    result += solFreeCpu.toDouble
    result += penaltyOverloadFunction(solOverCpu).toDouble
//    result += maxMinDiffPenalty(solMaxMinDiff)
//    result += prevSchedDiffPenalty(schedContainersDiff)
    if (isLastBest) {
      println(s"Fitness:\n\tsolFreeCpu=$solFreeCpu;\n\toverloadPenalty=${penaltyOverloadFunction(solOverCpu)};\n\tmaxMinDiffPenalty=${maxMinDiffPenalty(solMaxMinDiff)}\n\tprevschedDiff=${prevSchedDiffPenalty(schedContainersDiff)}")
    }
    result
  }


  // Penalty functions!!!!!
  def penaltyOverloadFunction(overCpu: Int): Int = {
    overCpu * 100
  }

  def maxMinDiffPenalty(diff: Int) = {
    diff * 1.1
  }

  def prevSchedDiffPenalty(diffContainers: Int): Double = {
    diffContainers * 1
  }


  def defaultAppStat(): AppStats = {
    val times = 10
    val defaultWorkload = 500
    val operatorIds = ctx.workload.getOperators().map(x => x.id)
    var statMap = Map[OperatorId, List[OperatorStats]]()
    var defaultOpStat = List[OperatorStats]()
    for (i <- 0 until times) {
      defaultOpStat :+= new OperatorStats(0, defaultWorkload, 0, 0.0, 0.0)
    }
    for (op <- operatorIds) {
      statMap += (op -> defaultOpStat)
    }
    new AppStats(statMap, 0, times, 1)
  }


  def prevSchedDifference(sol: SDSolution): Int = {
    var counter = 0
    if (ctx.schedule == null) {
      return counter
    }
    val sched = ctx.schedule.asInstanceOf[NodeOperatorContainerBasedScheduleImproved]
    val resources = ctx.env.resources.map(x=>x.id)
    for (res <- resources) {
      val schedContainers = sched.nodesMapping.get(res)
      if (schedContainers != null) {
        val solGenes = sol.chromosomes.filter(x => x.nodeId == res).head.genes
        for (cont <- schedContainers) {
          if (!solGenes.exists(x => x.operatorId.contains(cont._1))) {
            counter += 1
          }
        }
      }
    }
    counter
  }
}
