#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
#   
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# 
# Requires $HADOOP_HOME to be set.
#
# Figures out the major version of Hadoop we're using and sets commands
# for dfs commands
#
# Run by each example script.

# Find a hadoop shell

BASEMAPR=${MAPR_HOME:-/opt/mapr}
hadoopVersionFile="${BASEMAPR}/conf/hadoop_version"
if [ -f ${hadoopVersionFile} ]
then
hadoop_mode=`cat ${hadoopVersionFile} | grep default_mode | cut -d '=' -f 2`
    if [ "$hadoop_mode" = "yarn" ]; then
	    version_hadoop=`cat ${hadoopVersionFile} | grep yarn_version | cut -d '=' -f 2`
    elif [ "$hadoop_mode" = "classic" ]; then
    	version_hadoop=`cat ${hadoopVersionFile} | grep classic_version | cut -d '=' -f 2`
    else
	    echo 'Unknown hadoop version'
    fi

else
    version_cmd="hadoop version"
    res=`eval $CMD`
    HADOOP_VERSION_PATH=`readlink \`which hadoop\` | awk -F "/" '{print$5}'`
    version_hadoop=`echo ${HADOOP_VERSION_PATH} | cut -d'-' -f 2`
    confDir=${HADOOP_BASE_DIR}${version_hadoop}/conf/
fi
HADOOP_VERSION=hadoop-${version_hadoop}
HADOOP_HOME=${BASEMAPR}/hadoop/${HADOOP_VERSION}/

if [ "$HADOOP_HOME" != "" ] && [ "$MAHOUT_LOCAL" == "" ] ; then
  HADOOP="${HADOOP_HOME}/bin/hadoop"
  if [ ! -e $HADOOP ]; then
    echo "Can't find hadoop in $HADOOP, exiting"
    exit 1
  fi
fi

# Check Hadoop version
v=`${HADOOP_HOME}/bin/hadoop version | egrep "Hadoop [0-9]+.[0-9]+.[0-9]+" | cut -f 2 -d ' ' | cut -f 1 -d '.'`

if [ $v -eq "1" -o $v -eq "0" ]
then
  echo "Discovered Hadoop v0 or v1."
  export DFS="${HADOOP_HOME}/bin/hadoop dfs"
  export DFSRM="$DFS -rmr -skipTrash"
elif [ $v -eq "2" ]
then
  echo "Discovered Hadoop v2."
  export DFS="${HADOOP_HOME}/bin/hdfs dfs"
  export DFSRM="$DFS -rm -r -skipTrash"
else
  echo "Can't determine Hadoop version."
  exit 1
fi
echo "Setting dfs command to $DFS, dfs rm to $DFSRM."

export HVERSION=$v 
