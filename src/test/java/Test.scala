import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object Test {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName("test")
    val session: SparkSession = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate()

    val frame: DataFrame = session.sql("desc test")

    frame.foreach(print(_))


  }
}
