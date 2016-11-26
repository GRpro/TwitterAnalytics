package controllers

import play.api.mvc.{Action, Controller}

class ClassifiedTweetsController extends Controller {

  def classifiedTweets() = Action {
    Ok(views.html.index())
  }
}
