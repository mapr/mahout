package org.apache.mahout.sparkbindings.blas

import org.apache.mahout.sparkbindings.drm

import scala.reflect.ClassTag
import org.apache.mahout.sparkbindings.drm.DrmRddInput
import org.apache.mahout.math.drm.logical.OpPar
import org.apache.spark.rdd.RDD
import scala.math._

import org.apache.mahout.logging._

/** Physical adjustment of parallelism */
object Par {

  private final implicit val log = getLog(Par.getClass)

  def exec[K](op: OpPar[K], src: DrmRddInput[K]): DrmRddInput[K] = {

    implicit val ktag = op.keyClassTag
    val srcBlockified = src.isBlockified

    val srcRdd = if (srcBlockified) src.toBlockifiedDrmRdd(op.ncol) else src.toDrmRdd()
    val srcNParts = srcRdd.partitions.size

    // To what size?
    val targetParts = if (op.minSplits > 0) srcNParts max op.minSplits
    else if (op.exactSplits > 0) op.exactSplits
    else /* auto adjustment */ {
      val stdParallelism = srcRdd.context.getConf.get("spark.default.parallelism", "1").toInt
      val x1 = 0.95 * stdParallelism
      if (srcNParts <= ceil(x1)) ceil(x1).toInt else ceil(2 * x1).toInt
    }

    debug(s"par $srcNParts => $targetParts.")

    if (targetParts > srcNParts) {

      // Expanding. Always requires deblockified stuff. May require re-shuffling.
      val rdd = src.toDrmRdd().repartition(numPartitions = targetParts)

      rdd

    } else if (targetParts < srcNParts) {
      // Shrinking.

      if (srcBlockified) {
        drm.rbind(src.toBlockifiedDrmRdd(op.ncol).coalesce(numPartitions = targetParts))
      } else {
        src.toDrmRdd().coalesce(numPartitions = targetParts)
      }
    } else {
      // no adjustment required.
      src
    }

  }

}
