package com.klikatech.app.nest

import akka.actor.{Actor, ActorLogging, Props}
import com.firebase.client.Firebase.{AuthListener, CompletionListener}
import com.firebase.client.{DataSnapshot, Firebase, FirebaseError, ValueEventListener}
import com.klikatech.app.domain._
import com.klikatech.app.twitter.TwitterActor
import com.klikatech.app.util.NestClient

import scala.collection.JavaConversions._
import scala.collection.mutable

object NestActorProtocol {

  case class UpdateDevice(device: Device, action: Action)

}

object NestActor {
  val firebaseURL = "https://developer-api.nest.com"

  def props(nestClient: NestClient): Props = Props(new NestActor(nestClient))
}

class NestActor(nestClient: NestClient) extends Actor with ActorLogging {

  import NestActorProtocol._

  val fb = new Firebase(NestActor.firebaseURL)

  val location2thermostat = new mutable.HashMap[String, Thermostat]()

  fb.auth(nestClient.token, new AuthListener {
    def onAuthError(e: FirebaseError) {
      log.info("fb auth error: {}", e)
    }

    def onAuthSuccess(a: AnyRef) {
      log.info("fb auth success: {}", a)

      context.actorOf(TwitterActor.props(nestClient.twitterConfig))

      fb.addValueEventListener(new ValueEventListener {
        def onDataChange(snapshot: DataSnapshot) {
          self ! snapshot
        }

        def onCancelled(err: FirebaseError) {
          self ! err
        }
      })

    }

    def onAuthRevoked(e: FirebaseError) {
      log.info("fb auth revoked: {}", e)
    }
  })

  def receive = {
    case s: DataSnapshot =>
      location2thermostat.clear()
      Option(s.child("devices").child("thermostats")).foreach { therms =>
        if (therms != null) {
          therms.getChildren.foreach { therm =>
            if (therm != null) {
              val thermostat = buildThermostat(therm)
              log.info(s"$thermostat")
              val key = thermostat.name.split(" ").head.toLowerCase
              location2thermostat += (key -> thermostat)
            }
          }
        }
      }
      log.info("got firebase snapshot {}", s)

    case fbe: FirebaseError =>
      log.error("got firebase error {}", fbe)

    case UpdateDevice(device, action) =>
      device match {
        case ThermostatDevice =>
          val location = action.location
          val maybeThermostat = location2thermostat.get(location.toLowerCase)
          maybeThermostat.fold(log.warning("There is no thermostat in such location - {}", location)) { therm =>
            updateThermostat(therm, action)
          }
        case x =>
          log.warning("Not supported type of device - {}", x)
      }
  }

  private def buildThermostat(therm: DataSnapshot): Thermostat = {
    Thermostat(
      therm.child("device_id").getValue.toString,
      therm.child("name").getValue.toString,
      therm.child("target_temperature_c").getValue.toString,
      therm.child("hvac_mode").getValue.toString
    )
  }

  private def updateThermostat(therm: Thermostat, action: Action): Unit = {
    if (therm.hvac_mode != "off") {
      val thermRef = fb.child("devices").child("thermostats").child(therm.device_id)
      val targetTemp = therm.target_temperature_c
      val currentTemp = targetTemp.toDouble
      val temperature = action2Temperature(action, currentTemp)
      if (temperature >= 9 && temperature <= 32) {
        val m = Map[String, AnyRef](
          "target_temperature_c" -> temperature.asInstanceOf[AnyRef]
        )
        thermRef.updateChildren(mapAsJavaMap(m), new CompletionListener {
          def onComplete(err: FirebaseError, fb: Firebase) = {
            if (err != null) {
              log.error("Сompleted with error={}-{}, fb={}", err.getCode, err.getMessage, fb)
            }
          }
        })
        log.info("Thermostat temperature was set to {}", m)
      } else {
        log.warning("Target temperature is limited to a range of 9 - 32°C - {}", temperature)
      }
    } else {
      log.warning("Cannot set target_temperature while mode is off")
    }
  }

  private def action2Temperature(action: Action, currentTemp: Double): Double = {
    action match {
      case UP(_, value) =>
        log.info(s"UP - $value")
        adjustValue(currentTemp + value)
      case DOWN(_, value) =>
        log.info(s"DOWN - $value")
        adjustValue(currentTemp - value)
    }
  }

  private def adjustValue(temp: Double): Double = {
    Math.round(temp * 2) / 2.0d
  }

}
