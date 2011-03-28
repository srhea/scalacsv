// Copyright (c) 2011 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

package org.srhea.csv

import java.lang.StringBuilder
import java.io.File
import collection.immutable.Vector
import collection.immutable.VectorBuilder
import io.Source

object CSV {

  def fromString(s: String, sep: Char = ',') = fromSource(Source.fromString(s), sep)
  def fromFile(path: String, sep: Char = ',') = fromSource(Source.fromFile(path), sep)
  def fromFile(file: File) = fromSource(Source.fromFile(file))

  def fromSource(source: Source, sep: Char = ','): Vector[Vector[String]] = {

    class Parser {
      var state = 0
      var rows = new VectorBuilder[Vector[String]]
      var row = new VectorBuilder[String]
      var rowSize = 0
      var field = new StringBuilder

      def endField() {
        row += field.toString
        rowSize += 1
        field = new StringBuilder
      }

      def endLine() {
        rows += row.result
        row = new VectorBuilder[String]
        rowSize = 0
      }

      def doStateZero(c: Char) {
        c match {
          case '"'  => state = 1
          case '\n' => endField(); endLine()
          case '\r' => state = 3
          case _    => if (c == sep) endField() else field.append(c)
        }
      }

      def parse(): Vector[Vector[String]] = {
        source.foreach { c =>
          state match {
            case 0 => // Initial state.
              doStateZero(c)
            case 1 => // Inside a string.
              c match {
                case '"'  => state = 2
                case _    => field.append(c)
              }
            case 2 => // Did we match one double-quote, or two?
              c match {
                case '"'  => field.append('"'); state = 1
                case '\n' | '\r' => state = 0; doStateZero(c)
                case _ => if (c != sep) throw new MatchError(c); state = 0; doStateZero(c)
              }
            case 3 => // Have '\r', squash following '\n', if any.
              endField(); endLine(); state = 0; if (c != '\n') doStateZero(c)
          }
        }
        state match {
          case 0 => if (field.length > 0 || rowSize > 0) { endField(); endLine() }
          case 2 => endField(); endLine()
        }
        rows.result
      }
    }

    val parser = new Parser
    parser.parse
  }
}
