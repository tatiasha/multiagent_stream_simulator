package itmo.escience.streamsimulator.general

import java.util

import itmo.escience.streamsimulator.{AppId, ContainerId, OperatorId, ResourceId}
import scala.collection.JavaConversions._

/**
  * Created by mikhail on 30.03.2017.
  */
class NodeOperatorContainerBasedSchedule extends Schedule {
  // (operatorId, dedicatedCpu, sharedCpu)
  var nodesMapping = new util.HashMap[ResourceId, List[(OperatorId, Int, Int)]]()
}

class NodeOperatorContainerBasedScheduleImproved extends Schedule {
  // (operatorId, dedicatedCpu, sharedCpu)
  var nodesMapping = new util.HashMap[ResourceId, List[(AppId, ContainerId, Int, Int)]]()

  override def clone(): NodeOperatorContainerBasedScheduleImproved = {
    val s = new NodeOperatorContainerBasedScheduleImproved
    for ((resId, list) <- nodesMapping) {
      s.nodesMapping.put(resId, list)
    }
    s
  }
}

object NodeOperatorContainerBasedScheduleImproved {
  // this prefix means that container id should be replaced with generated id
  val CREATABLE_CONTAINER_PREFIX = "---"
}


