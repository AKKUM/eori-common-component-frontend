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

package uk.gov.hmrc.eoricommoncomponent.frontend.controllers.migration

import javax.inject.{Inject, Singleton}
import play.api.Application
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.eoricommoncomponent.frontend.controllers.CdsController
import uk.gov.hmrc.eoricommoncomponent.frontend.models.Journey
import uk.gov.hmrc.eoricommoncomponent.frontend.views.html.migration.return_user

import scala.concurrent.ExecutionContext

@Singleton
class ReturnUserController @Inject() (
  override val currentApp: Application,
  override val authConnector: AuthConnector,
  view: return_user,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends CdsController(mcc) {

  def show(): Action[AnyContent] = Action { implicit request =>
    Ok(view(Journey.Subscribe))
  }

}