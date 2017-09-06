package study.language

import org.apache.spark.deploy.master.DriverInfo

import scala.beans.BeanProperty
import scala.collection.mutable.{ArrayBuffer, HashSet}

/**
  * Created by winstone on 2017/8/31.
  */
class ScalaClassTest {

  @BeanProperty var age =90

  private[this] var value = 100

}

class TestClass51(val name:String = "" ,val age:Int = 0){

  println("name="+ name + " age=" + age)

}

class TestClass52(){
  var name:String = ""
  var age:Int = 0

  println("TestClass begin")

  def this(name:String){
    this()  //第一行调用主构造器
    this.name = name
    println("第一个构造方法")
  }

  def this(name:String , age:Int){
    this(name)  //第一行必须调用主构造器或者其他辅助构造器
    this.age = age
    println("第二个构造方法")
  }

  println("TestClass end")

}

object ScalaClassTest{

  def main(args: Array[String]) {

    var c = new ScalaClassTest

  /*  c.value = 200

    println(c.value)*/

    println(c.getAge)

    val tc = new TestClass51("test",18)

    val teset = new  TestClass52("zhangshuguangsb")

    val drivers = new HashSet[String]
    val completedDrivers = new ArrayBuffer[String]

    drivers += "1212"

    completedDrivers += "999"


    completedDrivers += "000"

    //drivers.add("accc")

    println("-----------------------------")

    println(drivers)

    println(completedDrivers)


  }
}