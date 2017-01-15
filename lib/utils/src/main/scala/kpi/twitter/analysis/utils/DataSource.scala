package kpi.twitter.analysis.utils

/**
  * Represents poll-based mechanism for retrieving data.
  * Requests are bounded by time and size
  * @tparam T type of record
  */
trait DataSource[T] {
  def poll(timeout: Long, maxRecords: Long): Seq[T]
}
