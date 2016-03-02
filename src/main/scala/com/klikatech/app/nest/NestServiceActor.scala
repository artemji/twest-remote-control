package com.klikatech.app.nest

import akka.actor.{Actor, ActorLogging, Props}
import com.klikatech.app.util.NestClient

object NestServiceProtocol {

  case class RegisterClient(nestClient: NestClient)

}

object NestServiceActor {
  def props(): Props = Props(new NestServiceActor)
}

class NestServiceActor extends Actor with ActorLogging {

  import NestServiceProtocol._

  def receive: Receive = {

    case RegisterClient(client) =>
      context.actorOf(NestActor.props(client))

  }

}
