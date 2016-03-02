package com.klikatech.app

import akka.actor.ActorSystem
import com.klikatech.app.nest.NestServiceActor
import com.klikatech.app.nest.NestServiceProtocol.RegisterClient
import com.klikatech.app.util.AppConfig

object Boot extends App {

  val appConfig = AppConfig.loadFromEnvironment()

  val system = ActorSystem("twest-remote-control")

  val nestService = system.actorOf(NestServiceActor.props())

  nestService ! RegisterClient(appConfig.nestClient)

}
