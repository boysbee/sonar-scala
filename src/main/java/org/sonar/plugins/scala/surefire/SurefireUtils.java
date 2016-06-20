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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.scala.surefire;

import java.io.File;
import java.io.IOException;

import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.maven.MavenPlugin;
import org.sonar.api.batch.maven.MavenSurefireUtils;
import org.sonar.api.config.Settings;

/**
 * @since 2.4
 */
public final class SurefireUtils {

  public static final String SUREFIRE_REPORTS_PATH_PROPERTY = "sonar.junit.reportsPath";
  private static final Logger LOG = LoggerFactory.getLogger(SurefireUtils.class);

  private SurefireUtils() {
		// to prevent instantiation
  }

  public static File getReportsDirectory(FileSystem fileSystem, Settings settings, MavenProject pom) {
    File dir = getReportsDirectoryFromProperty(fileSystem, settings);
    if (dir == null) {
      dir = getReportsDirectoryFromPluginConfiguration(pom);
    }
    if (dir == null) {
      dir = getReportsDirectoryFromDefaultConfiguration(fileSystem);
    }
    return dir;
  }

  private static File getReportsDirectoryFromProperty(FileSystem fileSystem, Settings settings) {
    String path = settings.getString(SUREFIRE_REPORTS_PATH_PROPERTY);
    if (path != null) {
    	File reportsDir = null;
		try {
			File canonicalBase = fileSystem.baseDir().getCanonicalFile();
			reportsDir = new File(canonicalBase, path);
		} catch (IOException e) {
			LOG.warn("Reports path could not be created", e);
		}
      return reportsDir;
    }
    return null;
  }

  private static File getReportsDirectoryFromPluginConfiguration(MavenProject pom) {
    MavenPlugin plugin = MavenPlugin.getPlugin(pom, MavenSurefireUtils.GROUP_ID, MavenSurefireUtils.ARTIFACT_ID);
    if (plugin != null) {
      String path = plugin.getParameter("reportsDirectory");
      if (path != null) {
        return new File(path);
      }
    }
    return null;
  }

  private static File getReportsDirectoryFromDefaultConfiguration(FileSystem fileSystem) {
	  File reportsDir = null;
	  try {
		File canonicalBase = fileSystem.baseDir().getCanonicalFile();
		reportsDir = new File(canonicalBase, "target/surefire-reports");
	} catch (IOException e) {
		LOG.warn("Reports path could not be created", e);
	}
	  return reportsDir;
  }
}