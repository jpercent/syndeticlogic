package syndeticlogic.stopwatch

import scala.util.parsing.combinator._
import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;
import java.lang.RuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

sealed abstract class SpecialTimeValue

case class StringTimeValue(minutes: Int, seconds: Int, hundreths: Int) extends SpecialTimeValue {
   require(minutes <= 59, minutes >= 0)
   require (seconds <= 59, seconds >= 0)
   require(hundreths <= 99, hundreths >= 0)
   val time = IntegerTimeValue(minutesToHundredths(minutes) + secondsToHundreths(seconds) + hundreths)
   
   def minutesToHundredths(minutes: Int): Int = {
     minutes * 60 * 100
   }
   
   def secondsToHundreths(seconds: Int): Int = {
     seconds * 100
   }
   
   def toStringVersion(): String = {
      minutes.toString+":"+seconds.toString+":"+hundreths.toString
   }
}

case class IntegerTimeValue(time: Int) extends SpecialTimeValue {
  val timeValue = time
  
  def stringTimeValue(): StringTimeValue = {
    val minutes: java.lang.Long = TimeUnit.MILLISECONDS.convert(timeValue*10, TimeUnit.MINUTES);
    val minutesMillis: java.lang.Long = TimeUnit.MINUTES.convert(minutes, TimeUnit.MILLISECONDS);
    
    val seconds: java.lang.Long = TimeUnit.MILLISECONDS.convert(timeValue*10 - minutesMillis, TimeUnit.SECONDS);
    val secondsMillis: java.lang.Long = TimeUnit.SECONDS.convert(seconds, TimeUnit.MILLISECONDS);
    
    val hundreths = ((timeValue*10 - minutesMillis) - secondsMillis)/10
    require(minutes >= 0, minutes < 60)
    require(seconds >= 0, seconds < 60)
    require(hundreths >= 0, hundreths < 99)
    StringTimeValue(minutes.toInt, seconds.toInt, hundreths.toInt)
  }  
}

object AverageTimes {
  def average(times List[IntegerTimeValue]): StringTimeValue {
    times
  }
}

trait StringTime extends JavaTokenParsers {
  def timeValue: Parser[StringTimeValue] = stringLiteral~":"~stringLiteral~":"~stringLiteral ^^ 
      {case minutes~":"~seconds~":"~hundreths => StringTimeValue(Integer.getInteger(minutes), 
          Integer.getInteger(seconds), Integer.getInteger(hundreths))}  
}

class StringTimeParser extends JavaTokenParsers with StringTime {
  def stringTimeStatement: Parser[StringTimeValue] = timeValue
  def entryPoint: Parser[List[StringTimeValue]] = rep(stringTimeStatement) ^^ (List() ++ _)  
}

object ParseStringTime extends StringTimeParser {
  val logger: Log = LogFactory.getLog(this.getClass);
  
  def parse(s: String): List[StringTimeValue] = {
    val reader = new StringReader(s)
    parse(reader)
  }
  
  def parse(r: Reader): List[StringTimeValue] = {
    val tree: ParseResult[List[StringTimeValue]] = parseAll(entryPoint, r)
    logger.debug(tree)
    try { tree.get 
    } catch { 
      case e: RuntimeException => {
       logger.error(tree)
       throw e
      }
    }
  }
  def main(args: scala.Array[String]): Unit = {
    val reader = new FileReader(args(0))
    println(parse(reader))
    println("I LOVE SCALA")
  }  
}
