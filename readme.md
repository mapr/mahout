# Welcome to Apache Mahout!

Mahout is a scalable machine learning library that implements many different
approaches to machine learning.  The project currently contains
implementations of algorithms for classification, clustering, frequent item
set mining, genetic programming and collaborative filtering. Mahout is 
scalable along three dimensions: It scales to reasonably large data sets by 
leveraging algorithm properties or implementing versions based on Apache 
Hadoop. It scales to your perferred business case as it is distributed under 
a commercially friendly license. In addition it scales in terms of support 
by providing a vibrant, responsive and diverse community.
 
## Getting Started

See the [Quickstart](https://cwiki.apache.org/MAHOUT/quickstart.html).

To compile the sources, run `mvn clean install`
To run all the tests run `mvn test`
To setup your ide run `mvn eclipse:eclipse` or `mvn idea:idea`
For more information on Maven, see Maven's [Project Page](http://maven.apache.org).

For more information on how to contribute, see [How to Contribute](https://cwiki.apache.org/confluence/display/MAHOUT/How+To+Contribute).

## Legal

Please see the NOTICE.txt included in this directory for more information.

## Documentation

See the Mahout [Project page](http://mahout.apache.org/) for current documentation.

## Compiling Mahout with MapR patches

Clone and checkout the `<mahout-version>-mapr` tag or branch of the Apache Mahout 
release version from [GitHub](https://github.com/mapr/mahout). For example,
if you want to compile Mahout version 0.7, check out the `0.7-mapr` tag.

	$ mkdir 0.7-mapr
	$ cd 0.7-mapr
	$ git clone git@github.com/mapr/mahout.git .
	$ git checkout 0.7-mapr
	$ mvn clean -DskipTests

The command line argument `-DskipTests` is optional and allows you to skip running 
the unit tests.

## Using Mahout artifacts in your Maven Project

Add the following dependency to your project's `pom.xml` file:

		<dependency>
			<groupId>com.mapr.mahout</groupId>
  			<artifactId>mahout</artifactId>
			<version>${mapr.mahout.version}</version>
		</dependency>