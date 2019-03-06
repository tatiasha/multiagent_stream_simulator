package model.models

import model.entities.Characteristics

/**
  * Created by mikhail on 28.02.2017.
  */
object PerformanceModel {

  def BFSThroughput(input: Double, queue: Double, sensors: Double, char: Characteristics): Double = {
    input -  input * (sensors / 300) / Math.log(char.cpu / 20 + 1)
  }

  def throughput(input: Double, queue: Double, char: Characteristics): Double = {
    input
  }

}
