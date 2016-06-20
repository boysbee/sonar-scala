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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.test.MavenTestUtils;

public class SurefireUtilsTest {

   private DefaultFileSystem fileSystem;
   private Settings settings;
    
   @Before
   public void setUp() {
	   this.fileSystem = new DefaultFileSystem(); 
	   this.settings = mock(Settings.class);
   }

  @Test
  public void shouldGetReportsFromProperty() {
	fileSystem.setBaseDir(new File("src/test/resources/surefire"));
    when(settings.getString("sonar.junit.reportsPath")).thenReturn("targetdir/surefire-reports");
    assertThat(SurefireUtils.getReportsDirectory(fileSystem, settings, null).exists()).isTrue();
    assertThat(SurefireUtils.getReportsDirectory(fileSystem, settings, null).isDirectory()).isTrue();
  }

  @Ignore("caused test failures on release")
  @Test
  public void shouldGetReportsFromPluginConfiguration() {
    MavenProject pom = MavenTestUtils.loadPom(getClass(), "shouldGetReportsFromPluginConfiguration/pom.xml"); 
    assertThat(SurefireUtils.getReportsDirectory(fileSystem, settings, pom).exists()).isTrue();
    assertThat(SurefireUtils.getReportsDirectory(fileSystem, settings, pom).isDirectory()).isTrue();
  }

  @Ignore("caused test failures on release")
  @Test
  public void shouldGetReportsFromDefaultConfiguration() {
	fileSystem.setBaseDir(new File("src/test/resources/surefire"));
	File reportsDir = SurefireUtils.getReportsDirectory(fileSystem, settings, null);
    assertThat(reportsDir.getPath().endsWith("src/test/resources/surefire/target/surefire-reports")).isTrue();
    assertThat(reportsDir.getPath().endsWith("src/test/resources/surefire/target/foo")).isFalse();
  }
  
  

}
