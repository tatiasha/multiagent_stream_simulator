package model.general

import java.util

import model.{ContainerId, ResourceId, SlotId}
import model.entities._

import scala.collection.JavaConversions._
import scala.collection.mutable


/**
  * Created by mikhail on 23.01.2017.
  */
class Environment(var resources: List[Resource], var networks: List[Network])
  extends Cloneable with Serializable {


  private[model] var resMap: util.HashMap[ResourceId, Resource] = new util.HashMap[ResourceId, Resource]()
  private[model] var slotMap: util.HashMap[SlotId, Slot] = new util.HashMap[SlotId, Slot]()
  private[model] var containerIds: List[ResourceId] = List[ResourceId]()

  fillResSlotMaps(resources)

  def getSlotById(slotId: String): Slot = {
    slotMap.get(slotId)
  }

  def getContainers() = {
    containerIds.map(x => resMap.get(x))
  }

  def getSlotIds() = {
    slotMap.keySet().toList
  }

  def getSlots() = {
    slotMap.values().toList
  }

  def getContIds() = {
    containerIds
  }

  def resource(id: ResourceId): Option[Resource] = Option(resMap.getOrDefault(id, null))

  def container(id: ContainerId): Option[Container] = Option(resMap.getOrDefault(id, null).asInstanceOf[Container])

  def slot(id:SlotId): Option[Slot] = Option(slotMap.getOrDefault(id, null))

  def allResources = resources

  def allNetworks = networks

  def rootResourceBySlotId(id: SlotId): Option[Resource] = {
    slot(id).map { slt =>
      rootResource(slt.resId)
    }
  }

  def addResource(res: Resource): Unit = {
    if (resources.contains(res.id)) {
      throw new RuntimeException(s"There is already element with such id ${res.id}")
    }

    resources = res :: resources

    resMap.put(res.id, res)

    //fillResSlotMaps(List(res))
  }

  def removeResource(id: ResourceId): Unit = {
    if (!resMap.containsKey(id)) {
      throw new NoSuchElementException(s"There is no resource with id $id")
    }

    val parentId = resMap.get(id).parentId
    if (resMap.containsKey(parentId)) {
      val res = resMap.get(parentId)
      res.resources = res.resources.filter(x => x.id != id)
    }

    resources = resources.filter(_.id != id)

    resMap.remove(id)

    for((id, resource) <- resMap.filter(_._2.parentId == id).toList){
      removeResource(id)
    }

    for((id, slot) <- slotMap.filter(_._2.resId == id).toList){
      slotMap.remove(id)
    }

    containerIds = containerIds.filter(x => x != id)
    //TODO: check containers map somehow
  }

  def addContainer(cont: Container): Unit = {
    if (cont.parentId == null || !resMap.containsKey(cont.parentId)) {
      throw new RuntimeException(s"Parent ${cont.parentId} doesn't exist in this environment")
    }

    if (resMap.containsKey(cont.id)) {
      throw new RuntimeException(s"Container ${cont.id} already exists")
    }

    resMap.put(cont.id, cont)
    containerIds :+= cont.id

    if (cont.parentId != null) {
      val parent = resMap.get(cont.parentId)
      parent.resources :+= cont
    }
  }

  def removeContainer(contId: ResourceId): Unit = {
    if (!resMap.containsKey(contId)) {
      throw new RuntimeException(s"Container $contId doesn't exist")
    }

    val cont = resMap.get(contId)

    containerIds = containerIds.filter(_ != cont.id)

    if (cont.parentId != null) {
      val parent = resMap.get(cont.parentId)
      parent.resources = parent.resources.filter(_.id != cont.id)
    }

    resMap.remove(contId)
  }

  def allocateContainer(resId: ResourceId, characteristics: Characteristics): ResourceId = {
    val res = resMap.get(resId)
    val contId = resId + "_cont" + (res.resources.size + 1)
    val cont = new Container(contId, resId, characteristics)
    resMap.put(contId, cont)
//    resources :+= cont
    res.resources :+= cont
    containerIds :+= contId
    contId
  }

  def deallocateContainer(contId: ResourceId): Unit = {
    if (!resMap.contains(contId)) {
      throw new RuntimeException(s"Container $contId doesn't exist")
    }
    resMap.remove(contId)
    containerIds = containerIds.filter(_ != contId)
  }

  def removeSlots(contId: ResourceId) = {
    val cont = resMap.get(contId).asInstanceOf[Container]
    val removedSlots = cont.slots
    for (slot <- removedSlots) {
      slotMap.remove(slot.id)
    }
    cont.slots = List[Slot]()
  }

  def allocateSlot(contId: ResourceId, characteristics: Characteristics): ResourceId = {
    val cont = resMap.get(contId).asInstanceOf[Container]
    // check
    val totalCpu = cont.characteristics.cpu
    val usedCpu = cont.slots.map(x => x.characteristics.cpu).sum
    val availableCpu = totalCpu - usedCpu
    if (availableCpu < characteristics.cpu - 1) {
      throw new IllegalStateException(s"Not enough available cpu to allocate a slot on container $contId")
    }
    // assign
    val slotId = cont.id + "_slot" + (cont.slots.size + 1)
    val slot = new Slot(slotId, contId, characteristics)
    slotMap.put(slotId, slot)
    cont.slots :+= slot
    slotId
  }

  // recursive search of all resources and slots
  def fillResSlotMaps(resList: List[Resource]): Unit = {
    for (r <- resList) {
      resMap.put(r.id, r)
      r match {
        case r: Container =>
          containerIds :+= r.id
          for (s <- r.slots) {
            slotMap.put(s.id, s)
          }
        case _ =>
      }
      fillResSlotMaps(r.resources)
    }
  }

  override def clone(): Environment = {
    new Environment(this.resources.map(x => x.clone()), this.networks.map(x => x.clone()))
  }

  private  def rootResource(resid: ResourceId): Resource = {
    val res = this.resMap.get(resid)
    if (res.parentId == null) {
      return res
    }
    rootResource(res.parentId)
  }
}
