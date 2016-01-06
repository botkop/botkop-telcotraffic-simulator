package traffic.brokers

import play.api.Configuration

trait MessageBroker {
    def send(topic: String, message: String)

    def configure(config: Configuration)
}

