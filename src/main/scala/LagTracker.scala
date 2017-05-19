package chess

import LagTracker._

case class LagTracker(
    quota: Centis = Centis(200),
    history: Option[DecayingStats] = None
) {

  def onMove(lag: Centis) = {
    val lagComp = lag.nonNeg atMost maxLagComp atMost quota

    val recorder = history getOrElse initialHistory

    (lagComp, copy(
      quota = (quota + quotaGain - lagComp) atMost quotaMax,
      history = Some(recorder.record(lagComp.centis))
    ))
  }

  def bestEstimate = Centis(history.fold(0) { _.mean.toInt })

  def lowEstimate = history.fold(Centis(0)) { h =>
    Centis((h.mean - h.stdDev).toInt).nonNeg
  }
}

object LagTracker {
  val quotaGain = Centis(100)
  val quotaMax = Centis(500)
  val maxLagComp = Centis(300)
  private val initialHistory = DecayingStats.empty(baseVarience = 10 * 10)
}
