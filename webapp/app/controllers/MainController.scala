package controllers

import play.api.mvc.{Action, Controller}

class MainController extends Controller {

  def index() = Action {
    Ok(views.html.index())
  }
}
