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

package uk.gov.hmrc.merchandiseinbaggage.pact

import com.itv.scalapact.ScalaPactVerifyDsl
import com.itv.scalapact.circe13.JsonInstances
import com.itv.scalapact.http4s21.impl.HttpInstances
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication

trait PactVerifySuite extends BaseSpecWithApplication with ScalaPactVerifyDsl with HttpInstances with JsonInstances
