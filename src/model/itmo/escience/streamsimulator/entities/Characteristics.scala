package itmo.escience.streamsimulator.entities

/**
  * Created by mikhail on 23.01.2017.
  */
case class Characteristics (
  var cpu: Double = 0.0,
  var sharedCpu: Double = 0.0,
  var cpuFreq: Double = 2000.0,
  var resType: String = "",
  var ram: Double = 0.0,
  var gpu: Double = 0.0,
  var status: String = ""
) extends Cloneable with Serializable {

  override def clone(): Characteristics = {
    val newChar = Characteristics()
    newChar.cpu = this.cpu
    newChar.cpuFreq = this.cpuFreq
    newChar.resType = this.resType
    newChar.ram = this.ram
    newChar.gpu = this.gpu
    newChar
  }
}