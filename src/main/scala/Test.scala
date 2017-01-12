
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.{HtmlDivision, HtmlPage, HtmlParagraph}
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


    //    val webClient = new WebClient()
    //    webClient.getOptions.setCssEnabled(false)
    //    webClient.getOptions.setJavaScriptEnabled(false)
    //    val page: HtmlPage = webClient.getPage("http://www.kb.cert.org/vuls/id/619767")
    //    val des = page.getByXPath("//div[@id='vulnerability-note-content']//table[1]//p").get(0).asInstanceOf[HtmlParagraph]
    //    println(des.asXml())
    //    webClient.closeAllWindows()



    val s = new SummaryExtraction(DBConnection.getConnection)
    s.crawlReferenceData()

    DBConnection.closeConnection





  }
}
