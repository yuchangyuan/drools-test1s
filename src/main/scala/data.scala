package me.ycy.drools.test1.data

import scala.reflect.BeanProperty

import java.util.Date

@SerialVersionUID(1L)
case class MyEvent(
  @BeanProperty val str: String,
  @BeanProperty val timestamp: Long = new Date().getTime
)
