package itmo.escience.streamsimulator.algorithms.signal_distr_ga

/**
  * Created by mikhail on 28.03.2017.
  */
class SDSolution(val chromosomes: List[SDChromosome]) {

  def allGenes: List[SDGene] = {
    chromosomes.foldLeft(List[SDGene]())((s, x) => s ++ x.genes)
  }

  override def toString(): String = {
    var result = "++++\n"
    for (chrom <- chromosomes) {
      result += s"${chrom.nodeId} = {\n"
      for (gene <- chrom.genes) {
        result += s"\t${gene.operatorId}\t${gene.dedicatedCpu}\t${gene.sharedCpu}\n"
      }
      result+= "}\n"
    }
    result += "----"
    result
  }
}

