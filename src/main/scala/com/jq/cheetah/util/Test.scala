package com.jq.cheetah.util


import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}

object Test {
  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("test").setMaster("local[2]")

    val session: SparkSession = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate()
    val struct: StructType = StructType(
      List(
        StructField("id", IntegerType, true),
        StructField("name", StringType, true)
      )
    )

    val rdd: RDD[(Int, String)] = session.sparkContext.parallelize(Seq((1, "xhangsan"), (2, "lisi")))
    val res: RDD[Row] = rdd.map(x => Row(x._1, x._2))

    val frame = session.createDataFrame(res, struct)

    frame.show()
    frame.write.mode(SaveMode.Overwrite).saveAsTable("test1")


  }

}
