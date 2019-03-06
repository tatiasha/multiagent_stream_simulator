package model.utilities

import java.util.Properties

import scala.io.Source

/**
  * Created by mikhail on 04.04.2017.
  */
object ExpProps {

  var tenantsNumber: Int = 0
  var nodesNumber: Int = 0
  var cores: Int = 0
  var tuplesPerCore: Int = 25

  var iterations: Int = 500
  var popSize: Int = 50

  var filepath: String = null

  def apply(): ExpProps = new ExpProps(tenantsNumber, nodesNumber, cores, tuplesPerCore, iterations, popSize)

  def setPropsFromFile(path: String) = {
    val properties: Properties = new Properties()
    val url = s"$path/exp.props"
    val source = Source.fromFile(url)
    properties.load(source.bufferedReader())

    tenantsNumber = Integer.parseInt(properties.getProperty("tenants"))
    nodesNumber = Integer.parseInt(properties.getProperty("nodes"))
    cores = Integer.parseInt(properties.getProperty("cores"))
    tuplesPerCore = Integer.parseInt(properties.getProperty("tuples_per_core"))
    filepath = path
  }
}


case class ExpProps(tenantsNumber: Int,
                    nodesNumber: Int,
                    cores: Int,
                    tuplesPerCore: Int,
                    iterations: Int = 500,
                    popSize: Int = 50,
                    filepath: String = null)
