package itmo.escience.streamsimulator.algorithms.prediction

import itmo.escience.streamsimulator.statistics._
import org.json4s.jackson.Serialization._
import scala.collection.JavaConversions._
import scala.util.Random

/**
  * Created by mikhail on 09.03.2017.
  */
object Predictor {
  implicit val formats = org.json4s.DefaultFormats
  case class JStat(times: List[Long], values: List[Long])

  def signalPrediction(stat: OperatorInstanceStats) = {
    val inputStringjson = stat2json(stat)
    println(inputStringjson)
    // TODO SET HOST AND PORT!!! 192.168.0.221:666 -> ?????
    val result = get("http://127.0.0.1:5000/signal/" + inputStringjson)
    val resultObj = read[JStat](result)
    val resultInput = resultObj.times.zip(resultObj.values)
    val outStat = new OperatorInstanceStats(stat.id, resultInput, stat.throughputs, stat.latencies, stat.delta)
    outStat
  }

  def get(url: String) = scala.io.Source.fromURL(url).mkString

  def stat2json(stat: OperatorInstanceStats) = {
    val unzipInput = stat.inputs.unzip
    val times = unzipInput._1
    val values = unzipInput._2
    write(JStat(times,values))
  }

//  def main(args: Array[String]): Unit = {
//    var input = List[CountPerWindow]()
//    for (i <- 0 until 2000) {
//      input :+= (i.toLong, (math.sin(i / 3.0)*1000 + Random.nextInt(10)).toLong + 100)
//    }
//    val inStat = new OperatorInstanceStats("id", input, List[CountPerWindow](), List[MeanPerWindow](), null)
//    val res = signalPrediction(inStat)
//    print(res)
//  }
}
