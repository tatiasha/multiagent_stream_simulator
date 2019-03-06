package itmo.escience.streamsimulator.general

import java.util

import itmo.escience.streamsimulator.{AppId, OperatorId, OperatorInstanceId}
import itmo.escience.streamsimulator.entities._

import scala.collection.JavaConversions._

/**
  * Created by mikhail on 24.01.2017.
  */
class Workload(var apps: List[App])
  extends Cloneable with Serializable {

  var appMap: util.HashMap[AppId, App] = new util.HashMap[String, App]()
  var operatorMap: util.HashMap[OperatorId, Operator] = new util.HashMap[String, Operator]()
  var taskMap: util.HashMap[OperatorInstanceId, OperatorInstance] = new util.HashMap[String, OperatorInstance]()

  fillAppOpTMaps()

  def getTaskById(taskId: String): OperatorInstance = {
    taskMap.get(taskId)
  }

  def getTaskIds(): List[OperatorInstanceId] = {
    taskMap.keySet().toList
  }

  def getTasks(): List[OperatorInstance] = {
    taskMap.values().toList
  }

  def getOperators(): List[Operator] = {
    operatorMap.values().toList
  }

  def add(app: App): Unit = {
    if (appMap.containsKey(app.id)){
      throw new RuntimeException(s"App with the same id ${app.id} already exists")
    }
    for(op <- app.operators) {
      if (operatorMap.containsKey(op.id)) {
        throw new RuntimeException(s"Operator of new app ${app.id} with the same id ${op.id} already exists")
      }
    }

    apps :+= app
    appMap.put(app.id, app)

    for(op <- app.operators) {
      operatorMap.put(op.id, op)
    }
  }

  def remove(id: AppId): Unit = {
    if (!appMap.containsKey(id)) {
      throw new RuntimeException(s"App with id $id doesn't exist")
    }

    val app = appMap.get(id)

    apps = apps.filter(_.id != id)
    appMap.remove(id)
    for(op <- app.operators) {
      operatorMap.remove(op.id)
    }

  }

  def allocateTask(opId: OperatorId): OperatorInstanceId = {
    val op = operatorMap.get(opId)
    val tId = opId + "_task" + (op.tasks.size + 1)
    val task = new OperatorInstance(tId, opId, List[String](), List[String]())
    taskMap.put(tId, task)
    op.tasks :+= task
    tId
  }

  def allocateTask(opInst: OperatorInstance): OperatorInstanceId = {
    val instId = opInst.id
    val opId = opInst.operatorId
    taskMap.put(instId, opInst)
    val op = operatorMap.get(opId)
    op.tasks :+= opInst
    instId
  }

  def removeTask(opInstId: OperatorInstanceId): OperatorInstanceId = {
    val opInst = taskMap.get(opInstId)
    val op = operatorMap.get(opInst.operatorId)
    op.tasks = op.tasks.filter(x => x.id != opInstId)
    taskMap.remove(opInstId)
    opInstId
  }

  def removeTasks(opId: OperatorId): List[OperatorInstanceId] = {
    val op = operatorMap.get(opId)
    var removed = op.tasks.map(x => x.id)
    for (t <- removed) {
      taskMap.remove(t)
    }
    op.tasks = List[OperatorInstance]()
    removed
  }

  def fillAppOpTMaps(): Unit = {
    for (a <- apps) {
      appMap.put(a.id, a)
      val ops = a.operators
      for (op <- ops) {
        operatorMap.put(op.id, op)
        val tasks = op.tasks
        for (t <- tasks) {
          taskMap.put(t.id, t)
        }
      }
    }
  }

  override def clone(): AnyRef = {
    new Workload(this.apps.map(x => x.clone()))
  }
}
