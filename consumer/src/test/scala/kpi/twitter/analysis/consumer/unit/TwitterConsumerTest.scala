package kpi.twitter.analysis.consumer.unit

import kpi.twitter.analysis.consumer.TwitterConsumer
import org.scalatest.FunSuite

class TwitterConsumerTest extends FunSuite {

  test("filter on empty string should return Nil") {
    assert(Nil === TwitterConsumer.filters(""))
  }

  test("filter correct string") {
    assert(Seq("#tag1", "#tag2") === TwitterConsumer.filters("#tag1,#tag2"))
  }

  test("filter should trim spaces") {
    assert(Seq("#tag1", "#tag2") === TwitterConsumer.filters("#tag1,  #tag2"))
  }

  test("filter should ignore empty values") {
    assert(Seq("#tag1", "#tag2") === TwitterConsumer.filters("#tag1,#tag2, , ,,"))
  }
}
