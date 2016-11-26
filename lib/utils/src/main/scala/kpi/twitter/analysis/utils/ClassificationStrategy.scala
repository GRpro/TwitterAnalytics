package kpi.twitter.analysis.utils

/**
  * Represents classified data
  * @param cls
  * @param data
  * @tparam C type of classifier
  * @tparam D type of data
  */
case class ClassAndData[C, D](cls: C, data: D) {}

trait ClassificationStrategy[C, D] {
  def classify(data: D): ClassAndData[C, D]
}

trait ClassificationStrategyFactory {
  def create(): ClassificationStrategy[_, _]
}

