package assembler

import org.scalatest.Matchers
import org.scalatest._

class TranslateSpec extends FlatSpec with Matchers {

  def translate(code: String): Iterator[String] = {
    val a = new Assembler(code)
    val asms0 = a.parse(code)
    val asms1 = a.translate2DummyACmd(asms0)
    var table = a.makeSymbolTable(asms1)

    for ((key, value) <- List(("MAX.ADDR", 32768), ("NEXT.MAX.ADDR", 32769))) {
      table = table.updated(key, value)
    }

    val asms2 = a.resolveDummyACmd(16, asms1, table)
    val asms3 = a.removeLabel(asms2)
    val (insts, info) = a.codeGen(asms3, table)
    insts.iterator
  }

  behavior of "translate: ACmd"
  it should "translate @1234" in {
    val code =
      """@1234
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("0" + "000010011010010")
  }

  it should "translate @32767" in {
    val code =
      """@32767
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("0" + "111111111111111")
  }

  it should "translate @32768" in {
    val code =
      """@MAX.ADDR
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("0" + "111111111111111")
    insts.next() shouldBe ("111" + "0110001" + "100" + "000")
  }

  it should "translate @32769" in {
    val code =
      """@NEXT.MAX.ADDR
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("0" + "111111111111110")
    insts.next() shouldBe ("111" + "0110001" + "100" + "000")
  }

  behavior of "translate: CCmd"
  it should "translate M=0" in {
    val code =
      """M=0
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0101010" + "001" + "000")
  }
  it should "translate M=1" in {
    val code =
      """M=1
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0111111" + "001" + "000")
  }
  it should "translate M=-1" in {
    val code =
      """M=-1
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0111010" + "001" + "000")
  }
  it should "translate M=A" in {
    val code =
      """M=A
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0110000" + "001" + "000")
  }
  it should "translate A=M" in {
    val code =
      """A=M
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1110000" + "100" + "000")
  }
  it should "translate A=D" in {
    val code =
      """A=D
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0001100" + "100" + "000")
  }
  it should "translate A=!D" in {
    val code =
      """A=!D
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0001101" + "100" + "000")
  }
  it should "translate A=!M" in {
    val code =
      """A=!M
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1110001" + "100" + "000")
  }

  it should "translate D=!A" in {
    val code =
      """D=!A
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0110001" + "010" + "000")
  }
  it should "translate D=-D" in {
    val code =
      """D=-D
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0001111" + "010" + "000")
  }
  it should "translate D=-A" in {
    val code =
      """D=-A
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0110011" + "010" + "000")
  }
  it should "translate D=-M" in {
    val code =
      """D=-M
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1110011" + "010" + "000")
  }
  it should "translate MD=D+1" in {
    val code =
      """MD=D+1
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0011111" + "011" + "000")
  }
  it should "translate MD=A+1" in {
    val code =
      """MD=A+1
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0110111" + "011" + "000")
  }
  it should "translate MD=M+1" in {
    val code =
      """MD=M+1
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1110111" + "011" + "000")
  }
  it should "translate MD=D-1" in {
    val code =
      """MD=D-1
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0001110" + "011" + "000")
  }
  it should "translate AD=M-1" in {
    val code =
      """AD=M-1
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1110010" + "110" + "000")
  }
  it should "translate AD=D+A" in {
    val code =
      """AD=D+A
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0000010" + "110" + "000")
  }
  it should "translate AD=D+M" in {
    val code =
      """AD=D+M
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1000010" + "110" + "000")
  }
  it should "translate AM=D-M" in {
    val code =
      """AM=D-M
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1010011" + "101" + "000")
  }
  it should "translate AM=A-D" in {
    val code =
      """AM=A-D
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0000111" + "101" + "000")
  }
  it should "translate AM=M-D" in {
    val code =
      """AM=M-D
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1000111" + "101" + "000")
  }
  it should "translate AMD=D&M" in {
    val code =
      """AMD=D&M
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1000000" + "111" + "000")
  }
  it should "translate AMD=M&A" in {
    val code =
      """AMD=D&A
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0000000" + "111" + "000")
  }
  it should "translate AMD=D|M" in {
    val code =
      """AMD=D|M
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1010101" + "111" + "000")
  }
  it should "translate AMD=D|A" in {
    val code =
      """AMD=D|A
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0010101" + "111" + "000")
  }
  it should "translate 0;JGT" in {
    val code =
      """0;JGT
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0101010" + "000" + "001")
  }
  it should "translate 1;JGT" in {
    val code =
      """1;JGT
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0111111" + "000" + "001")
  }
  it should "translate -1;JGT" in {
    val code =
      """-1;JGT
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0111010" + "000" + "001")
  }
  it should "translate A;JGT" in {
    val code =
      """A;JGT
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0110000" + "000" + "001")
  }
  it should "translate M;JEQ" in {
    val code =
      """M;JEQ
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1110000" + "000" + "010")
  }
  it should "translate D;JEQ" in {
    val code =
      """D;JEQ
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0001100" + "000" + "010")
  }
  it should "translate !D;JEQ" in {
    val code =
      """!D;JEQ
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0001101" + "000" + "010")
  }
  it should "translate !A;JEQ" in {
    val code =
      """!A;JEQ
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0110001" + "000" + "010")
  }
  it should "translate !M;JGE" in {
    val code =
      """!M;JGE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1110001" + "000" + "011")
  }
  it should "translate -D;JGE" in {
    val code =
      """-D;JGE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0001111" + "000" + "011")
  }
  it should "translate -M;JGE" in {
    val code =
      """-M;JGE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1110011" + "000" + "011")
  }
  it should "translate -A;JGE" in {
    val code =
      """-A;JGE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0110011" + "000" + "011")
  }
  it should "translate D+1;JLT" in {
    val code =
      """D+1;JLT
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0011111" + "000" + "100")
  }
  it should "translate M+1;JLT" in {
    val code =
      """M+1;JLT
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1110111" + "000" + "100")
  }
  it should "translate A+1;JLT" in {
    val code =
      """A+1;JLT
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0110111" + "000" + "100")
  }
  it should "translate M-1;JNE" in {
    val code =
      """M-1;JNE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1110010" + "000" + "101")
  }
  it should "translate A-1;JNE" in {
    val code =
      """A-1;JNE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0110010" + "000" + "101")
  }
  it should "translate D+A;JNE" in {
    val code =
      """D+A;JNE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0000010" + "000" + "101")
  }
  it should "translate D+M;JNE" in {
    val code =
      """D+M;JNE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1000010" + "000" + "101")
  }
  it should "translate D-A;JLE" in {
    val code =
      """D-A;JLE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0010011" + "000" + "110")
  }
  it should "translate D-M;JLE" in {
    val code =
      """D-M;JLE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1010011" + "000" + "110")
  }
  it should "translate A-D;JLE" in {
    val code =
      """A-D;JLE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0000111" + "000" + "110")
  }
  it should "translate M-D;JLE" in {
    val code =
      """M-D;JLE
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1000111" + "000" + "110")
  }
  it should "translate D&A;JMP" in {
    val code =
      """D&A;JMP
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0000000" + "000" + "111")
  }
  it should "translate D&M;JMP" in {
    val code =
      """D&M;JMP
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1000000" + "000" + "111")
  }
  it should "translate D|A;JMP" in {
    val code =
      """D|A;JMP
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "0010101" + "000" + "111")
  }
  it should "translate D|M;JMP" in {
    val code =
      """D|M;JMP
      |""".stripMargin

    val insts = translate(code)

    insts.next() shouldBe ("111" + "1010101" + "000" + "111")
  }
}
