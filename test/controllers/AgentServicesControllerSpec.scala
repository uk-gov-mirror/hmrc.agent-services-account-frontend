package controllers

import auth.{AgentRequest, AuthActions}
import connectors.BackendConnector
import org.scalatest.{BeforeAndAfterEach, Matchers, OptionValues, WordSpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.play.http.HeaderCarrier

class AgentServicesControllerSpec extends WordSpec with Matchers with OptionValues with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val mockConfig: Configuration = app.injector.instanceOf[Configuration]
  val backendConnector: BackendConnector = mock[BackendConnector]


  "AgentServicesController" should {
    "return Status: OK Body: empty" in {
      val authActions = new AuthActions(null, null, null) {
        override def AuthorisedWithAgentAsync(body: AsyncPlayUserRequest): Action[AnyContent] =
          Action.async { implicit request =>
            body(AgentRequest(Arn("TARN0000001"), request))
          }
      }

      val controller = new AgentServicesController(messagesApi, mockConfig, backendConnector, authActions)

      val response = controller.root()(FakeRequest("GET", "/"))

      status(response) shouldBe OK
      contentType(response).get shouldBe HTML
      contentAsString(response) should include(messagesApi("app.name"))
      contentAsString(response) should include("HELLO WORLD")
    }

    "return the redirect returned by authActions when authActions denies access" in {
      val authActions = new AuthActions(null, null, null) with Results {
        override def AuthorisedWithAgentAsync(body: AsyncPlayUserRequest): Action[AnyContent] =
          Action { implicit request =>
            Redirect("/gg/sign-in", 303)
          }
      }

      val controller = new AgentServicesController(messagesApi, mockConfig, backendConnector, authActions)

      val response = controller.root()(FakeRequest("GET", "/"))

      status(response) shouldBe 303
      redirectLocation(response) shouldBe Some("/gg/sign-in")
    }
  }
}


