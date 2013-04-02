package me.sonar.sdkmanager

import com.sonar.dossier.testing._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Suite, BeforeAndAfterAll, GivenWhenThen, FlatSpec}
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.support.GenericXmlContextLoader

/**
 * Default base class for unit tests
 */
trait SimpleTest extends FlatSpec with GivenWhenThen with ReflectionDiff with ShouldMatchers

/**
 * Default base class for component tests
 */
@ContextConfiguration(
    locations = Array("classpath:spring/root-context.xml", "classpath:spring/test-context.xml"),
    loader = classOf[GenericXmlContextLoader])
abstract class SpringComponentTest extends SimpleTest with SpringTesting

trait SpringTesting extends BeforeAndAfterAll {
    this: Suite =>
    override protected def beforeAll() {
        // spring injection
        new TestContextManager(getClass).prepareTestInstance(this)
    }
}
