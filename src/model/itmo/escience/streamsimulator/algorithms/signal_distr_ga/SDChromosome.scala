package itmo.escience.streamsimulator.algorithms.signal_distr_ga

import itmo.escience.streamsimulator.ResourceId

/**
  * Created by mikhail on 28.03.2017.
  */
class SDChromosome(val nodeId: ResourceId, var genes: List[SDGene]) {
  def copy = {
    new SDChromosome(nodeId, genes)
  }

  def length = genes.length
  def addGene(gene: SDGene) = {
    genes :+= gene
  }
}
