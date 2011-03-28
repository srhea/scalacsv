// Copyright (c) 2011 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

package org.srhea.csv

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class CSVSpec extends FlatSpec with ShouldMatchers {

  "the empty string" should "parse to an empty vector" in {
    CSV.fromString("") should equal (Vector())
  }

  "a file that ends in a newline" should "be okay" in {
    CSV.fromString("foo,bar\n") should equal (Vector(Vector("foo", "bar")))
  }

  "a file that doesn't end in a newline" should "be okay" in {
    CSV.fromString("foo,bar") should equal (Vector(Vector("foo", "bar")))
  }

  "leading spaces" should "be preserved" in {
    // See RFC 4180.
    CSV.fromString(" foo,bar") should equal (Vector(Vector(" foo", "bar")))
    CSV.fromString("foo, bar") should equal (Vector(Vector("foo", " bar")))
  }

  "trailing spaces" should "be preserved" in {
    // See RFC 4180.
    CSV.fromString("foo ,bar") should equal (Vector(Vector("foo ", "bar")))
    CSV.fromString("foo,bar ") should equal (Vector(Vector("foo", "bar ")))
  }

  "\\r\\n" should "be parsed as a newline" in {
    CSV.fromString("foo\r\nbar") should equal (Vector(Vector("foo"), Vector("bar")))
  }

  "empty fields denoted by successive commas" should "be okay" in {
    CSV.fromString("foo,,bar") should equal (Vector(Vector("foo", "", "bar")))
  }

  "empty fields denoted by double quotes" should "be okay" in {
    CSV.fromString("foo,\"\",bar") should equal (Vector(Vector("foo", "", "bar")))
  }

  "semicolon-separated mode" should "parse correctly" in {
    CSV.fromString("foo;bar", ';') should equal (Vector(Vector("foo", "bar")))
  }

  "the Wikipedia example" should "parse correctly" in {
    // example from http://en.wikipedia.org/wiki/Comma-separated_values
    val input = """Year,Make,Model,Description,Price
1997,Ford,E350,"ac, abs, moon",3000.00
1999,Chevy,"Venture ""Extended Edition""" + "\"\"\"" + ""","",4900.00
1999,Chevy,"Venture ""Extended Edition, Very Large""" + "\"\"\"" + ""","",5000.00
1996,Jeep,Grand Cherokee,"MUST SELL!
air, moon roof, loaded",4799.00"""
    val expected = Vector(
      Vector("Year","Make","Model","Description","Price"),
      Vector("1997","Ford","E350","ac, abs, moon","3000.00"),
      Vector("1999","Chevy","Venture \"Extended Edition\"","","4900.00"),
      Vector("1999","Chevy","Venture \"Extended Edition, Very Large\"","","5000.00"),
      Vector("1996","Jeep","Grand Cherokee","MUST SELL!\nair, moon roof, loaded","4799.00")
    )
    CSV.fromString(input) should equal (expected)
  }

  "the Google Docs example" should "parse correctly" in {
    val expected = Vector(
      Vector("foo", "bar"),
      Vector("hi, mom", "a single \""),
      Vector("two \"\"", ", and \""),
      Vector("a newline\nin this field"))
    CSV.fromFile("src/test/resources/google-docs.csv") should equal (expected)
  }

  "the Excel Mac 2008 example" should "parse correctly" in {
    // Excel on Mac encodes all newlines as a single '\r'.
    val expected = Vector(
      Vector("foo", "bar"),
      Vector("hi, mom", "a single \""),
      Vector("two \"\"", ", and \""),
      Vector("a newline\rin this field", ""))
    CSV.fromFile("src/test/resources/excel-mac-2008.csv") should equal (expected)
  }
}
