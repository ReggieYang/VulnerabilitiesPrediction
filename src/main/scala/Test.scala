
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html._
import crawler.HtmlCrawler
import lucene.{FeatureExtraction, LuceneUtils}
import nvd.data.{DBConnection, NvdItemDao, RawDataProcess, SummaryExtraction}


/**
  * Created by ReggieYang on 2016/10/22.
  */
object Test {

  def main(args: Array[String]) = {
    //    val readRawData = new RawDataProcess()
    //    val conn = DBConnection.getConnection
    //    //    val products = readRawData.readProduct(conn)
    //
    //    //        readRawData.readData(conn)
    //    //    //  read raw data
    //    //
    //    val nd = new NvdItemDao(conn)
    //
    //    val se = new SummaryExtraction(conn)
    //
    ////        se.featureByCwe()
    ////        se.featureByImpactScore()
    //    //    se.featureByProductDB(products)
    //    //    se.featureByProduct()
    //
    //    DBConnection.closeConnection
    //
    //    val fe = new FeatureExtraction
    //    fe.summaryToFrequencyDir("data\\test")


//    val keyword = "man in the suit"
//
//    val webClient = new WebClient()
//    webClient.getOptions.setCssEnabled(false)
//    webClient.getOptions.setJavaScriptEnabled(false)
//    val page: HtmlPage = webClient.getPage("https://www.baidu.com/s?wd=" + keyword)
//    val res = page.getByXPath("//a[@class='c-showurl']").toArray().map(_.asInstanceOf[HtmlAnchor].getHrefAttribute)
//    res.foreach(println(_))
//    webClient.closeAllWindows()



    val hc = new HtmlCrawler
    hc.init()
    println(hc.getYahooRes("reggie yang").mkString("\n"))
    hc.close()


    //    val s = new SummaryExtraction(DBConnection.getConnection)
    //    s.crawlReferenceData()
    //
    //    DBConnection.closeConnection


  }
}
