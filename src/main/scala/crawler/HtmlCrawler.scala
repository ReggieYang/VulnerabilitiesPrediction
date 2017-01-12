package crawler

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.{HtmlPage, HtmlParagraph}


/**
  * Created by ReggieYang on 2017/1/8.
  */
class HtmlCrawler {

  var siteXpathMap: Map[String, String] = null
  var webClient: WebClient = null

  def init() = {
    siteXpathMap = getSiteXpathMap
    webClient = new WebClient
    webClient.getOptions.setCssEnabled(false)
    webClient.getOptions.setJavaScriptEnabled(false)
  }

  def close() = {
    webClient.closeAllWindows()
  }

  def getSiteXpathMap: Map[String, String] = {
    val delimiter = "\t"
    scala.io.Source.fromFile("data\\crawler\\site_xpath").getLines().
      map(line => {
        (line.split(delimiter)(0), line.split(delimiter)(1))
      }).toMap
  }

  def crawlDescription(url: String): String = {
    val domain = url.replaceAll("((?:http|ftp)://.*?)/.*", "$1")
    siteXpathMap.get(domain) match {
      case None => {
        if(domain.startsWith("http://")||domain.startsWith("ftp://")) ""
        else domain
      }
      case Some(xpath) => {
        var page:HtmlPage = null
        try {
          page = webClient.getPage(url)
        }
        catch {
          case (e:Exception) => println("hahah 404")
          case _ =>
        }
          if ((page == null)||page.getByXPath(xpath).isEmpty) ""
          else {
            val des = page.getByXPath(xpath).get(0)
            des match {
              case des: HtmlParagraph => des.asText()
              case _ => des.toString
            }
          }


      }
    }

  }


}
