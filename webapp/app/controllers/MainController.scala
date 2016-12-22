package controllers

import play.api.mvc.{Action, Controller}
import process._

import play.api.libs.EventSource
import play.api.libs.iteratee._

class MainController extends Controller {

  val (out, channel) = Concurrent.broadcast[String]
  val dataReader = DataReader(channel)
  dataReader.start()

  def index() = Action {
    Ok(views.html.index())
  }

  def classifiedTweets() = Action { implicit req =>
    Ok.feed(out &> EventSource()).as("text/event-stream")
  }
}
