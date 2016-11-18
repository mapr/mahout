#!/usr/bin/env bash

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Figure out where Spark is installed
export SPARK_HOME=$(readlink "/usr/local/spark")
export MASTER="local[*]"
#"$SPARK_HOME"/bin/load-spark-env.sh # not executable by defult in $SPARK_HOME/bin
"$MAHOUT_HOME"/bin/mahout-load-spark-env.sh
# Find the java binary
if [ -n "${JAVA_HOME}" ]; then
  RUNNER="${JAVA_HOME}/bin/java"
else
  if [ `command -v java` ]; then
    RUNNER="java"
  else
    echo "JAVA_HOME is not set" >&2
    exit 1
  fi
fi

# Find assembly jar
SPARK_JARS_CLASSPATH=$(find $SPARK_HOME/jars -name '*.jar' -not -name 'netty-3.8.0.Final.jar' -printf '%p:' | sed 's/:$//')

LAUNCH_CLASSPATH=$SPARK_JARS_CLASSPATH

# Add the launcher build dir to the classpath if requested.
if [ -n "$SPARK_PREPEND_CLASSES" ]; then
  LAUNCH_CLASSPATH="$SPARK_HOME/launcher/target/scala-$SPARK_SCALA_VERSION/classes:$LAUNCH_CLASSPATH"
fi

export _SPARK_ASSEMBLY="$SPARK_JARS_CLASSPATH"

echo $LAUNCH_CLASSPATH

# The launcher library will print arguments separated by a NULL character, to allow arguments with
# characters that would be otherwise interpreted by the shell. Read that in a while loop, populating
# an array that will be used to exec the final command.
#CMD=()
#while IFS= read -d '' -r ARG; do
#  CMD+=("$ARG")
#done < <("$RUNNER" -cp "$LAUNCH_CLASSPATH" org.apache.spark.launcher.Main "$@")
#exec "${CMD[@]}"
