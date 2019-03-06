package model.entities

import model.{ResourceId, NetworkId}

/**
  * Created by mikhail on 24.01.2017.
  */
case class Network(var id: NetworkId, var bandwidth: Double, var resources: List[ResourceId]) extends Cloneable with Serializable {

  override def clone(): Network = {
    Network(this.id, this.bandwidth, this.resources)
  }
}
