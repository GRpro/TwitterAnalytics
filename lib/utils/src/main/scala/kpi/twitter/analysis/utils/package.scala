package kpi.twitter.analysis

package object utils {

  /**
    * kpi.twitter.analysis.BuildInfo class is auto generated before
    * compilation phase by all other modules that depend on this one.
    */
  val buildInfo = Class.forName("kpi.twitter.analysis.BuildInfo")
    .newInstance
    .asInstanceOf[{ val info: Map[String, String] }]
    .info

}
