package me.ycy.drools.test1

import org.drools.runtime.StatefulKnowledgeSession
import org.drools.builder.KnowledgeBuilder
import org.drools.KnowledgeBaseFactory
import org.drools.KnowledgeBase
import org.drools.builder.ResourceType
import org.drools.builder.KnowledgeBuilderFactory
import org.drools.io.ResourceFactory
import org.drools.logger.KnowledgeRuntimeLoggerFactory
import org.drools.conf.EventProcessingOption
import org.drools.marshalling.MarshallerFactory


import org.drools.runtime.conf.ClockTypeOption
import org.drools.time.SessionClock
import org.drools.time.SessionPseudoClock

import java.util.concurrent.TimeUnit

//import org.drools.runtime.conf.ClockTypeOption
import java.util.Date

import java.io.{File, FileInputStream, FileOutputStream}

import data._

object Test1 {
  private var sesOpt: Option[StatefulKnowledgeSession] = None

  def addRule(kbuilder: KnowledgeBuilder, drl: String): Boolean = {
    var ret = true

    kbuilder.add(
      ResourceFactory.newClassPathResource(drl),
      ResourceType.DRL
    )

    if (kbuilder.hasErrors) {
      ret = false
      println(s"Error build ${drl}")
      val i = kbuilder.getErrors.iterator
      while (i.hasNext) {
        println(i.next.getMessage)
      }
    }

    ret
  }

  def advance(ses: StatefulKnowledgeSession, n: Int) = {
    if (isRealtime) Thread.sleep(n)
    else {
      val pclk = ses.getSessionClock[SessionPseudoClock]()
      pclk.advanceTime(n, TimeUnit.MILLISECONDS)
    }
  }

  def getSession(): StatefulKnowledgeSession = {
    if (sesOpt.isDefined) return sesOpt.get

    val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder()

    List("data.drl").foreach {r ⇒
      if (!addRule(kbuilder, r)) throw new RuntimeException("rule build error.")
    }

    val kconf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration()
    kconf.setOption(EventProcessingOption.STREAM)

    val kbase = KnowledgeBaseFactory.newKnowledgeBase(kconf)
    kbase.addKnowledgePackages(kbuilder.getKnowledgePackages())

    val sconf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration()
    if (isRealtime) sconf.setOption(ClockTypeOption.get("realtime"))
    else sconf.setOption(ClockTypeOption.get("pseudo"))

    val ksession = kbase.newStatefulKnowledgeSession(sconf, null)
    sesOpt = Some(ksession)

    // val logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession)
    val sessionId = ksession.getId()
    println(s"session id = ${sessionId}")

    ksession
  }

  var isRealtime = false

  def main(args : Array[String]) : Unit = {
    println("Creating Knowledge Session")

    val ses = getSession()

    val stm = ses.getWorkingMemoryEntryPoint("stream")
    val clk = ses.getSessionClock[SessionClock]()

    for (i ← 0 until 5) {
      advance(ses, 500)
      val e = MyEvent(s"event ${i}", clk.getCurrentTime)
      stm.insert(e)
      ses.insert(e)

      advance(ses, 500)
      println(s"-------------- fire at ${clk.getCurrentTime} ---------------")
      ses.fireAllRules()
    }

    ses.dispose()
  }
}
