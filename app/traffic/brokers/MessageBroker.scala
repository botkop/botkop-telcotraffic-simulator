package traffic.brokers

trait MessageBroker {
    def send(message: String)
}

