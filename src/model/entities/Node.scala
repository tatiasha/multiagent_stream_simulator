package model.entities

import model.ResourceId

/**
  * Created by mikhail on 29.03.2017.
  */
case class Node(override val id: ResourceId, cpuCount: Int, containersLimit: Int) extends Resource(id, null, null) {

  def nodeFromResource(res: Resource): Node = {
    Node(res.id, res.characteristics.cpu.toInt, containersLimit=4)
  }

  override def clone(): Node = {
    Node(id, cpuCount, containersLimit)
  }
}
