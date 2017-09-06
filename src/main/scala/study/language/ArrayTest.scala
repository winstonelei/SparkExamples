package study.language

import java.util

import scala.collection.mutable
import scala.util.Random

/**
  * Created by winstone on 2017/8/29.
  */
class ArrayTest {

}

object ArrayTest{


  def main(args: Array[String]) {
    val arr = new Array[Int](8)
    println(arr)
    val arryBuffer = arr.toBuffer
    arryBuffer.append(121)
    arryBuffer.append(122)
    arryBuffer.append(123)
    println(arryBuffer(9))

/*    for(i <- arryBuffer){
        println(i)
    }*/

    val arr1 = Array("hadoop","spark","flume")
    for(i <- (0 until arr1.length)){
        println(arr1(i))
    }


    //构造不可变map
    val smap = Map("zs"->89,"lis"->90)

    //smap +=("aaa"->80)

    val muMap = scala.collection.mutable.Map("zs"->89,"lis"->90)

    println(muMap)

    //新增键值对

    muMap +=("xiaohong"->77)

    println(muMap)


    //遍历map

    //1.通过模式匹配
    //for((x,y) <- muMap)println("key="+x +" value="+y)


    //通过key值
    //for(k <- muMap.keySet)println("username="+k+" vlaue="+muMap(k))

    //通过foreach

   // muMap.foreach{case (m,n) => println("key"+m+" value"+n) }


    //可变set
    val data = scala.collection.mutable.Set.empty[Int]
    data ++=List(1,2,3)
    data += 4
    println(data)

    var set = scala.collection.mutable.HashSet[Int]()
    set +=12
    println(set)

    var set1= scala.collection.mutable.Set[Int](34,35,36)
    set1+=22



    println("---------------------")

    for(i <- set1)println(i)

    println("---------------------")


    val set2 = new util.HashSet[Int]()
    set2.add(221)
    set2.add(333)
    println(set2)




    val arr3 = Array("hello",1,2.0,ArrayTest)


    val v = arr3(Random.nextInt(4))
    v match{
      case x:Int => println("int="+x)
      case y:String => println("string="+y)
      case z:Double => println("double="+z)
      case _=> throw new Exception("not match exception")
    }










  }



}
