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

package uk.gov.hmrc.eoricommoncomponent.frontend.services

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.hmrc.eoricommoncomponent.frontend.connector.Save4LaterConnector
import uk.gov.hmrc.eoricommoncomponent.frontend.domain.{CacheIds, GroupId, InternalId}
import uk.gov.hmrc.eoricommoncomponent.frontend.services.cache.CachedData
import uk.gov.hmrc.eoricommoncomponent.frontend.services.subscription._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserGroupIdSubscriptionStatusCheckService @Inject() (
  subscriptionStatusService: SubscriptionStatusService,
  save4LaterConnector: Save4LaterConnector
)(implicit ec: ExecutionContext) {
  private val idType = "SAFE"

  def checksToProceed(groupId: GroupId, internalId: InternalId)(continue: => Future[Result])(
    userIsInProcess: => Future[Result]
  )(otherUserWithinGroupIsInProcess: => Future[Result])(implicit hc: HeaderCarrier): Future[Result] =
    save4LaterConnector
      .get[CacheIds](groupId.id, CachedData.groupIdKey)
      .flatMap {
        case Some(cacheIds) =>
          subscriptionStatusService
            .getStatus(idType, cacheIds.safeId.id)
            .flatMap {
              case NewSubscription | SubscriptionRejected =>
                save4LaterConnector
                  .delete(groupId.id)
                  .flatMap(_ => continue)
              case _ =>
                if (cacheIds.internalId == internalId)
                  userIsInProcess
                else
                  otherUserWithinGroupIsInProcess
            }
        case _ =>
          continue
      }

}
