package com.klikatech.app.util

import java.io.File

import com.klikatech.app.domain.Device
import com.typesafe.config.{Config, ConfigFactory}
import scala.collection.JavaConversions._

case class AppConfig(
                      nestClient: NestClient
                    )

case class NestClient(token: String, twitterConfig: TwitterConfig)

case class TwitterConfig(
                          token: String,
                          tokenSecret: String,
                          apiKey: String,
                          apiSecret: String,
                          device: Device,
                          users: List[String]
                        )

object AppConfig {

  /* Loads your config.
    * To be used from `main()` or equivalent.
    */
  def loadFromEnvironment(): AppConfig =
    load(ConfigUtil.loadFromEnvironment())

  /** Load from a given Typesafe Config object */
  def load(config: Config): AppConfig = {
    AppConfig(
      nestClient = NestClient(
        token = config.getString("nest-client.token"),
        twitterConfig = TwitterConfig(
          token = config.getString("nest-client.twitter-account.twitter-token"),
          tokenSecret = config.getString("nest-client.twitter-account.twitter-token-secret"),
          apiKey = config.getString("nest-client.twitter-account.twitter-api-key"),
          apiSecret = config.getString("nest-client.twitter-account.twitter-api-secret"),
          device = Device(config.getString("nest-client.twitter-account.device")),
          users = config.getStringList("nest-client.twitter-account.users").toList
        )
      )
    )
  }
}

object ConfigUtil {
  /** Utility to replace direct usage of ConfigFactory.load() */
  def loadFromEnvironment(): Config = {
    Option(System.getProperty("config.file"))
      .map(f => {
        val file = new File(f)
        ConfigFactory.parseFile(file).resolve()
      })
      .getOrElse(ConfigFactory.load())
  }
}