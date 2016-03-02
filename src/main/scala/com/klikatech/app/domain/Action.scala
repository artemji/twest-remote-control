package com.klikatech.app.domain

object Action {
  def parse(command: String): Option[Action] = {
    val terms = command.split(" ")
    if (terms.size == 3) {
      val location = terms.head
      val action = terms(1).toLowerCase
      val value = terms.last
      action match {
        case "up" =>
          Some(UP(location, value.toDouble))
        case "down" =>
          Some(DOWN(location, value.toDouble))
        case _ =>
          None
      }
    } else {
      None
    }
  }
}

sealed trait Action {
  def location: String

  def value: Double
}

case class UP(location: String, value: Double) extends Action

case class DOWN(location: String, value: Double) extends Action
