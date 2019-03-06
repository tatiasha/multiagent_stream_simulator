package model.general

import java.util

/**
  * Created by mikhail on 23.01.2017.
  */
case class Framework(name: String, version: String) extends Cloneable with Serializable {
  override def clone(): Framework = {
    val framework = Framework(this.name, this.version)
    framework
  }
}
