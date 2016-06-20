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
package org.sonar.plugins.scala.sensor;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.resources.Project;
import org.sonar.plugins.scala.language.Scala;
import org.sonar.plugins.scala.util.FileTestUtils;

public class AbstractScalaSensorTest {

  private AbstractScalaSensor abstractScalaSensor;
  private DefaultFileSystem fileSystem;
  

  @Before
  public void setUp() {
	fileSystem = new DefaultFileSystem();
    abstractScalaSensor = new AbstractScalaSensor(new Scala(), fileSystem) {

      public void analyse(Project project, SensorContext context) {
        // dummy implementation, never called in this test
      }
    };
  }

  @Test
  public void shouldExecuteOnScalaProjects() {
	Project scalaProject = mock(Project.class);
	FileTestUtils.addInputFiles(fileSystem, FileTestUtils.getInputFiles(
				"/scalaSourceImporter/", "MainFile", "scala", 1), false);
	assertTrue(abstractScalaSensor.shouldExecuteOnProject(scalaProject));
  }

  @Test
  public void shouldNotExecuteOnJavaProjects() {
 	Project javaProject = mock(Project.class);
 	FileTestUtils.addInputFiles(fileSystem, FileTestUtils.getInputFiles(
				"/scalaSourceImporter/", "JavaMainFile", "java", 1), false);
	assertFalse(abstractScalaSensor.shouldExecuteOnProject(javaProject));
  }

  @Test
  public void shouldHaveScalaAsLanguage() {
	assertThat(abstractScalaSensor.getScala(), equalTo(new Scala()));
  }
}