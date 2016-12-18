package kpi.twitter.analysis.utils

/**
  * Continuous stream of data
  * @tparam T type of incoming data
  */
trait DataSource[T] {
  def poll(timeout: Long, maxRecords: Long): Seq[T]
}
