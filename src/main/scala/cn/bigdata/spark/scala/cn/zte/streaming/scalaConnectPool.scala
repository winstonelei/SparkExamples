package cn.bigdata.spark.scala.cn.zte.streaming

import java.sql.{Connection, PreparedStatement, ResultSet}

import org.apache.commons.dbcp.BasicDataSource

/**
  * Created by winstone on 2017/9/6 0006.
  */
object scalaConnectPool {
  var ds:BasicDataSource = null
  def getDataSource={
    if(ds == null){
      ds = new BasicDataSource()
      ds.setUsername("root")
      ds.setPassword("root")
      ds.setUrl("jdbc:mysql://localhost:3306/sparkStreaming")
      ds.setDriverClassName("com.mysql.jdbc.Driver")
      ds.setInitialSize(20)
      ds.setMaxActive(100)
      ds.setMinIdle(50)
      ds.setMaxIdle(100)
      ds.setMaxWait(1000)
      ds.setMinEvictableIdleTimeMillis(5*60*1000)
      ds.setTimeBetweenEvictionRunsMillis(10*60*1000)
      ds.setTestOnBorrow(true)
    }
    ds
  }

  def getConnection : Connection= {
    var connect:Connection = null
    try {
      if(ds != null){
        connect = ds.getConnection
      }else{
        connect = getDataSource.getConnection
      }
    }
    connect
  }

  def shutDownDataSource: Unit=if (ds !=null){ds.close()}

  def closeConnection(rs:ResultSet,ps:PreparedStatement,connect:Connection): Unit ={
    if(rs != null){rs.close}
    if(ps != null){ps.close}
    if(connect != null){connect.close}
  }
}
