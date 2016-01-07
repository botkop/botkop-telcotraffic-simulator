package traffic

import play.api.test.FakeApplication

object FakeTestApp {

    // override test configuration here
    val config = Map(
        "messageBroker" -> "logBroker"
    )

    def apply() = FakeApplication(additionalConfiguration = config)
}
