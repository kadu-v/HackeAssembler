package assembler

import assembler.Util._
import java.io.PrintWriter

class CodeWriter(asms: Vector[Asm], symbolTable: Map[String, Int]) {
  def codeGen(): Vector[String] = {
    asms
      .map(asm => translate(asm))
  }

  def translate(asm: Asm): String = {
    asm.ty match {
      case ACmd(addr)             => binaryGenACmd(addr, asm.loc)
      case ACmdLabel(label)       => binaryGenACmd(label, asm.loc)
      case CCmd(dest, comp, jump) => binaryGenCCmd(dest, comp, jump, asm.loc)
      case Label(label)           => throwCodeGenError("label", label, asm.loc)
      case tt                     => throwCodeGenError("xx", "", asm.loc)
    }
  }

  def binaryGenACmd(addr: TokenType, loc: Loc): String = {
    addr match {
      case Number(n) =>
        String.format("%16s", Integer.toBinaryString(n)).replace(' ', '0')
      case Symbol(s) => {
        symbolTable.get(s) match {
          case None =>
            throwCodeGenError(
              s"to find a label (${s}) in symboltable",
              addr.toString(),
              loc
            )
          case Some(value) =>
            String
              .format("%16s", Integer.toBinaryString(value))
              .replace(' ', '0')
        }
      }
      case tt => throwCodeGenError("ACmd", tt.toString(), loc)
    }
  }

  def binaryGenCCmd(
      dest: TokenType,
      comp: TokenType,
      jump: TokenType,
      loc: Loc
  ): String = {
    val binDest = binaryGenDest(dest, loc)
    val binComp = binaryGenComp(comp, loc)
    val binJump = binaryGenJump(jump, loc)

    "111" + binComp + binDest + binJump
  }

  def binaryGenDest(dest: TokenType, loc: Loc): String = {
    dest match {
      case Null => "000"
      case M    => "001"
      case D    => "010"
      case MD   => "011"
      case A    => "100"
      case AM   => "101"
      case AD   => "110"
      case AMD  => "111"
      case tt =>
        throwCodeGenError(
          "null, M, D, MD, A, AM, AD or AMD",
          tt.toString(),
          loc
        )
    }
  }

  def binaryGenComp(comp: TokenType, loc: Loc): String = {
    comp match {
      case Number(0) => "0101010"
      case Number(1) => "0111111"
      case Minus     => "0111010"
      case D         => "0001100"
      case A         => "0110000"
      case BangD     => "0001101"
      case BangA     => "0110001"
      case MinusD    => "0001111"
      case MinusA    => "0110011"
      case DPlusOne  => "0011111"
      case APlusOne  => "0110111"
      case DMinusOne => "0001110"
      case AMinusOne => "0110010"
      case DPlusA    => "0000010"
      case DMinusA   => "0010011"
      case AMinusD   => "0000111"
      case DAndA     => "0000000"
      case DOrA      => "0010101"
      case M         => "1110000"
      case BangM     => "1110001"
      case MinusM    => "1110011"
      case MPlusOne  => "1110111"
      case MMinusOne => "1110010"
      case DPlusM    => "1000010"
      case DMinusM   => "1010011"
      case MMinusD   => "1000111"
      case DAndM     => "1000000"
      case DOrM      => "1010101"
      case tt =>
        throwCodeGenError(
          "0, 1, -1, D, A, !D, !A, -D, -A, D+1, A+1, D-1, D+A, D-A, A-D, D&A, D|A, M, !M, -M, M+1, M-1, D+M, D-M, M-D, D&M, D|M",
          tt.toString(),
          loc
        )
    }
  }

  def binaryGenJump(jump: TokenType, loc: Loc): String = {
    jump match {
      case Null => "000"
      case Jgt  => "001"
      case Jeq  => "010"
      case Jge  => "011"
      case Jlt  => "100"
      case Jne  => "101"
      case Jle  => "110"
      case Jmp  => "111"
      case tt =>
        throwCodeGenError(
          "null, JGT, JEQ, JGE, JLT, JNE, JLE, JMP",
          tt.toString(),
          loc
        )
    }
  }
}
