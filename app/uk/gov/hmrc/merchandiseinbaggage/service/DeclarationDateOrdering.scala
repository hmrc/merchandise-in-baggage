package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration

trait DeclarationDateOrdering {

  implicit val localDateOrdering: Ordering[Declaration] = Ordering.by(_.dateOfDeclaration.toLocalTime)

  def latest(declarations: List[Declaration]): Declaration =
    declarations.sortWith((d1, d2) => d1.dateOfDeclaration.isAfter(d2.dateOfDeclaration)).max
}
