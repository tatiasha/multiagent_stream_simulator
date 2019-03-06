package model.algorithms.signal_distr_ga

import model.entities.Node
import model.general.{Context, NodeOperatorContainerBasedScheduleImproved}
import model.utilities.ExpProps

/**
  * Created by nikolay on 07.04.17.
  */
object SDSchedulerUtils {

  def buildExpProps(ctx: Context[_]): ExpProps = {
    val nodes = ctx.env.resources.filter(_.isInstanceOf[Node]).map(_.asInstanceOf[Node])

    if (nodes.isEmpty) {
      throw new IllegalArgumentException("Count of nodes is zero")
    }

    val averageCoresNumberPerNode = nodes.map(_.cpuCount).sum / nodes.size
    //  here we assuming performance model
    // TODO: should be parametrized. Currently used for experiments with BFS app
    val tuplesPerCore = 500 / 20

    val props = ExpProps(ctx.tenants.size, nodes.size, averageCoresNumberPerNode, tuplesPerCore)

    props
  }
}
