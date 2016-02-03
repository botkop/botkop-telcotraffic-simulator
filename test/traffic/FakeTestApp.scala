package traffic

import play.api.test.FakeApplication

object FakeTestApp {

    // override test configuration here
    val config = Map(
        "messageBrokers" -> List("logBroker")
    )

    def apply() = FakeApplication(additionalConfiguration = config)
}
