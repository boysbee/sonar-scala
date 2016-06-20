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
/*
 * SonarQube Cobertura Plugin
 */
package org.sonar.plugins.scala.cobertura;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.test.IsMeasure;

public class CoberturaSensorTest {

  private Project project;
  private SensorContext context;
  private CoberturaSensor sensor;
  private PathResolver pathResolver;
  private Settings settings;
  private DefaultFileSystem fs;

  @Before
  public void setUp() {
	project = mock(Project.class);
    context = mock(SensorContext.class);
    fs = new DefaultFileSystem();
    pathResolver = mock(PathResolver.class);
    settings = new Settings();
    sensor = new CoberturaSensor(fs, pathResolver, settings);
  }

  @Test
  public void shouldNotFailIfReportNotSpecifiedOrNotFound() throws URISyntaxException {
    when(pathResolver.relativeFile(any(File.class), anyString()))
        .thenReturn(new File("notFound.xml"));

    Project project = mock(Project.class);

    settings.setProperty("sonar.cobertura.reportPath", "notFound.xml");
    sensor.analyse(project, context);


    File report = getCoverageReport();
    settings.setProperty("sonar.cobertura.reportPath", report.getParent());
    when(pathResolver.relativeFile(any(File.class), anyString()))
        .thenReturn(report.getParentFile().getParentFile());
    sensor.analyse(project, context);
  }

  @Test
  public void doNotCollectProjectCoverage() throws URISyntaxException {
    sensor.parseReport(getCoverageReport(), context);

    verify(context, never()).saveMeasure(eq(CoreMetrics.COVERAGE), anyDouble());
  }

  @Test
  public void doNotCollectProjectLineCoverage() throws URISyntaxException {
    sensor.parseReport(getCoverageReport(), context);

    verify(context, never()).saveMeasure(eq(CoreMetrics.LINE_COVERAGE), anyDouble());
    verify(context, never()).saveMeasure(argThat(new IsMeasure(CoreMetrics.COVERAGE_LINE_HITS_DATA)));
  }

  @Test
  public void doNotCollectProjectBranchCoverage() throws URISyntaxException {
    sensor.parseReport(getCoverageReport(), context);

    verify(context, never()).saveMeasure(eq(CoreMetrics.BRANCH_COVERAGE), anyDouble());
  }

  @Test
  public void collectPackageLineCoverage() throws URISyntaxException {
    sensor.parseReport(getCoverageReport(), context);
    verify(context, never()).saveMeasure(any(Resource.class), eq(CoreMetrics.LINE_COVERAGE), anyDouble());
    verify(context, never()).saveMeasure(any(Resource.class), eq(CoreMetrics.UNCOVERED_LINES), anyDouble());
  }

  @Test
  public void collectPackageBranchCoverage() throws URISyntaxException {
    sensor.parseReport(getCoverageReport(), context);

    verify(context, never()).saveMeasure(any(Resource.class), eq(CoreMetrics.BRANCH_COVERAGE), anyDouble());
    verify(context, never()).saveMeasure(any(Resource.class), eq(CoreMetrics.UNCOVERED_CONDITIONS), anyDouble());
  }

  @Test
  public void packageCoverageIsCalculatedLaterByDecorator() throws URISyntaxException {
    sensor.parseReport(getCoverageReport(), context);

    verify(context, never()).saveMeasure(any(Resource.class), eq(CoreMetrics.COVERAGE), anyDouble());
  }

  @Test
  public void collectFileLineCoverage() throws URISyntaxException {
	DefaultInputFile file = new DefaultInputFile("org/apache/commons/chain/config/ConfigParser.scala");
	fs.add(file); 
	sensor = new CoberturaSensor(fs, pathResolver, settings);
	sensor.parseReport(getCoverageReport(), context);
   
    verify(context).saveMeasure(eq(file), argThat(new IsMeasure(CoreMetrics.LINES_TO_COVER, 30.0)));
    verify(context).saveMeasure(eq(file), argThat(new IsMeasure(CoreMetrics.UNCOVERED_LINES, 5.0)));
  }

  @Test
  public void collectFileBranchCoverage() throws URISyntaxException {
	  DefaultInputFile file = new DefaultInputFile("org/apache/commons/chain/config/ConfigParser.scala");
	  fs.add(file); 
	  sensor = new CoberturaSensor(fs, pathResolver, settings);  
	  sensor.parseReport(getCoverageReport(), context);

    verify(context).saveMeasure(eq(file), argThat(new IsMeasure(CoreMetrics.CONDITIONS_TO_COVER, 6.0)));
    verify(context).saveMeasure(eq(file), argThat(new IsMeasure(CoreMetrics.UNCOVERED_CONDITIONS, 2.0)));
  }

  @Test
  public void testDoNotSaveMeasureOnResourceWhichDoesntExistInTheContext() throws URISyntaxException {
    when(context.getResource(any(Resource.class))).thenReturn(null);
    sensor.parseReport(getCoverageReport(), context);
    verify(context, never()).saveMeasure(any(Resource.class), any(Measure.class));
  }

  @Test
  public void collectFileLineHitsData() throws URISyntaxException {
	DefaultInputFile file = new DefaultInputFile("org/apache/commons/chain/impl/CatalogBase.scala");
	fs.add(file);
	sensor = new CoberturaSensor(fs, pathResolver, settings);
    sensor.parseReport(getCoverageReport(), context);
    verify(context).saveMeasure(
        eq(file),
        argThat(new IsMeasure(CoreMetrics.COVERAGE_LINE_HITS_DATA,
            "48=117;56=234;66=0;67=0;68=0;84=999;86=999;98=318;111=18;121=0;122=0;125=0;126=0;127=0;128=0;131=0;133=0")));
  }

  private File getCoverageReport() throws URISyntaxException {
	    return new File(getClass().getResource("/org/sonar/plugins/scala/cobertura/CoberturaSensorTest/commons-chain-coverage.xml").toURI());
	  }
  
  @Test
  public void should_execute_if_filesystem_contains_scala_files() {
	  DefaultInputFile scalaFile = new DefaultInputFile("src/org/foo/scala");
	  scalaFile.setLanguage("scala");
	  fs.add(scalaFile);
	  sensor = new CoberturaSensor(fs, pathResolver, settings);  
	  Assertions.assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void should_not_execute_if_filesystem_does_not_contains_scala_files() {
	  DefaultInputFile javaFile = new DefaultInputFile("src/org/foo/java");
	  javaFile.setLanguage("java");
	  fs.add(javaFile);
	  sensor = new CoberturaSensor(fs, pathResolver, settings);  
	  Assertions.assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }
  
}
