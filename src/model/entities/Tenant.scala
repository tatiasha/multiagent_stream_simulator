package model.entities

import model.{AppId, TenantId}

/**
  * Created by mikhail on 23.01.2017.
  */
case class Tenant(id: TenantId, var priority: Int = 1) extends Cloneable with Serializable {
  override def clone(): Tenant = {
    Tenant(this.id, this.priority)
  }
}
