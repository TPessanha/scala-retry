
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random

class ExponentialBackOff(maxAttempts: Int = 3,
                         val baseTimer: FiniteDuration = 5.seconds,
                         val jitter: FiniteDuration = 2.seconds)
  extends RetryPolicy(maxAttempts) {

  override def run(): Unit = {
    val wait = baseTimer.toMillis * Math.pow(2, count - 1).toLong + Random.nextInt(jitter.toMillis.toInt)
    super.run()
    logger.warn(s"Waiting for ${wait}ms")
    Thread.sleep(wait)
  }

}
