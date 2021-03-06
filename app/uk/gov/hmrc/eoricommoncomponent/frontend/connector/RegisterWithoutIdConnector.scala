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

package uk.gov.hmrc.eoricommoncomponent.frontend.connector

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.eoricommoncomponent.frontend.audit.Auditable
import uk.gov.hmrc.eoricommoncomponent.frontend.config.AppConfig
import uk.gov.hmrc.eoricommoncomponent.frontend.domain.{RegisterWithoutIdRequestHolder, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegisterWithoutIdConnector @Inject() (http: HttpClient, appConfig: AppConfig, audit: Auditable)(implicit
  ec: ExecutionContext
) {

  private val logger = Logger(this.getClass)
  private val url    = appConfig.getServiceUrl("register-without-id")

  def register(request: RegisterWithoutIDRequest)(implicit hc: HeaderCarrier): Future[RegisterWithoutIDResponse] = {
    auditCallRequest(url, request)
    http.POST[RegisterWithoutIdRequestHolder, RegisterWithoutIdResponseHolder](
      url,
      RegisterWithoutIdRequestHolder(request)
    ) map { resp =>
      logger.info(
        s"Successful. postUrl $url, acknowledgement ref: ${request.requestCommon.acknowledgementReference}, response status: ${resp.registerWithoutIDResponse.responseCommon.statusText}"
      )
      auditCallResponse(url, resp)
      resp.registerWithoutIDResponse
    } recover {
      case e: Throwable =>
        logger.debug(
          s"Failure. postUrl: $url, acknowledgement ref: ${request.requestCommon.acknowledgementReference}, error: $e"
        )
        throw e
    }
  }

  private def auditCallRequest(url: String, request: RegisterWithoutIDRequest)(implicit hc: HeaderCarrier): Unit =
    audit.sendDataEvent(
      transactionName = "customs-registration-without-id",
      path = url,
      detail = request.requestDetail.keyValueMap(),
      eventType = "RegistrationWithoutIdSubmitted"
    )

  private def auditCallResponse(url: String, response: RegisterWithoutIdResponseHolder)(implicit
    hc: HeaderCarrier
  ): Unit =
    audit.sendDataEvent(
      transactionName = "customs-registration-without-id",
      path = url,
      detail = response.registerWithoutIDResponse.keyValueMap(),
      eventType = "RegistrationWithoutIdResult"
    )

}
