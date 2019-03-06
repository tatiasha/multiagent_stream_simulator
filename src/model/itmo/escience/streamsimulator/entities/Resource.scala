package itmo.escience.streamsimulator.entities

import itmo.escience.streamsimulator.ResourceId

/**
  * Created by mikhail on 23.01.2017.
  */
class Resource(val id: ResourceId,
               val parentId: ResourceId,
               var characteristics: Characteristics,
               var resources: List[Resource] = List[Resource]())
  extends Cloneable with Serializable {

  override def clone(): Resource = {
    var newResources = List[Resource]()
    for (r <- this.resources) {
      newResources :+= r.clone()
    }
    val char = if (this.characteristics != null) this.characteristics.clone() else null
    val res = new Resource(this.id, this.parentId, char)
    res.resources = newResources
    res
  }
}

class Container(override val id: ResourceId,
                override val parentId: ResourceId,
                characteristics: Characteristics,
                var status: String = Container.UNKNOWN,
                var slots: List[Slot] = List[Slot]())
  extends Resource(id, parentId, characteristics) with Cloneable with Serializable {

  override def clone(): Container = {
    val cont = new Container(this.id, this.parentId, this.characteristics.clone())
    var newSlots = List[Slot]()
    for (s <- this.slots) {
      newSlots :+= s.clone()
    }
    cont.slots = newSlots
    cont
  }
}

object Container {
  val UNKNOWN = "unknown"
  val RUNNING = "running"
  val STARTING = "starting"
  val STOPPING = "stopping"
}
