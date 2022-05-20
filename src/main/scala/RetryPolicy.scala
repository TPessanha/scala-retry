
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.{DurationInt, FiniteDuration}

abstract class RetryPolicy(val maxAttempts: Int = 3) extends LazyLogging {

  protected var count = 1

  def run(): Unit = {
    logger.warn(s"Retrying (retry number: $count)")
    count += 1
  }

  def hasEnded(): Boolean = count > maxAttempts

}

object RetryPolicy {

  def ExponentialBackOff = new ExponentialBackOff()

  def ExponentialBackOff(maxAttempts: Int = 3, baseTimer: FiniteDuration = 5.seconds, jitter: FiniteDuration = 2.seconds) =
    new ExponentialBackOff(maxAttempts, baseTimer, jitter)

  def ImmediateRetry = new ImmediateRetry()

}