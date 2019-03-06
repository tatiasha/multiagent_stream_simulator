package model.entities

import model.ResourceId

/**
  * Created by mikhail on 29.03.2017.
  */
class SharedContainer(override val id: ResourceId, val nodeId: ResourceId, val cpu: Int, val sharedCpu: Int) extends Resource(id, nodeId, null) {

}
