package me.yingrui.segment.core.disambiguation

import me.yingrui.segment.core.{SegmentResult, Word}
import me.yingrui.segment.tools.accurary.SegmentResultCompareHook

/**
  *
  * @param expect
  * @param actual
  */
class DisambiguationToSerialLabels(expect: SegmentResult, actual: SegmentResult) extends SegmentResultCompareHook {

  import DisambiguationToSerialLabels._

  var serialLabels = List[(String, String, Int)]()

  override def compeleted: Unit = {

  }

  override def foundCorrectWordHook(expectWord: Word, actualWord: Word, expectWordIndex: Int, actualWordIndex: Int): Unit = {
    add(expectWord.name, LABEL_A, expectWord.pos)
  }

  override def foundError(expectWord: Word, actualWord: Word, expectWordIndex: Int, actualWordIndex: Int): Unit = {
    addLabel(expectWord, actualWord, expectWordIndex, actualWordIndex)
  }

  private def addLabel(expectWord: Word, actualWord: Word, expectWordIndex: Int, actualWordIndex: Int): String = {
    if (isExpectWordContainsActualWord(expectWord, actualWord)) {
      if (isWordStart(expectWord, actualWord)) {
        add(actualWord.name, LABEL_SB, actualWord.pos)
      } else if (isWordEnding(expectWord, actualWord, expectWordIndex, actualWordIndex)) {
        add(actualWord.name, LABEL_SE, actualWord.pos)
      } else if (isWordMiddle(expectWord, actualWord)) {
        add(actualWord.name, LABEL_SM, actualWord.pos)
      } else {
        add(actualWord.name, LABEL_A, actualWord.pos)
      }
    } else {
      if (isActualWordStartsWithExpectedWordAndLastCharacterBelongsToNextWord(expectWord, actualWord, expectWordIndex)) {
        add(actualWord.name, LABEL_LC, actualWord.pos)
      } else if (isTwoCharactersWord(actualWord) && isActualWordComposedOfTwoExpectedWords(expectWord, actualWord, expectWordIndex)) {
        add(actualWord.name, LABEL_U, actualWord.pos)
      } else if (isThreeCharactersWord(actualWord) && isActualWordComposedOfTwoExpectedWords(expectWord, actualWord, expectWordIndex)) {
        add(actualWord.name, LABEL_U, actualWord.pos)
      } else if (isThreeCharactersWord(actualWord) && isActualWordComposedOfThreeExpectedWords(expectWord, actualWord, expectWordIndex)) {
        add(actualWord.name, LABEL_UT, actualWord.pos)
      } else if (isFourCharactersWord(actualWord) && isTwoCharactersWord(expectWord) && isActualWordComposedOfTwoExpectedWords(expectWord, actualWord, expectWordIndex)) {
        add(actualWord.name, LABEL_U, actualWord.pos)
      } else if (isTwoCharactersWord(actualWord) && shouldActualWordSeparateToJoinPreviousAndNextWords(expectWord, actualWord, expectWordIndex)) {
        add(actualWord.name, LABEL_SH, actualWord.pos)
      } else if (firstCharacterBelongsToLastWord(expectWord, actualWord, expectWordIndex, actualWordIndex)) {
        add(actualWord.name, LABEL_FL, actualWord.pos)
      } else {
        add(actualWord.name, LABEL_A, actualWord.pos)
      }
    }
  }

  private def firstCharacterBelongsToLastWord(expectWord: Word, actualWord: Word, expectWordIndex: Int, actualWordIndex: Int): Boolean = {
    val word = expectWord.name.last + getNextExpectWord(expectWordIndex)
    (isLastLabel(LABEL_SB) || isLastLabel(LABEL_SM)) && isNotLastWord(expectWordIndex) && (word == actualWord.name)
  }

  private def isWordEnding(expectWord: Word, actualWord: Word, expectWordIndex: Int, actualWordIndex: Int): Boolean = {
    val lengthEqual = expect.getWordEndAt(expectWordIndex) == actual.getWordEndAt(actualWordIndex)
    lengthEqual && expectWord.name.endsWith(actualWord.name) && (isLastLabel(LABEL_SB) || isLastLabel(LABEL_SM) || isLastLabel(LABEL_SH) || isLastLabel(LABEL_LC))
  }

