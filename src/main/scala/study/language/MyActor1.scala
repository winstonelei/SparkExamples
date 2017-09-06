package study.language

import akka.actor.{Actor, ActorSystem, Props}

/**
  * Created by winstone on 2017/7/27.
  */
object MyActor1 {

  def main(args: Array[String]) {

    val system = ActorSystem()

    class EchoServer extends Actor {
      def receive ={
        case msg:String => println("echo "+ msg)
      }
    }

    val echoServer = system.actorOf(Props[EchoServer])

    echoServer !"fuck"
  }

}
