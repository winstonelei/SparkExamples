package main.scala.study.language

import scala.collection.immutable.HashSet
import scala.collection.mutable.ListBuffer



/**
  * Created by dell on 2016/8/25.
  */
object Curring {

  def main(args: Array[String]) {


    def multiple(x: Int, y: Int) = x * y
    def multipleOne(x: Int) = (y: Int)  => x * y
    println(multipleOne(6)(7))

    def curring(x: Int)(y: Int) = x * y

    println(curring(10)(10))

   def curringtest(x:Int)(y:Int)(z:Int)=x*y*z
    println(curringtest(12)(10)(10))

    val a = Array("Hello", "Spark")
    val b = Array("hello", "spark")

    println(a.corresponds(b)(_.equalsIgnoreCase(_)))

    val array = Array(1,2,3,4,5)
    for(i<- (0 until array.length))
      print( array(i))

    val response = for(e <- array if e%2==0)yield e*10

    println(response.toBuffer)

    println(response.sum)

    println(response.max)


    //map的第一种创建方式
    var scores = Map("tom"->45,"lisi"->90)

    println("tom")

    scores+=("tom"->50)


    scores.foreach(x=>{
      println("key="+x._1)
      println("value="+x._2)
    })

    //map 的第二种方式

    val scores2 = Map(("xiaoming",88),("lisi",100))

    println(scores2.getOrElse("lisi2",77))


    //元组
     val tuple = ("xuyang",12,"yy")

     println(tuple._1)

     val arr = Array(("zhangsan",200),("lisi",300))

     println(arr.toMap)


     val zip1 = Array("1","2","3")

     val zip2 = Array("xiaoming","xiaoli")

     println(zip1.zip(zip2).toBuffer)


      val list1 = List(1,2,4)

      val list2 = 0::list1

      println(list2)

      val list3 = list1.::(0)

      println(list3)

      val lst4 = 0 +: list1

      println(lst4)

      val lst5 = list1.+:(0)

      println(lst5)


      val lst6 = list1 :+ 3

      println(lst6)

      val lst0 = List(4,5,6)

      println(lst6 ++ lst0)

      println(lst0 ++ lst6)


      //将2个list合并成一个新的List
      val lst7 = list1 ++ lst0

      //将lst0插入到lst1前面生成一个新的集合
      val lst8 = list1 ++: lst0

      //将lst0插入到lst1前面生成一个新的集合
      val lst9 = list1.:::(lst0)

       println(lst9)


       //可变的序列

       val arrayLst0 = ListBuffer[Int](1,2,4)

       val arrayLst1 = new ListBuffer[Int]()

       arrayLst1+=1212

       arrayLst1.append(55)

       println(arrayLst1)

       arrayLst0 ++=arrayLst1

       println(arrayLst0)


       //不可变的set
       val set1 = new HashSet[Int]()
       //将元素和set1合并生成一个新的set，原有set不变
       val set2 = set1 + 4
       //set中元素不能重复
       val set3 = set1 ++ Set(5, 6, 7)
       val set0 = Set(1,3,4) ++ set1

       println(set0)
       println(set0.getClass)




       val map1 = new scala.collection.mutable.HashMap[String, Int]()
       //向map中添加数据
       map1("spark") = 1
       map1 += (("hadoop", 2))
       map1.put("storm", 3)
       println(map1)


      //从map中移除元素
      map1 -= "spark"
      map1.remove("hadoop")
      println(map1)


     val m2 = new scala.collection.immutable.HashMap[String,Int]()



  }



}
