package com.moyasar.android.sdk.core.util

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class MadaRangeTest {

  @Test
  fun testInMadaRange_withValidRanges() {
    /// Test cases with All numbers that should be in the madaRanges
    val validNumbers = arrayOf(
      "22337902",
      "22337986",
      "22402030 ",
      "403024",
      "40545400",
      "406136",
      "406996",
      "40719700",
      "40739500",
      "407520",
      "409201",
      "410621",
      "410685",
      "410834",
      "412565",
      "417633",
      "419593",
      "420132",
      "421141",
      "422222",
      "422817",
      "422818",
      "422819",
      "428331",
      "428671",
      "428672",
      "428673",
      "431361",
      "432328",
      "434107",
      "439954",
      "440533",
      "440647",
      "440795",
      "442429",
      "442463",
      "445564",
      "446393",
      "446404",
      "446672",
      "45488707",
      "45501701",
      "455036",
      "455708",
      "457865",
      "457997",
      "458456",
      "462220",
      "468540",
      "468541",
      "468542",
      "468543",
      "474491",
      "483010",
      "483011",
      "483012",
      "484783",
      "486094",
      "486095",
      "486096",
      "489318",
      "489319",
      "49098000",
      "504300",
      "513213",
      "515079",
      "516138",
      "520058",
      "521076",
      "52166100",
      "524130",
      "524514",
      "524940",
      "529415",
      "529741",
      "530060",
      "531196",
      "535825",
      "535989",
      "536023",
      "537767",
      "53973776",
      "543085",
      "543357",
      "549760",
      "554180",
      "555610",
      "558563",
      "588845",
      "588848",
      "588850",
      "604906",
      "636120",
      "968201",
      "968202",
      "968203",
      "968204",
      "968205",
      "968206",
      "968207",
      "968208",
      "968209",
      "968211",
      "968212",
      /// Test full card number sample
      "96821211111111",
      "968212222222222222222",
    )
    for (number in validNumbers) {
      assertTrue("$number should be in madaRanges", inMadaRange(number))
    }
  }

  @Test
  fun testInMadaRange_withInvalidRanges() {
    // Test cases with numbers that should not be in the madaRanges
    val invalidNumbers = arrayOf(
      "12345678", "22222222", "99999999", "400000", "431362",
      "968210", "968213"
    )

    for (number in invalidNumbers) {
      assertFalse("$number should not be in madaRanges", inMadaRange(number))
    }
  }
}