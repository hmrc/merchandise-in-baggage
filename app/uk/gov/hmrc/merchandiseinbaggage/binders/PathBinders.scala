/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.binders

import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationId, Eori, MibReference}

object PathBinders {

  implicit object DeclarationIdBinder extends SimpleObjectBinder[DeclarationId](DeclarationId.apply, _.value)

  implicit def mibRefQueryBinder: QueryStringBindable[MibReference] =
    new QueryStringBindable[MibReference] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, MibReference]] =
        params.get("mibReference") match {
          case Some(ref) => Some(Right(MibReference(ref.mkString)))
          case None      => None
        }

      override def unbind(key: String, value: MibReference): String = value.value
    }

  implicit def eoriQueryBinder: QueryStringBindable[Eori] =
    new QueryStringBindable[Eori] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Eori]] =
        params.get("eori") match {
          case Some(ref) => Some(Right(Eori(ref.mkString)))
          case None      => None
        }

      override def unbind(key: String, value: Eori): String = value.value
    }
}
