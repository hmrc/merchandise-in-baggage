/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.model.api.PaymentRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.Declaration

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait PaymentService {

  def persistDeclaration(persist: Declaration => Future[Boolean], paymentRequest: PaymentRequest)(implicit ec: ExecutionContext): Future[Declaration] =
    for {
      declaration <- Future.fromTry(Try(paymentRequest.toDeclarationInInitialState))
      _           <- persist(declaration)
    } yield declaration
}
