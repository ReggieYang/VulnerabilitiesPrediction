
import lucene.{FeatureExtraction, LuceneUtils}
import nvd.data.{DBConnection, NvdItemDao, RawDataProcess, SummaryExtraction}


/**
  * Created by ReggieYang on 2016/10/22.
  */
object Test {


  def main(args: Array[String]) = {
    val readRawData = new RawDataProcess()
    val conn = DBConnection.getConnection

    println("haha")
    //    val products = readRawData.readProduct(conn)

    //        readRawData.readData(conn)
    //    //  read raw data
    //
    val nd = new NvdItemDao(conn)

    val se = new SummaryExtraction(conn)

//        se.featureByCwe()
//        se.featureByImpactScore()
    //    se.featureByProductDB(products)
    //    se.featureByProduct()

    DBConnection.closeConnection

    val fe = new FeatureExtraction
    fe.summaryToFrequencyDir("data\\featureByProduct")

  }
}
