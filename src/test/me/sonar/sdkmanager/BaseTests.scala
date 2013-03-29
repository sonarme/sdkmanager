package me.sonar.sdkmanager

import com.sonar.dossier.testing.ReflectionDiff
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{GivenWhenThen, FlatSpec}

/**
 * Default base class for unit tests
 */
trait SimpleTest extends FlatSpec with GivenWhenThen with ReflectionDiff with ShouldMatchers
