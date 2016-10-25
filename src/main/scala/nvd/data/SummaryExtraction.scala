package nvd.data

import java.sql.Connection

import util.Utils._

/**
  * Created by ReggieYang on 2016/10/23.
  */
class SummaryExtraction(conn: Connection) {


  def featureByCwe() = {
    val sql = "select cwe, GROUP_CONCAT(summary Separator'\\t') as summary from feature f, vulnerability v " +
      "where f.summary not like \"\\*\\* REJECT \\*\\*%\" and f.id = v.id group by v.cwe"
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
    val begin = "SELECT GROUP_CONCAT(summary Separator'\\t') as summary from feature f, vulnerability v " +
      "where f.id = v.id and v.impact_score >= "
    val middle = " and v.impact_score < "

    val scoreRange = Range(1, 11)
    scoreRange.foreach(score => {
      val sql = begin + score + middle + (score + 1)
      println(sql)
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

  def featureByProductDB(products: Array[String]) = {
    conn.setAutoCommit(false)
    //    val sql = "select 'git' as product, GROUP_CONCAT(v.summary SEPARATOR '\\t') as summary " +
    //      "from vulnerability_copy v where v.product like \"git\\t%\" or v.product like \"%\\tgit\" or v.product like \"%\\tgit\\t%\" or v.product like \"%git%\""

    //    val sql = "select GROUP_CONCAT(summary SEPARATOR '\\t') as summary from feature f where f.id in " +
    //      "(SELECT v.id FROM vulnerability v WHERE v.product LIKE \"git\\t%\" or v.product like \"%\\tgit\" or v.product like \"%\\tgit\\t%\" or v.product like \"%git%\")"

    val sql = "SELECT 'git' as name, v.id as id FROM vulnerability v " +
      "WHERE v.product LIKE \"git\\t%\" or v.product like \"%\\tgit\" or v.product like \"%\\tgit\\t%\" or v.product like \"git\""

    val cmd = conn.prepareStatement("insert into product(name, vul) values(?,?)")

    val matchText = "git"
    var i = 0

    products.foreach(product => {
      println(i)

      val sqlNew = sql.replaceAll(matchText, product)
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery(sqlNew)
      while (rs.next()) {
        cmd.setString(1, rs.getString("name"))
        cmd.setString(2, rs.getString("id"))
        cmd.addBatch()
        //        val pathPrefix = "data\\featureByProduct\\"
        //        val fileName = product
        //        val content = rs.getString("summary")
        //        writeFile(pathPrefix + fileName, content)
      }
      if ((i % 1000 == 0)||(i >= 30000)) {
        conn.commit()
        cmd.executeBatch()
      }

      i = i + 1
    })

    conn.commit()
    cmd.executeBatch()
    cmd.close()

  }

  def featureByProduct() = {
    val sql = "select p.`name` as name, GROUP_CONCAT(summary SEPARATOR '\\t') as summary from feature f, product p where f.id = p.vul group by p.`name`"

    val stmt = conn.createStatement()
    val rs = stmt.executeQuery(sql)
    var i = 0

    while (rs.next()) {
      println(i)
      i = i + 1
      val pathPrefix = "data\\featureByProduct\\"
      val name = rs.getString("name")
      val content = rs.getString("summary")
      writeFile(pathPrefix + name, content)
    }

  }
}
