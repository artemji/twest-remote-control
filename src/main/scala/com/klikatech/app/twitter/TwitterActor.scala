package com.klikatech.app.twitter

import java.util.Date

import akka.actor.{Props, Actor, ActorLogging}
import com.klikatech.app.domain.{Author, Tweet, Action}
import com.klikatech.app.nest.NestActorProtocol.UpdateDevice
import com.klikatech.app.util.TwitterConfig
import twitter4j._
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder

object TwitterActor {
  def props(twitterConfig: TwitterConfig): Props = Props(new TwitterActor(twitterConfig))
}

class TwitterActor(twitterConfig: TwitterConfig) extends Actor with ActorLogging {

  val factory = new TwitterStreamFactory(new ConfigurationBuilder().build())

  val twitterStream = factory.getInstance()

  twitterStream.setOAuthConsumer(twitterConfig.apiKey, twitterConfig.apiSecret)
  twitterStream.setOAuthAccessToken(new AccessToken(twitterConfig.token, twitterConfig.tokenSecret))
  twitterStream.addListener(userListener)
  twitterStream.user(twitterConfig.users: _*)

  override def receive: Receive = {
    case tweet: Tweet =>
      log.info(s"$tweet")
      Action.parse(tweet.body).fold(log.warning("Cannot parse this command")) { action =>
        context.parent ! UpdateDevice(device = twitterConfig.device, action)
      }
  }

  def userListener = new UserStreamListener {

    override def onStatus(s: Status): Unit = {
      self ! Tweet(s.getId, Author(s.getUser.getScreenName), new Date(s.getCreatedAt.getTime), s.getText)
    }

    override def onFriendList(longs: Array[Long]): Unit = {}

    override def onUserListUnsubscription(user: User, user1: User, userList: UserList): Unit = {}

    override def onBlock(user: User, user1: User): Unit = {}

    override def onUserListSubscription(user: User, user1: User, userList: UserList): Unit = {}

    override def onFollow(user: User, user1: User): Unit = {}

    override def onUserListMemberAddition(user: User, user1: User, userList: UserList): Unit = {}

    override def onDirectMessage(directMessage: DirectMessage): Unit = {}

    override def onUnblock(user: User, user1: User): Unit = {}

    override def onUserListUpdate(user: User, userList: UserList): Unit = {}

    override def onUnfollow(user: User, user1: User): Unit = {}

    override def onUserProfileUpdate(user: User): Unit = {}

    override def onUserListMemberDeletion(user: User, user1: User, userList: UserList): Unit = {}

    override def onUserDeletion(l: Long): Unit = {}

    override def onRetweetedRetweet(user: User, user1: User, status: Status): Unit = {}

    override def onFavoritedRetweet(user: User, user1: User, status: Status): Unit = {}

    override def onDeletionNotice(l: Long, l1: Long): Unit = {}

    override def onFavorite(user: User, user1: User, status: Status): Unit = {}

    override def onQuotedTweet(user: User, user1: User, status: Status): Unit = {}

    override def onUnfavorite(user: User, user1: User, status: Status): Unit = {}

    override def onUserSuspension(l: Long): Unit = {}

    override def onUserListDeletion(user: User, userList: UserList): Unit = {}

    override def onUserListCreation(user: User, userList: UserList): Unit = {}

    override def onStallWarning(stallWarning: StallWarning): Unit = {}

    override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {
      log.info(s"$statusDeletionNotice")
    }

    override def onScrubGeo(l: Long, l1: Long): Unit = {}

    override def onTrackLimitationNotice(i: Int): Unit = {}

    override def onException(ex: Exception): Unit = {
      log.error(ex.getCause, "twitter error")
    }
  }

}
