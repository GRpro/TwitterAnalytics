package kpi.twitter.analysis.consumer

object SimpleTest {

  def main(args: Array[String]) {
    val info  = Class.forName("kpi.twitter.analysis.BuildInfo")
      .newInstance.asInstanceOf[{ val info: Map[String, String] }]
    println(info.info)

  }
}
