

class RetrySpec extends UnitSpec {

  "Retry" should "retry 3 times and succeed" in {
    var runCount = 0

    val value = Retry[String](RetryPolicy.ImmediateRetry) {
      runCount += 1
      if (runCount < 4) {
        throw new Exception("NonFatalError")
      } else
        "Done"
    }

    assert(value == "Done")
  }

  it should "retry and fail on fatal exception" in {
    var runCount = 0

    assertThrows[ThreadDeath] {
      Retry[String] {
        runCount += 1
        if (runCount < 4) {
          throw new ThreadDeath()
        } else
          "Done"
      }
    }
  }

  it should "succeed on retryable exception and fail on non retryable exception" in {
    var runCount = 0

    def isRetryable(ex: Throwable) = {
      ex match {
        case ex: IllegalArgumentException => true
        case _ => false
      }
    }

    val value = Retry(RetryPolicy.ImmediateRetry, isRetryable) {
      runCount += 1
      if (runCount < 4) {
        throw new IllegalArgumentException()
      } else
        "Done"
    }

    assert(value == "Done")

    runCount = 0
    assertThrows[IllegalStateException] {
      Retry(RetryPolicy.ImmediateRetry, isRetryable) {
        runCount += 1
        if (runCount < 4) {
          throw new IllegalStateException()
        } else
          "Done"
      }
    }
  }

  it should "succeed using isSuccess conditional function" in {
    var runCount = 0

    runCount = 0
    val value = Retry(RetryPolicy.ImmediateRetry, isSuccess = (value: Int) => value > 2) {
      runCount += 1
      runCount
    }

    assert(value >= 3)
  }

  it should "retry max tries give up and return incorrect value" in {
    var runCount = 0

    val value = Retry[String] {
      runCount += 1
      if (runCount < 4) {
        throw new Exception()
      } else
        "Done"
    }

    assert(value == "Done")
    assert(runCount == 4)
  }

}
