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

import java.io.File

import org.apache.log4j.PropertyConfigurator
import org.apache.mahout.sparkbindings._
import org.apache.spark.SparkConf
import org.apache.spark.repl.{SparkILoop, Main => sMain}

import scala.tools.nsc.GenericRunnerSettings


object Main {
  private var _interp: SparkILoop = _

  private var hasErrors = false

  private def scalaOptionError(msg: String): Unit = {
    hasErrors = true
    Console.err.println(msg)
  }

  private def getMaster = {
    val master = System.getenv("MASTER")
    if(master == null){
      throw new RuntimeException("MASTER should be set")
    }

    master
  }

  def main(args: Array[String]) {
    PropertyConfigurator.configure(getMahoutHome() + "/conf/log4j.properties")

    System.setProperty("scala.usejavacp", "true")
    _interp = new MahoutSparkILoop()

    // It looks like we need to initialize this too, since some Spark shell initilaization code
    // expects it
    org.apache.spark.repl.Main.interp = _interp

    sMain.conf.setIfMissing("spark.master", getMaster)

    val jars = getUserJars(sMain.conf, isShell = true).mkString(File.pathSeparator)
    val interpArguments = List(
      "-Yrepl-class-based",
      "-Yrepl-outdir", s"${sMain.outputDir.getAbsolutePath}",
      "-classpath", jars
    ) ++ args.toList

    val settings = new GenericRunnerSettings(scalaOptionError)
    settings.processArguments(interpArguments, true)

    if (!hasErrors) {
      _interp.process(settings) // Repl starts and goes in loop of R.E.P.L
      Option(sMain.sparkContext).foreach(_.stop)
    }
  }

  /**
    * In YARN mode this method returns a union of the jar files pointed by "spark.jars" and the
    * "spark.yarn.dist.jars" properties, while in other modes it returns the jar files pointed by
    * only the "spark.jars" property.
    */
  def getUserJars(conf: SparkConf, isShell: Boolean = false): Seq[String] = {
    val sparkJars = conf.getOption("spark.jars")
    val master = sMain.conf.get("spark.master")
    if (master == "yarn" && isShell) {
      val yarnJars = conf.getOption("spark.yarn.dist.jars")
      unionFileLists(sparkJars, yarnJars).toSeq
    } else {
      sparkJars.map(_.split(",")).map(_.filter(_.nonEmpty)).toSeq.flatten
    }
  }

  /**
    * Unions two comma-separated lists of files and filters out empty strings.
    */
  def unionFileLists(leftList: Option[String], rightList: Option[String]): Set[String] = {
    var allFiles = Set[String]()
    leftList.foreach { value => allFiles ++= value.split(",") }
    rightList.foreach { value => allFiles ++= value.split(",") }
    allFiles.filter { _.nonEmpty }
  }
}
