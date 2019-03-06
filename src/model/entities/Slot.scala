package model.entities

import model.{SlotId, ResourceId}

/**
  * Created by mikhail on 23.01.2017.
  */
case class Slot(id: SlotId, resId: ResourceId, var characteristics: Characteristics)
  extends Cloneable with Serializable {

  override def clone(): Slot = {
    Slot(this.id, this.resId, this.characteristics.clone())
  }
}
