package model.utilities

import java.util.Random

import model.algorithms.signal_distr_ga.{SDGene, SDChromosome, SDSolution}

/**
  * Created by mikhail on 29.03.2017.
  */
object Misc {
  def getRandomElement[T](list: List[T], rng: Random): T = {
    val len = list.size
    val idx = rng.nextInt(len)
    list(idx)
  }

  def custom_sol: SDSolution = {
    var chromosomes = List[SDChromosome]()
    // #1
    chromosomes :+= new SDChromosome("1", List[SDGene](
      new SDGene("10", 0, 1),
      new SDGene("11", 0, 1),
      new SDGene("6", 0, 1),
      new SDGene("7", 0, 1)
    ))
    chromosomes :+= new SDChromosome("2", List[SDGene](
      new SDGene("5", 0, 1),
      new SDGene("12", 0, 1)
    ))
    chromosomes :+= new SDChromosome("3", List[SDGene](
      new SDGene("9", 0, 1),
      new SDGene("8", 0, 1)
    ))
    chromosomes :+= new SDChromosome("4", List[SDGene](
      new SDGene("4", 0, 1),
      new SDGene("1", 0, 1)
    ))
    chromosomes :+= new SDChromosome("5", List[SDGene](
      new SDGene("3", 0, 1),
      new SDGene("2", 0, 1)
    ))
    chromosomes :+= new SDChromosome("6", List[SDGene]())
    chromosomes :+= new SDChromosome("7", List[SDGene]())
    chromosomes :+= new SDChromosome("8", List[SDGene]())


    new SDSolution(chromosomes)
  }
}
