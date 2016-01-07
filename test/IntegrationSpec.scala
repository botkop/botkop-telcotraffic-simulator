import org.junit.runner._
import org.openqa.selenium.firefox.FirefoxDriver
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test._
import traffic.FakeTestApp

/**
  * add your integration spec here.
  * An integration test will fire up a whole play application in a real (or headless) browser
  */
@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

    "Application" should {

        "work from within a browser" in new WithBrowser(webDriver = new FirefoxDriver(), app = FakeTestApp()) {

            browser.goTo(s"http://localhost:$port/simulator")

            // need some time to build up browser contents
            Thread.sleep(2000)

            browser.pageSource must contain("Traffic Simulator")
        }
    }
}
