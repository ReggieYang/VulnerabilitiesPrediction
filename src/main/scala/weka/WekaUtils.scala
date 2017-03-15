package weka

import java.io.{File, PrintWriter}
import java.sql.Connection

import util.{ResultSetIt, Utils}
import weka.classifiers.{AbstractClassifier, Classifier}
import weka.core.converters.ConverterUtils.DataSource

/**
  * Created by ReggieYang on 2017/3/9.
  */
object WekaUtils {

  def testModel(model: Classifier, testFile: String, classIndex: Int) = {
    val testSet = DataSource.read(testFile)
    testSet.setClassIndex(classIndex)
    Range(0, testSet.size()).foreach(index => testSet.get(index).setClassValue(model.classifyInstance(testSet.get(index))))
    testSet
  }

  def genArff(conn: Connection, output: String = "impact", path:String = "E:\\secdata\\wekaData\\") = {
    val sql = s"SELECT concat_ws(',', vector, $output) from feature_output2 where $output is not null"
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery(sql)
    val data = new ResultSetIt(rs).toArray
    val pw = new PrintWriter(new File(s"$path$output.arff"))
    pw.println(s"@relation text_$output")
    Range(1, 101).foreach(i => {
      pw.println(s"@attribute v$i numeric")
    })

    val outputType = output match {
      case "amount" => "numeric"
      case "impact" => "numeric"
      case "category" => "string"
      case _ => "string"
    }

    pw.println(s"@attribute $output $outputType")
    pw.println("@data")
    data.foreach(line => pw.println(line))
    pw.close()

  }

}
