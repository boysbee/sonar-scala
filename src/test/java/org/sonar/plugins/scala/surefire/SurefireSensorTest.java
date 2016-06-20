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
package org.sonar.plugins.scala.surefire;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

public class SurefireSensorTest {

    private SurefireSensor sensor;
    private Settings settings;
    private DefaultFileSystem fileSystem;
    private Project project;
    
    @Before
    public void setUp() {
    	this.settings = new Settings();
    	this.fileSystem = new DefaultFileSystem();
    	sensor = new SurefireSensor(settings, fileSystem);
    	project = mock(Project.class);
    }


    @Test
    public void shouldDependOnCoverageSensors() {
        assertEquals(CoverageExtension.class, sensor.dependsUponCoverageSensors());
    }

    
    @Test
    public void should_execute_if_filesystem_contains_scala_files() {
	  DefaultInputFile javaFile = new DefaultInputFile("src/org/foo/scala");
	  javaFile.setLanguage("scala");
	  fileSystem.add(javaFile);
      SurefireSensor surefireSensor = new SurefireSensor(settings,fileSystem);
      Assertions.assertThat(surefireSensor.shouldExecuteOnProject(project)).isTrue();
    }

    @Test
    public void should_not_execute_if_filesystem_does_not_contains_scala_files() {
	  DefaultInputFile scalaFile = new DefaultInputFile("src/org/foo/java");
	  scalaFile.setLanguage("java");
	  fileSystem.add(scalaFile);
      SurefireSensor surefireSensor = new SurefireSensor(settings,fileSystem);
      Assertions.assertThat(surefireSensor.shouldExecuteOnProject(project)).isFalse();
    }
    
    @Test
    public void testToString() {
        assertEquals("Scala SurefireSensor", sensor.toString());
    }
}