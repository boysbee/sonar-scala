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
package org.sonar.plugins.scala.cobertura;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.plugins.scala.language.Scala;

import java.io.File;

public class CoberturaSensor implements Sensor, CoverageExtension {

  private static final Logger LOG = LoggerFactory.getLogger(CoberturaSensor.class);

  public static final String SCALA_COBERTURA_REPORTS_PATH_PROPERTY ="sonar.scala.cobertura.reportPath";
  public static final String COBERTURA_REPORTS_PATH_PROPERTY ="sonar.cobertura.reportPath";
  private FileSystem fileSystem;;
  private PathResolver pathResolver;
  private Settings settings;


  public CoberturaSensor(FileSystem fileSystem, PathResolver pathResolver, Settings settings) {
    this.pathResolver = pathResolver;
    this.settings = settings;
    this.fileSystem = fileSystem;
  }

  public boolean shouldExecuteOnProject(Project project) {
	  if(fileSystem.languages().contains(Scala.KEY)){
        LOG.info("SonarScala - CoberturaSensor will be executed");                
        return true;
    } else {
        LOG.info("SonarScala - CoberturaSensor will NOT be executed"); 
        return false;
    }
  }

    public void analyse(Project project, SensorContext context) {
        String path = settings.getString(SCALA_COBERTURA_REPORTS_PATH_PROPERTY);
        if (path == null) {
            LOG.warn("Cobertura report path property \"sonar.scala.cobertura.reportPath\" not found!");
            path = settings.getString(COBERTURA_REPORTS_PATH_PROPERTY);
        }
        if (path == null) {
            LOG.warn("Cobertura report path property \"sonar.cobertura.reportPath\" not found!");
            return;
        }
        File report = pathResolver.relativeFile(fileSystem.baseDir(), path);
        if (!report.isFile()) {
            LOG.warn("Cobertura report not found at {}", report);
            return;
        }
        parseReport(report, context);
    }

  protected void parseReport(File xmlFile, SensorContext context) {
    LOG.info("parsing {}", xmlFile);
    ScalaCoberturaReportParser.parseReport(xmlFile, context, fileSystem);
  }

  @Override
  public String toString() {
	  return "Scala CoberturaSensor";
  }

}