  private def isWordMiddle(expectWord: Word, actualWord: Word): Boolean = {
    expectWord.name.contains(actualWord.name) && (isLastLabel(LABEL_SB) || isLastLabel(LABEL_SM) || isLastLabel(LABEL_LC))
  }

  private def isWordStart(expectWord: Word, actualWord: Word): Boolean = {
    expectWord.name.startsWith(actualWord.name) && !isLastLabel(LABEL_SB) && !isLastLabel(LABEL_SM)
  }

  private def shouldActualWordSeparateToJoinPreviousAndNextWords(expectWord: Word, actualWord: Word, expectWordIndex: Int): Boolean = {
    isNotLastWord(expectWordIndex) && expectWord.name.endsWith(actualWord.name.substring(0, 1)) &&
      getNextExpectWord(expectWordIndex).startsWith(actualWord.name.substring(1)) &&
      !getNextExpectWord(expectWordIndex).equals(actualWord.name.substring(1))
  }

  private def isTwoCharactersWord(actualWord: Word): Boolean = actualWord.length == 2

  private def isThreeCharactersWord(actualWord: Word): Boolean = actualWord.length == 3

  private def isFourCharactersWord(actualWord: Word): Boolean = actualWord.length == 4

  private def isActualWordComposedOfTwoExpectedWords(expectWord: Word, actualWord: Word, expectWordIndex: Int): Boolean = {
    isNotLastWord(expectWordIndex) && actualWord.name == (expectWord.name + getNextExpectWord(expectWordIndex))
  }

  private def isActualWordComposedOfThreeExpectedWords(expectWord: Word, actualWord: Word, expectWordIndex: Int): Boolean = {
    expectWordIndex + 2 < expect.length() && actualWord.name == (expectWord.name + getNextExpectWord(expectWordIndex) + getNextNextExpectWord(expectWordIndex))
  }

  private def isNotLastWord(expectWordIndex: Int): Boolean = expectWordIndex + 1 < expect.length()

  private def getNextExpectWord(index: Int): String = expect.getWord(index + 1)

  private def getNextNextExpectWord(index: Int): String = expect.getWord(index + 2)

  private def isActualWordStartsWithExpectedWordAndLastCharacterBelongsToNextWord(expectWord: Word, actualWord: Word, expectWordIndex: Int): Boolean = {
    isNotLastWord(expectWordIndex) && isActualWordContainsExpectedWord(expectWord, actualWord) && isLastCharacterBelongToNextWord(expectWord, actualWord) && getNextExpectWord(expectWordIndex).length > 1
  }

  private def isLastLabel(label: String): Boolean = {
    serialLabels.length > 0 && serialLabels.last._2 == label
  }

  private def isLastCharacterBelongToNextWord(expectWord: Word, actualWord: Word): Boolean = {
    actualWord.name.startsWith(expectWord.name) && actualWord.length == expectWord.length + 1
  }

  private def isActualWordContainsExpectedWord(expectWord: Word, actualWord: Word): Boolean = {
    actualWord.name.contains(expectWord.name)
  }

  private def isExpectWordContainsActualWord(expectWord: Word, actualWord: Word): Boolean = {
    expectWord.name.contains(actualWord.name)
  }

  private def add(word: String, label: String, pos: Int): String = {
    serialLabels = serialLabels :+ (word, label, pos)
    label
  }
}

/**
  * SB 一个词属于正确的词的开始 (Separated word Beginning part)
  * SM 一个词属于正确的词的中间 (Separated word Middle part)
  * SE 一个词属于正确的词的结束 (Separated word Ending part)
  * SH 一个双字词，应该分为两部分，并且将两个字分别加入前后两个词 (Split Half)
  * FL 一个词应该分为两部分，首字属于前词，其余单独成词 (First character belongs to Last word)
  * LC 一个词的最后一个字是下一个词的首字 (Last Character)
  * U  当前词由两个词组成，第一个词是单字 (Union words)
  * UT 当前词由三个字组成 (Union words composed of Three single character words)
  * A  默认标记
  */
object DisambiguationToSerialLabels {

  val LABEL_SB = "SB"

  val LABEL_SM = "SM"

  val LABEL_SE = "SE"

  val LABEL_SH = "SH"

  val LABEL_FL = "FL"

  val LABEL_LC = "LC"

  val LABEL_U = "U"

  val LABEL_UT = "UT"

  val LABEL_A = "A"

}