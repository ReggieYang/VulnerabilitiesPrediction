package crawler

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html._
import org.w3c.dom.html.HTMLElement


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
      case None => url
      case Some(xpath) => {
        var page: HtmlPage = null
        try {
          page = webClient.getPage(url)
        }
        catch {
          case (e: Exception) => println("hahah 404")
          case _ =>
        }
        val newXpath = xpath.split("&")(0)
        val attribute = if (xpath.contains("&")) xpath.split("&")(1) else ""

        if ((page == null) || page.getByXPath(newXpath).isEmpty) url
        else {
          val des = page.getByXPath(newXpath).get(0)
          des match {
            case des: DomElement => getContent(des, attribute)
            case _ => des.toString
          }
        }
      }
    }
  }

  def getContent(ele: DomElement, attr: String) = if (attr.isEmpty) ele.asText() else ele.getAttribute(attr)

  def getBaiduRes(kw: String): Array[String] = {
    val page: HtmlPage = webClient.getPage("https://www.baidu.com/s?wd=" + kw)
    page.getByXPath("//a[@class='c-showurl']").toArray().map(_.asInstanceOf[HtmlAnchor].getHrefAttribute)
  }

  def getYahooRes(kw: String): Array[String] = {
    val page: HtmlPage = webClient.getPage("https://sg.search.yahoo.com/search?p=" + kw)
    page.getByXPath("//a[@class=' td-u']").toArray().map(_.asInstanceOf[HtmlAnchor].getHrefAttribute)
  }

  def getBingRes(kw: String): Array[String] = {
    val page: HtmlPage = webClient.getPage("http://bing.com/search?q=" + kw)
    page.getByXPath("//cite").toArray().map(_.asInstanceOf[HtmlCitation].asText())
  }

}
