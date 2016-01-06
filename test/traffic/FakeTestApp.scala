package traffic

import play.api.test.FakeApplication

object FakeTestApp {
    def apply(): FakeApplication = FakeApplication(additionalConfiguration = Map("messageBroker" -> "logBroker"))
}
