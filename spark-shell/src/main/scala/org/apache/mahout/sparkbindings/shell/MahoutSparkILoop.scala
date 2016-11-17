/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.sparkbindings.shell

import org.apache.log4j.PropertyConfigurator
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.repl.SparkILoop
import scala.tools.nsc.Properties
import org.apache.mahout.sparkbindings._


class MahoutSparkILoop extends SparkILoop {

  private var _interp: SparkILoop = _

  private var sdc: SparkDistributedContext = _


  // Hack: for some very unclear reason, log4j is not picking up log4j.properties in Spark conf/ even
  // though the latter is added to the classpath. So we force it to pick it.
  PropertyConfigurator.configure(getMahoutHome() + "/conf/log4j.properties")

  System.setProperty("scala.usejavacp", "true")

  _interp = this

  // It looks like we need to initialize this too, since some Spark shell initilaization code
  // expects it
  org.apache.spark.repl.Main.interp = _interp

  // this is technically not part of Spark's explicitly defined Developer API though
  // nothing in the SparkILoopInit.scala file is marked as such.
  override def initializeSpark() {

    intp.beQuietDuring {

      // get the spark context, at the same time create and store a mahout distributed context.
      processLine("""
          @transient val spark = if (org.apache.spark.repl.Main.sparkSession != null) {
            org.apache.spark.repl.Main.sparkSession
          } else {
            org.apache.spark.repl.Main.createSparkSession()
          }

          @transient val sc = {
            val _sc = spark.sparkContext
            if (_sc.getConf.getBoolean("spark.ui.reverseProxy", false)) {
              val proxyUrl = _sc.getConf.get("spark.ui.reverseProxyUrl", null)
              if (proxyUrl != null) {
                println(s"Spark Context Web UI is available at ${proxyUrl}/proxy/${_sc.applicationId}")
              } else {
                println(s"Spark Context Web UI is available at Spark Master Public URL")
              }
            } else {
              _sc.uiWebUrl.foreach {
                webUrl => println(s"Spark context Web UI available at ${webUrl}")
              }
            }
            println("Spark context available as 'sc' " +
              s"(master = ${_sc.master}, app id = ${_sc.applicationId}).")
            println("Spark session available as 'spark'.")
            _sc
          }
          val jars = sc.jars.map(new java.io.File(_).getAbsolutePath)
          val sdc = org.apache.mahout.sparkbindings.mahoutSparkContext(sc, jars)
          println("Mahout distributed context is available as \"implicit val sdc\".")
      """)

      processLine("import org.apache.spark.SparkContext._")
      processLine("import spark.implicits._")
      processLine("import spark.sql")
      processLine("import org.apache.spark.sql.functions._")
      processLine("import collection.JavaConversions._")
      processLine("import org.apache.mahout.sparkbindings._")
      processLine("import decompositions._")
      processLine("import RLikeDrmOps._")
      processLine("import drm._")
      processLine("import RLikeOps._")
      processLine("import scalabindings._")
      processLine("import org.apache.mahout.math._")

      replayCommandStack = Nil // remove above commands from session history.
    }
  }

  // this is technically not part of Spark's explicitly defined Developer API though
  // nothing in the SparkILoopInit.scala file is marked as such..
  override def printWelcome(): Unit = {
    echo(
      """
                         _                 _
         _ __ ___   __ _| |__   ___  _   _| |_
        | '_ ` _ \ / _` | '_ \ / _ \| | | | __|
        | | | | | | (_| | | | | (_) | |_| | |_
        |_| |_| |_|\__,_|_| |_|\___/ \__,_|\__|  version 0.12.2

      """)
    import Properties._
    val welcomeMsg = "Using Scala %s (%s, Java %s)".format(
      versionString, javaVmName, javaVersion)
    echo(welcomeMsg)
    echo("Type in expressions to have them evaluated.")
    echo("Type :help for more information.")
  }
}

