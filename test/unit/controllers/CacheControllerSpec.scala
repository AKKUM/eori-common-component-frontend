/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.{AnyContent, Request, Result, Session}
import play.mvc.Http.Status.SEE_OTHER
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.eoricommoncomponent.frontend.controllers.routes._
import uk.gov.hmrc.eoricommoncomponent.frontend.controllers.subscription.CacheController
import uk.gov.hmrc.eoricommoncomponent.frontend.domain.subscription.SubscriptionDetails
import uk.gov.hmrc.eoricommoncomponent.frontend.models.Journey
import uk.gov.hmrc.eoricommoncomponent.frontend.services.cache.{RequestSessionData, SessionCache}
import uk.gov.hmrc.http.HeaderCarrier
import util.ControllerSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.{AuthActionMock, SessionBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CacheControllerSpec extends ControllerSpec with AuthActionMock {

  private val mockAuthConnector  = mock[AuthConnector]
  private val mockAuthAction     = authAction(mockAuthConnector)
  private val mockSessionCache   = mock[SessionCache]
  private val requestSessionData = new RequestSessionData()
  private val userId: String     = "someUserId"
  private val mockRequest        = mock[Request[AnyContent]]

  val controller = new CacheController(mockAuthAction, mockSessionCache, mcc, requestSessionData)

  "Cache controller" should {

    assertNotLoggedInAndCdsEnrolmentChecksForSubscribe(
      mockAuthConnector,
      controller.clearCache(atarService, Journey.Subscribe)
    )

    "clear cache for subscription holder for subscription journey" in {
      withAuthorisedUser(userId, mockAuthConnector)
      when(mockSessionCache.saveSubscriptionDetails(any[SubscriptionDetails])(any[HeaderCarrier]))
        .thenReturn(Future.successful(true))
      val result: Result =
        await(
          controller.clearCache(atarService, Journey.Subscribe).apply(SessionBuilder.buildRequestWithSession(userId))
        )

      status(result) shouldBe SEE_OTHER
      result.header.headers("Location") should be(EmailController.form(atarService, Journey.Subscribe).url)
      assertSessionDoesNotContainKeys(result.session(mockRequest))
    }

    "clear cache for subscription holder for get an eori journey" in {
      withAuthorisedUser(userId, mockAuthConnector)
      when(mockSessionCache.saveSubscriptionDetails(any[SubscriptionDetails])(any[HeaderCarrier]))
        .thenReturn(Future.successful(true))
      val result: Result =
        await(
          controller.clearCache(atarService, Journey.Register).apply(SessionBuilder.buildRequestWithSession(userId))
        )
      status(result) shouldBe SEE_OTHER
      result.header.headers("Location") should be(EmailController.form(atarService, Journey.Register).url)
      assertSessionDoesNotContainKeys(result.session(mockRequest))
    }
  }

  private def assertSessionDoesNotContainKeys(session: Session): Unit =
    session.data should contain noneOf (
      key("selected-organisation-type"),
      key("subscription-flow"),
      key("uri-before-subscription-flow")
    )

}
