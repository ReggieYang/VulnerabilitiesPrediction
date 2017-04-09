package nvd.data

import java.sql.Connection

import nvd.model.{NvdItem, ProductSearch, SearchRes, SearchRes2}
import util.Utils

/**
  * Created by ReggieYang on 2016/10/22.
  */
case class NvdItemDao(conn: Connection) {

  def saveFeature(items: Array[NvdItem]) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into feature(id, reference, summary) values(?,?,?)")

    //    var i = 0
    items.foreach(item => {
      cmd.setString(1, item.id)
      cmd.setString(2, item.reference)
      cmd.setString(3, item.summary)
      cmd.addBatch()
      //      i = i + 1
      //      if (i % 1000 == 0) {
      //        conn.commit()
      //        cmd.executeBatch()
      //      }
    })

    cmd.executeBatch()
    conn.commit()
    cmd.close()

  }

  def saveSearchRes(products: Array[ProductSearch]) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into product_search(product, res) values(?,?)")
    var i = 0
    products.foreach(res => {
      cmd.setString(1, res.product)
      cmd.setString(2, res.res.mkString("\t"))
      cmd.addBatch()
      i = i + 1

      if (i % 1000 == 0) {
        cmd.executeBatch()
        conn.commit()
      }
    })

    cmd.executeBatch()
    conn.commit()
    cmd.close()
  }

  def saveSearchRes(product: ProductSearch) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into product_search(product, res) values(?,?)")
    cmd.setString(1, product.product)
    cmd.setString(2, product.res.mkString("\t"))
    cmd.addBatch()
    cmd.executeBatch()
    conn.commit()
    cmd.close()
  }

  def saveProduct(items: Array[NvdItem]) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into product_version(vul, product) values(?,?)")
    var i = 0
    items.foreach(item => {
      item.product.split("\t").foreach(product => {
        cmd.setString(1, item.id)
        cmd.setString(2, product)
        cmd.addBatch()
        i = i + 1

        if (i % 1000 == 0) {
          cmd.executeBatch()
          conn.commit()
        }
      })
    })

    cmd.executeBatch()
    conn.commit()
    cmd.close()
  }

  def saveItem(items: Array[NvdItem]) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into vulnerability2(id, product, impact_score, cwe) values(?,?,?,?)")

    var i = 0
    items.foreach(item => {

      cmd.setString(1, item.id)
      cmd.setString(2, item.product)
      cmd.setDouble(3, item.impactScore)
      cmd.setString(4, item.cwe)
      cmd.addBatch()
      i = i + 1
      if (i % 1000 == 0) {
        cmd.executeBatch()
        conn.commit()
      }
    })

    cmd.executeBatch()
    conn.commit()
    cmd.close()

  }

  def getProductVul() = {

  }

  def saveSearchSiteRes(results: Array[SearchRes]) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into search_res(url, res) values(?, ?)")
    var i = 0
    results.foreach(res => {
      cmd.setString(1, res.url)
      cmd.setString(2, res.res)
      cmd.addBatch()
//      i = i + 1
//
//      if (i % 1 == 0) {
//        cmd.executeBatch()
//        conn.commit()
//      }
    })

    cmd.executeBatch()
    conn.commit()
    cmd.close()
  }


  def saveSearchSiteRes(res: SearchRes) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("insert into search_res(url, res) values(?, ?)")
    cmd.setString(1, res.url)
    cmd.setString(2, res.res)
    cmd.addBatch()
    cmd.executeBatch()
    conn.commit()
    cmd.close()
  }

  def saveSearchSiteRes(res: SearchRes2) = {
    conn.setAutoCommit(false)
    val cmd = conn.prepareStatement("update search_res2 set res = ? where id = ?")
    cmd.setString(1, res.res)
    cmd.setInt(2, res.id)
    cmd.addBatch()
    cmd.executeBatch()
    conn.commit()
    cmd.close()
  }

}
