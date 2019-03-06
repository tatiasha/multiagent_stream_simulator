package model.algorithms.signal_distr_ga

import java.util
import java.util.Random

import model.{OperatorId, ResourceId}
import model.general.{Schedule, Context}
import org.uncommons.watchmaker.framework.operators.AbstractCrossover

/**
  * Created by mikhail on 29.03.2017.
  */
class SDCrossover[TSchedule <: Schedule](ctx: Context[TSchedule], crossPoints: Int = 1) extends AbstractCrossover[SDSolution](crossPoints) {
  //  override def mate(t: SDSolution, t1: SDSolution, i: Int, random: Random): util.List[SDSolution] = {
  //    val chromsSize = t1.chromosomes.length
  //    val point = random.nextInt(chromsSize)
  //    var c1Chroms = List[SDChromosome]()
  //    var c2Chroms = List[SDChromosome]()
  //    for (i <- 0 until chromsSize) {
  //      if (i < point) {
  //        c1Chroms :+= t.chromosomes(i).copy
  //        c2Chroms :+= t1.chromosomes(i).copy
  //      }
  //      else {
  //        c1Chroms :+= t1.chromosomes(i).copy
  //        c2Chroms :+= t.chromosomes(i).copy
  //      }
  //    }
  //    val offspring = new util.ArrayList[SDSolution](2)
  //    val child1 = new SDSolution(c1Chroms)
  //    val child2 = new SDSolution(c2Chroms)
  //    val repairedChild1 = SDProblem.repairSolution(child1, ctx)
  //    val repairedChild2 = SDProblem.repairSolution(child2, ctx)
  //    offspring.add(repairedChild1)
  //    offspring.add(repairedChild2)
  //    offspring
  //  }

  override def mate(t: SDSolution, t1: SDSolution, i: Int, random: Random): util.List[SDSolution] = {
    val chromsSize = t1.chromosomes.length
    val point = random.nextInt(chromsSize)

    val allNodesIds = ctx.env.resources.map(x => x.id)

    val temp1 = new SDTempChrome(t, allNodesIds)
    val temp2 = new SDTempChrome(t1, allNodesIds)
    val c1 = new SDTempChrome(t, allNodesIds)
    val c2 = new SDTempChrome(t1, allNodesIds)
    val operators = c1.genes.map(x => x._1)
    var g1 = List[(OperatorId, ResourceId)]()
    var g2 = List[(OperatorId, ResourceId)]()
    for (op <- operators) {
      val node1 = c1.genes.filter(x => x._1 == op).head._2
      val node2 = c2.genes.filter(x => x._1 == op).head._2
      if (random.nextBoolean()) {
        g1 :+=(op, node1)
        g2 :+=(op, node2)
      } else {
        g1 :+=(op, node2)
        g2 :+=(op, node1)
      }
    }
//    c1.genes = c1.genes.take(point) ++ c2.genes.drop(point)
//    c2.genes = c2.genes.take(point) ++ c1.genes.drop(point)
    c1.genes = g1
    c2.genes = g2
    val offspring = new util.ArrayList[SDSolution](2)
    offspring.add(c1.toSDSol())
    offspring.add(c2.toSDSol())
    offspring
  }
}
