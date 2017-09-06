package main.scala.study.language

/**
  * 函数闭包
  * Created by stone on 2016/8/25.
  */
object ClosureOps {
  def main(args: Array[String]) {
    val data = List(1, 2, 3, 4, 5, 6)
    var sum = 0
    data.foreach(sum += _)

    def add(more: Int) = (x: Int) => x + more
    val a = add(1)
    val b = add(9999)
    println(a(10))
    println(b(10))


    def sub(less:Int) = (y:Int) => y - less

    val a1 = sub(100)
    val a2= sub(150)

    println(a1(10))

    println(a2(500))


  }
}
