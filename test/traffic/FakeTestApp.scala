package traffic

import play.api.test.FakeApplication

object FakeTestApp {

    // override test configuration here
    val config = Map(
        "testBroker.class" -> "traffic.brokers.LogBroker",
        "messageBrokers" -> List("testBroker"),
        "db.default.url" -> "jdbc:sqlite:dist/data/traffic.db"
    )

    def apply() = {
        FakeApplication(additionalConfiguration = config)
    }
}

