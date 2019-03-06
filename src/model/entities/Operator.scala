package model.entities

import model.{AppId, OperatorId}

/**
  * Created by mikhail on 23.01.2017.
  */
case class Operator(id: OperatorId,
                    appId: AppId,
                    var children: List[OperatorId],
                    var parents: List[OperatorId],
                    var tasks: List[OperatorInstance],
                    partitionsLimit: Int = 3,
                    content: List[String]=List(),
                    opType: String="unknown")
  extends Cloneable with Serializable {

  override def clone(): Operator = {
    new Operator(this.id, this.appId, this.children, this.parents, this.tasks.map(x => x.clone()), content=this.content.map(x => x), opType=this.opType)
  }

  def generateInstances(n: Int): List[OperatorInstance] = {
    var instances = List[OperatorInstance]()
    for (i <- 0 until n) {
      instances +:= new OperatorInstance(s"${id}_i$i", id, List[String](), List[String]())
    }
    instances
  }

}

class Source(appId: AppId) extends Operator("source", appId, List(), List(), List()) {
  override def clone(): Operator = new Source(appId)
}

object Source {
  val SOURCE_ID = "source"
}

class Sink(appId: AppId) extends Operator("source", appId, List(), List(), List()) {
  override def clone(): Operator = new Sink(appId)
}

object Sink {
  val SINK_ID = "sink"
}
