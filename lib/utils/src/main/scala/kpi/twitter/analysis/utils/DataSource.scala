package kpi.twitter.analysis.utils

/**
  * Represents mechanism to poll data bounding
  * requests by time and size of fetched records
  * @tparam T type of data
  */
trait DataSource[T] {
  def poll(timeout: Long, maxRecords: Long): Seq[T]
}
