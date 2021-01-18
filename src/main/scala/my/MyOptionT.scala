package my

import cats.data.OptionT
import cats.implicits._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// https://typelevel.org/cats/datatypes/optiont.html
object MyOptionT extends App {
  println("MyOptionT")

  println("----- F[Option[A]] -> OptionT[F, A] -----")
  val customGreeting:  Future[Option[String]] = Future.successful(Some("welcome back, Lola"))
  val excitedGreeting: Future[Option[String]] = customGreeting.map(_.map(_ + "!"))
  val hasWelcome:      Future[Option[String]] = customGreeting.map(_.filter(_.contains("welcome")))
  val noWelcome:       Future[Option[String]] = customGreeting.map(_.filterNot(_.contains("welcome")))
  val withFallback:            Future[String] = customGreeting.map(_.getOrElse("hello, there!"))
  println(customGreeting)  // Future(Success(Some(welcome back, Lola)))
  println(excitedGreeting) // Future(Success(Some(welcome back, Lola!)))
  println(hasWelcome)      // Future(Success(Some(welcome back, Lola)))
  println(noWelcome)       // Future(Success(None))
  println(withFallback)    // Future(Success(welcome back, Lola))

  val customGreetingT:  OptionT[Future, String] = OptionT(customGreeting)
  val excitedGreetingT: OptionT[Future, String] = customGreetingT.map(_ + "!")
  val hasWelcomeT:      OptionT[Future, String] = customGreetingT.filter(_.contains("welcome"))
  val noWelcomeT:       OptionT[Future, String] = customGreetingT.filterNot(_.contains("welcome"))
  val withFallbackT:             Future[String] = customGreetingT.getOrElse("hello, there!")
  println(customGreetingT)  // OptionT(Future(Success(Some(welcome back, Lola))))
  println(excitedGreetingT) // OptionT(Future(Success(Some(welcome back, Lola!))))
  println(hasWelcomeT)      // OptionT(Future(Success(Some(welcome back, Lola))))
  println(noWelcomeT)       // OptionT(Future(Success(None)))
  println(withFallbackT)    // Future(Success(welcome back, Lola))

  println("----- lift Option[A] or F[A] to OptionT[F, A] -----")
  val greetingFO: Future[Option[String]] = Future.successful(Some("Hello"))
  val firstnameF: Future[String] = Future.successful("Jane")
  val lastnameO:  Option[String] = Some("Doe")
  val ot: OptionT[Future, String] = for {
    g <- OptionT(greetingFO)
    f <- OptionT.liftF(firstnameF)
    l <- OptionT.fromOption[Future](lastnameO)
  } yield s"$g $f $l" // g f l are strings ?!
  val result: Future[Option[String]] = ot.value // get F[O[A]] out of OptionT[F, A]: Future(Some("Hello Jane Doe"))

  println(ot)     // OptionT(Future(<not completed>))
  println(result) // Future(<not completed>)
  Thread.sleep(1000) // without this, Futures *may* not complete
  println(ot)     // OptionT(Future(Success(Some(Hello Jane Doe))))
  println(result) // Future(Success(Some(Hello Jane Doe)))

  println("----- lift A to OptionT[F, A] -----")
  val greet:       OptionT[Future,String] = OptionT.pure("Hola!")
  val greetAlt:    OptionT[Future,String] = OptionT.some("Hi!") // same
  val failedGreet: OptionT[Future,String] = OptionT.none
  println(greet)       // OptionT(Future(Success(Some(Hola!))))
  println(greetAlt)    // OptionT(Future(Success(Some(Hi!))))
  println(failedGreet) // OptionT(Future(Success(None)))

  println("----- ??? -----")
  val defaultGreeting: Future[String] = Future.successful("hello, there")
  // without OptionT
  val greeting: Future[String] = customGreeting.flatMap(custom => {
    println(custom) // Some(welcome back, Lola)
    custom.map(Future.successful).getOrElse(defaultGreeting)
  })
  Thread.sleep(1000) // without this, Futures *may* not complete
  println("greeting:" + greeting) // Future(Success(welcome back, Lola))
  // with OptionT
  val greeting1: Future[String] = customGreetingT.getOrElseF(defaultGreeting) // note the F in getOrElseF
  Thread.sleep(1000) // without this, Futures *may* not complete
  println("greeting1:" + greeting1) // same

}
