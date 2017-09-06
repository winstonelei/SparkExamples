package main.scala.study.language

/**
  * Created by dell on 2016/8/25.
  */
abstract class Person
case class Student(age: Int) extends Person
case class Worker(age: Int, salary: Double) extends Person
case object Shared extends Person


object case_class_object {

  def main(args: Array[String]){


    def caseOps(person: Person) =  person match {
      case Student(age) => println("I am " + age + "years old")
      case Worker(_, salary) => println("Wow, I got " + salary)
      case Shared => println("No property")
    }


    caseOps(Student(19))
    caseOps(Shared)

    val worker = Worker(29, 10000.1)
    val worker2 = worker.copy(salary = 19.95)
    val worker3 = worker.copy(age = 30)
    caseOps(Worker(29,100))



    def match_array(arr : Any) = arr match {
      case Array(0) => println("Array:" + "0")
      case Array(x, y) => println("Array:" + x + " " +y)
      case Array(0, _*) => println("Array:" + "0 ...")
      case _ => println("something else")
    }

    match_array(Array(0))
    match_array(Array(0,1))
    match_array(Array(0,1,2,3,4,5))


    val pattern = "([0-9]+) ([a-z]+)".r

     "20150628 hadoop" match {
       case pattern(num, item) => println(num + " : " + item)
    }

  }
}
