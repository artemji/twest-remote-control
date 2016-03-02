package com.klikatech.app.domain

import java.util.Date

case class Author(handle: String)

case class Tweet(id: Long, author: Author, timestamp: Date, body: String)
