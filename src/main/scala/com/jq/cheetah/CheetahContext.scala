package com.jq.cheetah

import com.jq.cheetah.execution.reader.{MysqlReader, Reader}
import com.jq.cheetah.execution.writer.{HiveWriter, Writer}
import com.jq.cheetah.util.ClassUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession}

/**
  * Created by wenxuelin on 2017/6/23.
  */
private[cheetah] class CheetahContext(conf: CheetahConf) extends Serializable{
  @transient
  private val sparkConf: SparkConf = new SparkConf().setMaster("local[2]").setAppName("test")
  @transient
  private val sc: SparkContext = new SparkContext(sparkConf)
  @transient
  private val sqlContext: SparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate()

  protected var reader: Reader = null
  protected var writer: Writer = null

  private[cheetah] def get(key: String): String = conf.get(key)

  private[cheetah] def getConf(): CheetahConf = {
    conf
  }

  def setAppName(name: String): CheetahContext = {
    sparkConf.setAppName(name)
    this
  }

  def read(): DataFrame = {
    if(reader == null) {
      reader = conf.getReaderClassString match {
        case Some(clazzName) => {
          val reader = try {
            val clazz = ClassUtils.classForName(clazzName)
            val constructor = clazz.getConstructor(classOf[SparkSession], classOf[CheetahConf])
            constructor.newInstance(sqlContext, conf).asInstanceOf[Reader]
          } catch {
            case e : ClassNotFoundException => throw new ClassNotFoundException(s"$clazzName not found, ${e.getMessage}")
            case _ : Throwable => throw new RuntimeException
          }
          reader
        }
        //如果没有配置，则默认为MysqlReader
        case _ => new MysqlReader(sqlContext, conf)
      }
    }
    reader.read
  }

  def write(dataFrame: DataFrame): Unit = {
    if(writer == null) {
      writer = conf.getWriterClassString match {
        case Some(clazzName) => {
          val writer = try {
            val clazz = ClassUtils.classForName(clazzName)
            val constructor = clazz.getConstructor(classOf[SparkSession], classOf[CheetahConf])
            constructor.newInstance(sqlContext, conf).asInstanceOf[Writer]
          } catch {
            case e : ClassNotFoundException => throw new ClassNotFoundException(s"$clazzName not found, ${e.getMessage}")
            case _ : Throwable => throw new RuntimeException
          }
          writer
        }
        //如果没有配置，则默认为HiveWriter
        case _ => new HiveWriter(sqlContext, conf)
      }
    }
    writer.write(dataFrame)
  }

  def execute(): Unit = {
    write(read)
  }

  def getSparkConf: SparkConf = sparkConf

  def getSparkContext: SparkContext = sc

  def getSparkSqlContext: SparkSession = sqlContext

  def setReader(reader: Reader): Unit = {
    this.reader = reader
  }

  def setWriter(writer: Writer): Unit = {
    this.writer = writer
  }

  def getReader(): Reader = reader

  def getWriter(): Writer = writer

  def setLogLevel(level: String): CheetahContext ={
    sc.setLogLevel(level)
    this
  }
}

