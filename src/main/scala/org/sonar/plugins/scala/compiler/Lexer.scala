/*
 * SonarQube
 * Copyright (C) 2011-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.scala.compiler

import collection.mutable.ListBuffer

import org.sonar.plugins.scala.language.{Comment, CommentType}
import scala.reflect.io.AbstractFile
import scala.reflect.internal.util.BatchSourceFile

import scala.collection.immutable.Range

/**
 * This class is a wrapper for accessing the lexer of the Scala compiler
 * from Java in a more convenient way.
 *
 * @author Felix Müller
 * @since 0.1
 */
class Lexer {

  import scala.collection.JavaConversions._
  import Compiler.CompilationUnit
  import Compiler.syntaxAnalyzer


  def getTokens(code: String): java.util.List[Token] = {
    val unit = new CompilationUnit(new BatchSourceFile("", code.toCharArray))
    tokenize(unit)
  }

  def getTokensOfFile(path: String): java.util.List[Token] = {
    val unit = new CompilationUnit(new BatchSourceFile(AbstractFile.getFile(path)))
    tokenize(unit)
  }

  private def tokenize(unit: CompilationUnit): java.util.List[Token] = {
    val scanner = new syntaxAnalyzer.UnitScanner(unit)
    val tokens = ListBuffer[Token]()

    scanner.init()
    while (scanner.token != scala.tools.nsc.ast.parser.Tokens.EOF) {
      tokens += Token(scanner.token, scanner.parensAnalyzer.line(scanner.offset) + 1)
      scanner.nextToken()
    }
    tokens
  }

  def getComments(code: String): java.util.List[Comment] = {
    val unit = new CompilationUnit(new BatchSourceFile("", code.toCharArray))
    tokenizeComments(unit)
  }

  def getCommentsOfFile(path: String): java.util.List[Comment] = {
    val unit = new CompilationUnit(new BatchSourceFile(AbstractFile.getFile(path)))
    tokenizeComments(unit)
  }

  private def tokenizeComments(unit: CompilationUnit): java.util.List[Comment] = {
    val comments = ListBuffer[Comment]()
    val scanner = new syntaxAnalyzer.UnitScanner(unit) {

      private var lastDocCommentRange: Option[scala.collection.immutable.Range] = None

      private var foundToken = false

      override def nextToken() {
        super.nextToken()
        foundToken = token != 0
      }

      // This is no longer part of the syntax analyzer UnitParser
      // Took out the override that was here.
      def foundComment(value: String, start: Int, end: Int) = {
        def isHeaderComment(value: String) = {
          !foundToken && comments.isEmpty && value.trim().startsWith("/*")
        }

        lastDocCommentRange match {

          case Some(r: scala.collection.immutable.Range) => {
            if (r.start != start || r.end != end) {
              comments += new Comment(value, CommentType.NORMAL)
            }
          }

          case None => {
            if (isHeaderComment(value)) {
              comments += new Comment(value, CommentType.HEADER)
            } else {
              comments += new Comment(value, CommentType.NORMAL)
            }
          }
        }
      }

      // This is no longer part of the syntax analyzer UnitParser
      // Took out the override that was here.
      def foundDocComment(value: String, start: Int, end: Int) = {
        comments += new Comment(value, CommentType.DOC)
        lastDocCommentRange = Some(Range(start, end))
      }
    }

    scanner.init()
    while (scanner.token != scala.tools.nsc.ast.parser.Tokens.EOF) {
      scanner.nextToken()
    }

    comments
  }
}