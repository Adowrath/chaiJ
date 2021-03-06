package chaij
package junit

import org.scalatest._

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.Description.{ createTestDescription => testDesc, EMPTY => EMPTY_DESC }
import org.junit.runners.model.Statement

import java.lang.annotation.{ Annotation => JAnnot }

import chaij.ChaiJ.expect
import chaij.ExceptionReporter.MultipleException
import chaij.UnmetExpectationException

class MultipleExpectationsTests extends FlatSpec with Matchers {

  val allRule:  TestRule = MultipleExpectations.all()
  val noneRule: TestRule = MultipleExpectations.none()

  @MultipleExpectation def multiple = getClass.getMethod("multiple").getAnnotation(classOf[MultipleExpectation])
  @SingleExpectation   def single   = getClass.getMethod("single"  ).getAnnotation(classOf[SingleExpectation]  )

  private def run(rule: TestRule, description: Description = EMPTY_DESC)(code: => Unit) = {
    rule.apply(new Statement() {
      override def evaluate(): Unit = code
    }, description).evaluate()
  }

  "MultipleExpectation.all" should "have a correct toString" in {
    allRule.toString should equal ("MultipleExpectations(enabledByDefault=true)")
  }

  it should "accept a single truth" in {
    run(allRule) {
      expect(true).to.be.ok()
    }
  }

  it should "report a single falsehood" in {
    the [UnmetExpectationException] thrownBy {
      run(allRule) {
        expect(false).to.be.ok()
      }
    } should have message "Expected a ok-ish boolean."
  }

  it should "report multiple falsehoods" in {
    val caught = the [MultipleException] thrownBy {
      run(allRule) {
        expect(false).to.be.ok()
        expect(2).to.be.above(3)
      }
    }

    caught.getMessage.lines.toList should === (
      "There were 2 errors:" ::
      " - chaij.UnmetExpectationException(Expected a ok-ish boolean.)" ::
      " - chaij.UnmetExpectationException(Expected 2 to be above 3.)" ::
      Nil
    )
  }

  it should "correctly recognize the 'single' annotation" in {
    the [UnmetExpectationException] thrownBy {
      run(allRule, testDesc(getClass, "exceptionForSingle", single)) {
        expect(false).to.be.ok()
        expect(2).to.be.above(3)
      }
    } should have message "Expected a ok-ish boolean."
  }

  it should "correctly ignore the 'multiple' annotation" in {
    val caught = the [MultipleException] thrownBy {
      run(allRule, testDesc(getClass, "noExceptionForMultiple", multiple)) {
        expect(false).to.be.ok()
        expect(2).to.be.above(3)
      }
    }

    caught.getMessage.lines.toList should === (
      "There were 2 errors:" ::
      " - chaij.UnmetExpectationException(Expected a ok-ish boolean.)" ::
      " - chaij.UnmetExpectationException(Expected 2 to be above 3.)" ::
      Nil
    )
  }

  "MultipleExpectation.none" should "have a correct toString" in {
    noneRule.toString should equal ("MultipleExpectations(enabledByDefault=false)")
  }

  it should "accept a single truth" in {
    run(noneRule) {
      expect(true).to.be.ok()
    }
  }

  it should "report a single falsehood" in {
    the [UnmetExpectationException] thrownBy {
      run(noneRule) {
        expect(false).to.be.ok()
      }
    } should have message "Expected a ok-ish boolean."
  }

  it should "not report multiple falsehoods" in {
    the [UnmetExpectationException] thrownBy {
      run(noneRule) {
        expect(false).to.be.ok()
        expect(2).to.be.above(3)
      }
    } should have message "Expected a ok-ish boolean."
  }

  it should "correctly recognize the 'multiple' annotation" in {
    val caught = the [MultipleException] thrownBy {
      run(noneRule, testDesc(getClass, "exceptionForMultiple", multiple)) {
        expect(false).to.be.ok()
        expect(2).to.be.above(3)
      }
    }

    caught.getMessage.lines.toList should === (
      "There were 2 errors:" ::
      " - chaij.UnmetExpectationException(Expected a ok-ish boolean.)" ::
      " - chaij.UnmetExpectationException(Expected 2 to be above 3.)" ::
      Nil
    )
  }

  it should "correctly ignore the 'single' annotation" in {
    the [UnmetExpectationException] thrownBy {
      run(noneRule, testDesc(getClass, "noExceptionForSingle", single)) {
        expect(false).to.be.ok()
        expect(2).to.be.above(3)
      }
    } should have message "Expected a ok-ish boolean."
  }
}
