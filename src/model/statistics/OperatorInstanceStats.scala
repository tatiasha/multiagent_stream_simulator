package model.statistics

import model.OperatorInstanceId
import scala.concurrent.duration._

/**
  * Represents metrics of stream processing for particular operator instances
 *
  * @param id identifier for an instance of some operator
  * @param inputs amount of tuples that has been consumed by the instance
  * @param throughputs amount of tuples that has been produced by the instance
  * @param latencies average delay in processing for the tuples
  * @param delta time interval for measurement of characteristics
  */
case class OperatorInstanceStats(id: OperatorInstanceId,
                                 inputs:List[CountPerWindow],
                                 throughputs: List[CountPerWindow],
                                 latencies: List[MeanPerWindow],
                                 delta: Duration) extends Serializable with Cloneable{
  override def toString: String =
    s"OperatorInstanceStats{id=$id,5inputs=${inputs.take(5)}, " +
    s"5throughputs=${throughputs.take(5)}, 5latencies=${latencies.take(5)}, delta=$delta}"

  def union(other: OperatorInstanceStats): OperatorInstanceStats = {
    new OperatorInstanceStats(id, inputs ++ other.inputs, throughputs ++ other.throughputs, latencies ++ other.latencies, delta)
  }

  override def clone(): OperatorInstanceStats = {
    new OperatorInstanceStats(id, inputs, throughputs, latencies, delta)
  }
}
