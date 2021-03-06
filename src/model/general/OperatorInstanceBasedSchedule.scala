package model.general

import java.util

import model.{OperatorInstanceId, OperatorId, SlotId}
import model.entities._

import scala.collection.JavaConversions._

/**
  * Created by mikhail on 23.01.2017.
  */
class OperatorInstanceBasedSchedule extends Schedule with Cloneable with Serializable {

  var operatorInstanceIdToSlotId: util.HashMap[OperatorInstanceId, SlotId] = new util.HashMap[OperatorId, SlotId]()

  override def clone(): OperatorInstanceBasedSchedule = {
    val nMap = new util.HashMap[OperatorId, SlotId]()
    for (key <- operatorInstanceIdToSlotId.keySet()) {
      nMap.put(key, operatorInstanceIdToSlotId.get(key))
    }
    val nSched = new OperatorInstanceBasedSchedule()
    nSched.operatorInstanceIdToSlotId = nMap
    nSched
  }
}
