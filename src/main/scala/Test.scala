
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.{HtmlDivision, HtmlMeta, HtmlPage, HtmlParagraph}
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


//        val webClient = new WebClient()
//        webClient.getOptions.setCssEnabled(false)
//        webClient.getOptions.setJavaScriptEnabled(false)
//        val page: HtmlPage = webClient.getPage("https://ics-cert.us-cert.gov/advisories/ICSA-12-205-01")
//        val des = page.getByXPath("//meta[@name='description']").get(0).asInstanceOf[HtmlMeta]
//        println(des.asText())
//        webClient.closeAllWindows()



    val s = new SummaryExtraction(DBConnection.getConnection)
    s.crawlReferenceData()

    DBConnection.closeConnection





  }
}
