# scala-retry

An addon to the Scala language to allow for easy retrying of code

Right now the lib deals with simple retry of code with no extra handling of the given code.

Will maybe add this code to maven repositories one day to be used as a plug and play lib.

# Quickstart

TODO add quick start descriptions

## Simple use

```
Retry {
    <YOUR CODE>
}
```

## Simple use with return type

```
val returnValue = Retry[String] {
    <YOUR CODE>
}
```

## Using basic policy with exponential back off

```
val returnValue = Retry(RetryPolicy.ExponentialBackOff) {
    <YOUR CODE>
}
```

# Retry configuration

The library allows for simple direct use like in the quickstart for ease of use, but it also allows for a degree of
configuration as will be now explained.

The Retry object accepts 3 arguments like such:
`
Retry(policy, isRetryable, isSuccess)
`

## Policy

The policy defines how should the retry process behave.

Every policy will have an `maxAttempts` argument, this sets the max number of retries.

WARNING: On success the retry loop will return the code value, but it will also return the code value if
the `maxAttempts` count is reached, as shown in example '
Is success example' later in this page. Sometimes this is desirable others not so much please be aware of this until the
{Success, Failure} update is implemented.

### Immediate retry

Immediate retry, tries the code again immediately after it fails.

Arguments

* `maxAttempts: Int`- The max number of attempts before giving up (default: 3)

### Exponential backoff

Exponential backoff tries the code again after a given time wait duration, and the wait duration grows exponentially
after each retry.

Arguments

* `maxAttempts: Int`- The max number of attempts before giving up (default: 3)
* `baseTimer: FiniteDuration` - The base timer for the first retry (default: 5 seconds)
* `jitter: FiniteDuration` - The max jitter to add to the wait time between retries, each retry will use a random value
  with range [0, jitter[ (default: 2 seconds)

## isRetryable

A function that receives an instance of `Throwable` class and returns `True` or `False` to decide if the thrown error is
retriable or it can't be recovered.

* `True` -> Continue the retries if the current retry count is less than `maxAttempts`.
* `False` -> Thrown error is not recoverable and give up early.

Default to always retry.

## isSuccess

A function that receives as input the returned value of the code running in the retry loop and returns `True` or `False`
to decide if the returned value is a success or not.

This allows the user to use a given output value as an error and retry again. Sometimes useful for code that doesn't
throw an exception but is still not valid, example API calls that return the API is
`503 Service Unavailable` or other custom logic that is not supported by default.

* `True` -> Stops the retry loop and returns the value.
* `False` -> Continue the retries if the current retry count is less than `maxAttempts`.

# Examples

## Is retryable example

In this example we use a custom function to check if the thrown exeption is an `IllegalArgumentException` if it is we
count that as user error and is not an error we can recover from.

```
def isRetryableCustom(ex: Throwable) = {
  ex match {
    case ex: IllegalArgumentException => false
    case _ => true
  }
}

Retry(RetryPolicy.ImmediateRetry, isRetryable) {
  runCount += 1
  if (runCount < 4) {
    throw new IllegalStateException()
  } else
    "Done"
}

```

Result: Will throw an error.

## Is success example

Will retry to get a random number with range [0,4[ and will only succeed when that number is bigger than 2
Uses the exponential back off policy with a a max attempt of 3, and 1 seconds base timer.

The waiting time in seconds will be (1 -> 2 -> 4) between runs.

```
val policy = new ExponentialBackOff(3, 1.seconds)
Retry(policy, isSuccess = (value: Int) => value > 2) {
      Random.nextInt(4)
    }
```

Result: Some times it will return the number 3 and count as success and sometimes it will fail and return the random
value of the latest loop after all the retry attempts ran out.

# To implement

* Futures: This will allow the use of futures to wait for values.
* Success/Failure: Return values with the scala built in scala.util.{Try,Success,Failure} classes.