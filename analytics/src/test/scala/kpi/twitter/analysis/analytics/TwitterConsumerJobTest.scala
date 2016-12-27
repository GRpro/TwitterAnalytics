package kpi.twitter.analysis.analytics

import org.scalatest.FunSuite

class TwitterConsumerJobTest extends FunSuite {

  test("filter on empty string should return Nil") {
    assert(Nil === TwitterConsumerJob.filters(""))
  }

  test("filter correct string") {
    assert(Seq("#tag1", "#tag2") === TwitterConsumerJob.filters("#tag1,#tag2"))
  }

  test("filter should trim spaces") {
    assert(Seq("#tag1", "#tag2") === TwitterConsumerJob.filters("#tag1,  #tag2"))
  }

  test("filter should ignore empty values") {
    assert(Seq("#tag1", "#tag2") === TwitterConsumerJob.filters("#tag1,#tag2, , ,,"))
  }
}
