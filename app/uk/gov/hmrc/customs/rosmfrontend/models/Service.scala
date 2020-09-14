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

package uk.gov.hmrc.customs.rosmfrontend.models

import play.api.libs.json.{Reads, Writes}
import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.customs.rosmfrontend.util.Constants

object Service extends Enumeration {

  val ATaR = Value

  private val atarPath = "atar"

  implicit val reads: Reads[Service.Value] = Reads.enumNameReads(Service)
  implicit val writes: Writes[Service.Value] = Writes.enumNameWrites

  implicit lazy val pathBindable: PathBindable[Service.Value] = new PathBindable[Service.Value] {

    override def bind(key: String, value: String): Either[String, Service.Value] =
      value match {
        case `atarPath` => Right(ATaR)
        case _                   => Left(Constants.INVALID_PATH_PARAM)
      }

    override def unbind(key: String, value: Service.Value): String =
      value match {
        case ATaR     => atarPath
      }
  }

  def apply(journey: String): Service.Value = journey match {
    case `atarPath` => ATaR
  }

  implicit def queryBindable(implicit pathBindable: PathBindable[Service.Value]): QueryStringBindable[Service.Value] =
    new QueryStringBindable[Service.Value] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Service.Value]] =
        params.get(key).map(seq => pathBindable.bind(key, seq.headOption.getOrElse("")))

      override def unbind(key: String, value: Service.Value): String = pathBindable.unbind(key, value)
    }
}
