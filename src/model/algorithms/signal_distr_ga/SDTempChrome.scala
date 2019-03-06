package model.algorithms.signal_distr_ga

import java.util

import model.{ResourceId, OperatorId}
import scala.collection.JavaConversions._

/**
  * Created by mikhail on 04.04.2017.
  */
class SDTempChrome(sol: SDSolution, val allNodes: List[ResourceId]) {
  var genes = List[(OperatorId, ResourceId)]()
  for (chrom <- sol.chromosomes) {
    for (gene <- chrom.genes) {
      genes :+= (gene.operatorId, chrom.nodeId)
    }
  }

  def toSDSol(): SDSolution = {
    var chromMap = new util.HashMap[ResourceId, List[OperatorId]]()
    for (gene <- genes) {
      val node = gene._2
      val op = gene._1
      if (!chromMap.containsKey(node)) {
        chromMap.put(node, List[OperatorId]())
      }
      chromMap.put(node, chromMap.get(node) :+ op)
    }
    var chromosomes = List[SDChromosome]()
    for (key <- chromMap.keySet()) {
      chromosomes :+= new SDChromosome(key, chromMap.get(key).map(x => new SDGene(x, 0, 1)))
    }
    for (n <- allNodes) {
      if (!chromMap.containsKey(n)) {
        chromosomes :+= new SDChromosome(n, List[SDGene]())
      }
    }
    new SDSolution(chromosomes)
  }

}
