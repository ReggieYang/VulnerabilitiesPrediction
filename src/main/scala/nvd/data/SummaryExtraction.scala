package nvd.data

import java.sql.Connection

import util.Utils._

/**
  * Created by ReggieYang on 2016/10/23.
  */
class SummaryExtraction(conn: Connection) {


  def featureByCwe() = {
    val sql = "select cwe, GROUP_CONCAT(summary Separator'\\t') as summary from vulnerability_copy v where v.summary not like \"\\*\\* REJECT \\*\\*%\" group by v.cwe"
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery(sql)
    while (rs.next()) {
      val pathPrefix = "data\\featureByCwe\\"
      val cwe = if (rs.getString("cwe").isEmpty) "cwe-other" else rs.getString("cwe")
      val content = rs.getString("summary")
      writeFile(pathPrefix + cwe, content)
    }
  }

  def featureByImpactScore() = {
    val begin = "SELECT GROUP_CONCAT(summary Separator'\\t') as summary from vulnerability_copy v where v.impact_score >= "
    val middle = " and v.impact_score < "

    val scoreRange = Range(1, 11)
    scoreRange.foreach(score => {
      val sql = begin + score + middle + (score + 1)
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery(sql)
      while (rs.next()) {
        val pathPrefix = "data\\featureByImpactScore\\"
        val fileName = "Impact Score-" + score
        val content = rs.getString("summary")
        writeFile(pathPrefix + fileName, content)
      }
    })


  }

  def featureByProduct(products:Array[String]) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into product(name, num) values(?,?)")
    val sql = "select 'git' as product, count(*) as num, GROUP_CONCAT(v.summary SEPARATOR '\\t') as summary " +
      "from vulnerability_copy v where v.product like \"git\\t%\" or v.product like \"%\\tgit\" or v.product like \"%\\tgit\\t%\" or v.product like \"%git%\""
    val matchText = "git"

    var i = 0

    products.foreach(product => {
      println(i)
      if (i >= 30000) {
        val sqlNew = sql.replaceAll(matchText, product)
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery(sqlNew)
        while (rs.next()) {
          val pathPrefix = "data\\featureByProduct\\"
          val fileName = product
          val content = rs.getString("num")
          cmd.setString(1, fileName)
          cmd.setString(2, content)
          cmd.addBatch()
          //        writeFile(pathPrefix + fileName, content)
        }
        conn.commit()
        cmd.executeBatch()
      }
      i = i + 1

    })


    cmd.close()
  }

}
