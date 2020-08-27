/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.model.core.Declaration

import scala.concurrent.{ExecutionContext, Future}

trait PaymentService {

  def persistDeclaration(persist: Declaration => Future[Boolean], declaration: Declaration)(implicit ec: ExecutionContext) =
    persist(declaration).map(_ => declaration)
}
