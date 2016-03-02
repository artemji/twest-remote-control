package com.klikatech.app.domain

object Device {
  def apply(device: String): Device = {
    device match {
      case "Thermostat" => ThermostatDevice
    }
  }
}

sealed trait Device

case object ThermostatDevice extends Device
