package itmo.escience.streamsimulator.utilities

import java.io.PrintWriter

import itmo.escience.streamsimulator.entities._
import itmo.escience.streamsimulator.general._
import org.json4s.jackson.Serialization._
import scala.collection.JavaConversions._
import java.util


/**
  * Created by mikhail on 26.01.2017.
  */
@Deprecated
object JSONParser {
  implicit val formats = org.json4s.DefaultFormats

  // J types
  case class JTask(name: String)
  case class JSlot(name: String, characteristics: Map[String, Any])
  case class JContainer(name: String, characteristics: Map[String, Any], slots: List[JSlot])
  case class JNode(name: String, characteristics: Map[String, Any], nodes: List[JContainer])
  case class JOperator(name: String, tasks: List[JTask])
  case class JApp(name: String, operators: List[JOperator])
  case class JContext(environment: List[JNode], workload: List[JApp], schedule: Map[String, String]=Map[String, String]())

  def readInputJson(path: String) = {
    var inputJson = ""
    for (line <- scala.io.Source.fromFile(path).getLines) inputJson += line
    val jsonObg = read[JContext](inputJson)
    val tenants = List[Tenant](new Tenant("user", 13))
    val jEnv = jsonObg.environment
    val jWld = jsonObg.workload
    val jSched = jsonObg.schedule

    val resources = jEnv.map(x => jNodeToNode(x))
    val apps = jWld.map(x => jAppToApp(x))
    val sched = jSchedToSched(jSched)

    var networks = List[Network]()
    networks :+= new Network("glob", 100.0, resources.map(x=>x.id))
    val env = new Environment(resources, networks)
    val wld = new Workload(apps)
    val curTime = 0.0
    val framework = new Framework("Spark", "6.6.6")
    val ctx = new Context(tenants, env, wld, sched, curTime, framework)
    ctx
  }

  def writeOutput(path: String, ctx: Context[OperatorInstanceBasedSchedule]) = {
    val env = ctx.env
    val wld = ctx.workload
    val sched = ctx.schedule

    val jenv = env.allResources.map(x => nodeToJNode(x))
    val jwld = wld.apps.map(x => appToJApp(x))
    val jsched = schedToJSched(sched)

    val jctx = new JContext(jenv, jwld, jsched)
    val outJson = write(jctx)
    new PrintWriter(path) { write(outJson); close() }
    println(outJson)

  }

  def writeOutputToStr(ctx: Context[OperatorInstanceBasedSchedule]) = {
    val env = ctx.env
    val wld = ctx.workload
    val sched = ctx.schedule

    val jenv = env.allResources.map(x => nodeToJNode(x))
    val jwld = wld.apps.map(x => appToJApp(x))
    val jsched = schedToJSched(sched)

    val jctx = new JContext(jenv, jwld, jsched)
    val outJson = write(jctx)
    outJson

  }

  // json to obj
  def jCharToChar(jchar: Map[String, Any]): Characteristics = {
    val char = new Characteristics()
    char.cpu = jchar.get("cpu").get.asInstanceOf[BigInt].toDouble
    char
  }

  def jTaskToTask(jtask: JTask, opId: String): OperatorInstance = {
    new OperatorInstance(jtask.name, opId, List[String](), List[String]())
  }

  def jOperToOper(joper: JOperator, appId: String): Operator = {
    new Operator(joper.name, appId, List[String](), List[String](), joper.tasks.map(x => jTaskToTask(x, joper.name)), content=List(), opType="unknown")
  }

  def jAppToApp(japp: JApp): App = {
    new App(japp.name, "user", japp.operators.map(x => jOperToOper(x, japp.name)))
  }

  def jSlotToSlot(jslot: JSlot, contId: String): Slot = {
    new Slot(jslot.name, contId, jCharToChar(jslot.characteristics))
  }

  def jContToCont(jcont: JContainer, resId: String): Container = {
    val cont = new Container(jcont.name, resId, jCharToChar(jcont.characteristics))
    cont.slots = jcont.slots.map(x => jSlotToSlot(x, jcont.name))
    cont
  }

  def jNodeToNode(jnode: JNode): Resource = {
    val res = new Resource(jnode.name, "glob", jCharToChar(jnode.characteristics))
    res.resources = jnode.nodes.map(x => jContToCont(x, jnode.name))
    res
  }

  def jSchedToSched(jsched: Map[String, String]): OperatorInstanceBasedSchedule = {
    val schedmap = new util.HashMap[String,String]()
    jsched.foreach(x => schedmap.put(x._1, x._2))
    val sched = new OperatorInstanceBasedSchedule()
    sched.operatorInstanceIdToSlotId = schedmap
    sched
  }

  // obj to json
  def slotToJSlot(slot: Slot): JSlot = {
    new JSlot(slot.id, charToJChar(slot.characteristics))
  }

  def contToJCont(cont: Container): JContainer = {
    new JContainer(cont.id, charToJChar(cont.characteristics), cont.slots.map(x => slotToJSlot(x)))
  }

  def nodeToJNode(node: Resource): JNode = {
    new JNode(node.id, charToJChar(node.characteristics), node.resources.map(x => contToJCont(x.asInstanceOf[Container])))
  }

  def taskToJTask(task: OperatorInstance): JTask = {
    new JTask(task.id)
  }

  def operToJOper(oper: Operator): JOperator = {
    new JOperator(oper.id, oper.tasks.map(x => taskToJTask(x)))
  }

  def appToJApp(app: App): JApp = {
    new JApp(app.id, app.operators.map(x => operToJOper(x)))
  }

  def schedToJSched(sched:OperatorInstanceBasedSchedule): Map[String, String] = {
    sched.operatorInstanceIdToSlotId.toMap[String, String]
  }

  def charToJChar(characteristics: Characteristics): Map[String, Any] = {
    var res = Map[String, Any]()
    res += ("cpu" -> characteristics.cpu)
    res
  }
}
