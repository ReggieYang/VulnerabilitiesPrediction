package nvd.data

import java.io.{BufferedWriter, File, FileWriter}
import java.sql.Connection

import nvd.model.NvdItem
import org.dom4j.Element
import org.dom4j.io.SAXReader
import util.Utils._

/**
  * Created by ReggieYang on 2016/10/22.
  */
class RawDataProcess {

  def readData(conn: Connection) = {
    val yearList = Range(2002, 2017)
    val nd = new NvdItemDao(conn)
    yearList.foreach(year => {
      val filePath = "data\\rawData\\nvdcve-2.0-" + year + ".xml"
      nd.saveList(getItems(filePath))

    })
  }

  def readProduct(conn: Connection): Array[String] = {
    val yearList = Range(2002, 2017)
    var pSet = Set[String]()

    yearList.foreach(year => {
      val filePath = "data\\rawData\\nvdcve-2.0-" + year + ".xml"
      val temp = getProductList(filePath)
      pSet = pSet ++ temp
    })

//    val bw = new BufferedWriter(new FileWriter(new File("data\\productList\\productList")))
//
//    pSet.foreach(product => {
//      bw.write(product + "\n")
//    })
//
//    bw.close()
    pSet.toArray
  }

  def concatElement(entry: Element, name: String, sonName: String, attribute: String = ""): String = {
    val node = entry.element(name)
    if (node == null) {
      EmptyString
    }
    else {
      val elements = node.elements(sonName).toArray().map(_.asInstanceOf[Element])
      var res = EmptyString
      elements.foreach(element => {
        val temp = if (!attribute.isEmpty) element.attribute(attribute).getValue else element.getStringValue
        res = res + temp + EmptyString + TabSep
      })
      res
    }
  }


  def getItems(filePath: String): Array[NvdItem] = {
    val reader = new SAXReader()
    val document = reader.read(new File(filePath))
    val node = document.getRootElement

    val elements = node.elements().toArray().map(_.asInstanceOf[Element])
    elements.map(entry => {
      val id = entry.element("cve-id").getStringValue
      val products2 = concatElement(entry, "vulnerable-software-list", "product")
      val products = extractProduct(products2)
      val impactScore = if (entry.element("cvss") != null) entry.element("cvss").element("base_metrics").element("score").getStringValue.toDouble else 0d
      val cwe = if (entry.element("cwe") != null) entry.element("cwe").attribute("id").getValue else EmptyString
      val reference = concatElement(entry, "references", "reference", "href")
      val summary = entry.element("summary").getStringValue
      val cvdItem = NvdItem(id, products, impactScore, reference, cwe, summary)
      cvdItem
    })
  }

  def getProductList(filePath: String): Set[String] = {
    val reader = new SAXReader()
    val document = reader.read(new File(filePath))
    val node = document.getRootElement
    var pSet = Set[String]()

    val elements = node.elements().toArray().map(_.asInstanceOf[Element])
    elements.foreach(entry => {
      val products2 = concatElement(entry, "vulnerable-software-list", "product")
      val products = extractProduct(products2)
      val tempSet = products.split(TabSep).toSet
      pSet = pSet ++ tempSet
    })

    pSet
  }

  def extractProduct(products: String): String = {
    val productList = products.split(TabSep)
    productList.map(product => {
      if (!product.isEmpty) {
        val title = product.split(":")
        if (title.length >= 4) title(3) else title(title.length - 1)
      }
    }).toSet.mkString(TabSep)
  }


}
