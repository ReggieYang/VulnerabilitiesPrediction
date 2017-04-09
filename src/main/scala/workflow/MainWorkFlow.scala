package workflow

import java.sql.Connection

import nvd.data.SummaryExtraction
import org.slf4j.LoggerFactory
import util.{ResultSetIt, ResultSetIt2}
import java.util

import weka.ModelTrain
import weka.core.converters.ConverterUtils.DataSink
import weka.core.{Attribute, DenseInstance, Instance, Instances}
import weka.filters.Filter
import weka.filters.unsupervised.attribute.StringToNominal

/**
  * Created by ReggieYang on 2017/4/9.
  */
class MainWorkFlow(conn: Connection) {

  lazy val logger = LoggerFactory.getLogger(this.getClass)

  def run() = {
    //Summary has been saved in db
    val se = new SummaryExtraction(conn)
    val summaryTableName = "search_res_attacker"
    se.writeVectors(summaryTableName)
    logger.info(s"Vectors are saved in table: ${summaryTableName}_vector")

    //    val mt = new ModelTrain()
    //    Array("category", "impact", "amount").foreach(feature => {
    //      getTrainData(feature, summaryTableName + "_vector")
    //      mt.crossValidate2(feature)
    //    })


  }

  def getTrainData(feature: String, vectorTableName: String) = {
    val sql = s"select CONCAT_WS(',', srv.vector, v.$feature) as output " +
      s"FROM $vectorTableName srv, vul_$feature v WHERE srv.product = v.`name`"
    logger.info(sql)
    val stmt = conn.createStatement()
    conn.setAutoCommit(false)
    val rs = stmt.executeQuery(sql)
    val dimension = 100
    val atts = new util.ArrayList[Attribute]()
    Range(0, dimension).foreach(i => {
      val dim = new Attribute(s"v$i")
      atts.add(dim)
    })

    val outputPath = s"E:\\secdata\\wekaData\\train2\\$feature.arff"

    if (feature != "category") {
      logger.info(s"Processing $feature")
      atts.add(new Attribute(feature))
      val instances = new Instances(feature, atts, 0)
      while (rs.next()) {
        val values = rs.getString(1).split(",").map(_.toDouble)
        val di = new DenseInstance(1d, values)
        instances.add(di)
      }
      DataSink.write(outputPath, instances)
    }

    else {
      logger.info(s"Processing $feature")
      atts.add(new Attribute(feature, null: util.ArrayList[String]))
      val instances = new Instances(feature, atts, 0)
      while (rs.next()) {
        val dimensions = rs.getString(1).split(",").take(dimension)
        val category = rs.getString(1).split(",").takeRight(1)(0)
        val values = new Array[Double](dimension + 1)
        Range(0, dimension).foreach(i => {
          values(i) = dimensions(i).toDouble
        })
        values(dimension) = instances.attribute(dimension).addStringValue(category).toDouble
        val di = new DenseInstance(1d, values)
        instances.add(di)
      }
      val filter = new StringToNominal
      filter.setInputFormat(instances)
      DataSink.write(outputPath, Filter.useFilter(instances, filter))
    }

    logger.info(s"Training data is saved at $outputPath")

  }

}
