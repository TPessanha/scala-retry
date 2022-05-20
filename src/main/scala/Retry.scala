
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

class Retry[T](val policy: RetryPolicy,
               val isRetryable: Throwable => Boolean = (_: Throwable) => true,
               val isSuccess: T => Boolean = (_: T) => true) extends LazyLogging {

  @annotation.tailrec
  final def run(fn: => T): T = {
    Try {
      fn
    } match {
      case Success(value) =>
        if (isSuccess(value))
          value
        else if (!policy.hasEnded()) {
          policy.run()
          run(fn)
        }
        else
          value
      case Failure(exception) =>
        if (isRetryable(exception)) {
          if (policy.hasEnded())
            throw exception

          policy.run()
          run(fn)
        }
        else {
          logger.error("Non retryable exception thrown: " + exception)
          throw exception
        }
    }
  }

}

object Retry {

  def apply[T](policy: RetryPolicy = RetryPolicy.ImmediateRetry,
               isRetryable: Throwable => Boolean = (_: Throwable) => true,
               isSuccess: T => Boolean = (_: T) => true
              ): (=> T) => T = {
    new Retry[T](policy, isRetryable, isSuccess).run(_)
  }

  def apply[T](fn: => T): T = new Retry[T](RetryPolicy.ImmediateRetry).run(fn)

  def ExponentialBackOff[T](fn: => T): T = new Retry[T](new ExponentialBackOff()).run(fn)

  def ImmediateRetry[T](fn: => T): T = new Retry[T](new ImmediateRetry()).run(fn)

}
