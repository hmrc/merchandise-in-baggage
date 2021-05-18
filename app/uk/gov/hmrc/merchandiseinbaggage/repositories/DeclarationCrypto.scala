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

package uk.gov.hmrc.merchandiseinbaggage.repositories

import play.api.Configuration
import uk.gov.hmrc.crypto.{Crypted, CryptoWithKeysFromConfig, PlainText}
import uk.gov.hmrc.merchandiseinbaggage.model.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DeclarationCrypto @Inject()(configuration: Configuration)(implicit ec: ExecutionContext) {
  private lazy val crypto = new CryptoWithKeysFromConfig("mongodb.encryption", configuration.underlying)

  def cryptoDeclaration(declaration: Declaration, crypto: String => String): Declaration = {
    val name: Name = declaration.nameOfPersonCarryingTheGoods
    val nameCryp = Name(crypto(name.firstName), crypto(name.lastName))

    val emailCryp = declaration.email.map(e => e.copy(email = crypto(e.email)))

    def cryptGoods(goods: Seq[Goods]): Seq[Goods] = goods.map {
      case g: ImportGoods => g.copy(category = crypto(g.category))
      case g: ExportGoods => g.copy(category = crypto(g.category))
    }

    val goodsCryp = cryptGoods(declaration.declarationGoods.goods)

    val amendGoodsCryp: Seq[Amendment] = declaration.amendments.map { am =>
      am.copy(goods = am.goods.copy(goods = cryptGoods(am.goods.goods)))
    }

    declaration.copy(
      nameOfPersonCarryingTheGoods = nameCryp,
      email = emailCryp,
      declarationGoods = DeclarationGoods(goodsCryp),
      amendments = amendGoodsCryp)
  }

  def encryptDeclaration(declaration: Declaration): Declaration = cryptoDeclaration(declaration, encrypt)

  def decryptDeclaration(declaration: Declaration): Declaration = cryptoDeclaration(declaration, decrypt)

  def encrypt(value: String): String =
    crypto.encrypt(PlainText(value)).value

  def decrypt(encrypted: String): String =
    crypto.decrypt(Crypted(encrypted)).value

}
