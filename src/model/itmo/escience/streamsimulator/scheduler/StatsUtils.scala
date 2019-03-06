package itmo.escience.streamsimulator.scheduler

/**
  * Created by 463 on 13.04.2017.
  */
object StatsUtils {
  implicit val opStatsOrdering = new Ordering[OperatorStats]{
    override def compare(x: OperatorStats, y: OperatorStats): Int = x.timestamp.compare(y.timestamp)
  }

  def mergeStats(statsList: List[AppStats]): AppStats = {
    statsList match {
      case stats if stats.isEmpty =>
        null

      case stats if stats.nonEmpty =>
        val mergedOpStats = stats
          .flatMap(_.opStats.flatMap{ case (opId, sts) => sts.map(opstat => opId -> opstat)} )
          .groupBy { case (opId, opStats) => opId }
          .map { case (opId, sts) => opId -> sts.map(_._2).sorted}

        val (begin, end) = mergedOpStats
          .flatMap { case (opId, stats) => stats.map(_.timestamp)}
          .foldLeft((Long.MaxValue, Long.MinValue)) { case((min, max), t) => (Math.min(min, t), Math.max(max, t))}

        AppStats(mergedOpStats, begin, end, stats.head.delta)
    }
  }
}
