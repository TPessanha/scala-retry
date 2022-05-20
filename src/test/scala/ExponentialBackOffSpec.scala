import java.time.{Duration, Instant}
import scala.concurrent.duration.DurationInt

class ExponentialBackOffSpec extends UnitSpec {

  "ExponentialBackOff" should "wait exponentially 3 times using 1 second base timer" in {
    val policy = new ExponentialBackOff(3, 1.seconds)

    var before = Instant.now()
    policy.run()
    var after = Instant.now()
    assert(Duration.between(before, after).getSeconds >= 1)

    before = Instant.now()
    policy.run()
    after = Instant.now()
    assert(Duration.between(before, after).getSeconds >= 2)

    before = Instant.now()
    policy.run()
    after = Instant.now()
    assert(Duration.between(before, after).getSeconds >= 4)

    assert(policy.hasEnded)

  }
}
