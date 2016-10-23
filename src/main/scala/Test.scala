
import nvd.data.{DBConnection, FeatureExtraction, NvdItemDao, RawDataProcess}


/**
  * Created by ReggieYang on 2016/10/22.
  */
object Test {


  def main(args: Array[String]) = {
    val readRawData = new RawDataProcess()
    val conn = DBConnection.getConnection

    //    readRawData.readData(conn)

    //    val items = readRawData.getItems("data\\rawData\\nvdcve-2.0-2016.xml")
    //    //  read raw data
    //
    //    val nd = new NvdItemDao(conn)
    //    nd.saveList(items)
    //    //  save into db

    val fe = new FeatureExtraction(conn)
    fe.featureByCwe()
    fe.featureByImpactScore()

    //    val x = "cpe:/a:siemens:automation_license_manager:5.3:sp3"
    //    val matchRegex = ".*:(.*):([0-9.]*)"
    //
    //    val product = x.replaceAll(matchRegex, "$1")
    //    val version = x.replaceAll(matchRegex, "$2")
    //    println(product)
    //    println(version)

    DBConnection.closeConnection

  }
}
