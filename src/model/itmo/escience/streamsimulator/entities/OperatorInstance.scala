package itmo.escience.streamsimulator.entities

import itmo.escience.streamsimulator.{OperatorId, OperatorInstanceId}
import itmo.escience.streamsimulator.statistics.OperatorInstanceStats

/**
  * Created by mikhail on 23.01.2017.
  */
case class OperatorInstance(id: OperatorInstanceId,
                            operatorId: OperatorId,
                            var parents: List[OperatorInstanceId],
                            var children: List[OperatorInstanceId])
  extends Cloneable with Serializable {

  override def clone(): OperatorInstance = {
    OperatorInstance(this.id, this.operatorId, this.parents, this.children)
  }

  override def toString = s"OperatorInstance(id=$id, opid=$operatorId, parents=$parents, children=$children)"
}
