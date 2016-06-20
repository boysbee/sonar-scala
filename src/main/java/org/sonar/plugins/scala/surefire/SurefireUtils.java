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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.config.Settings;

//import javax.annotation.CheckForNull;

/**
 * @since 2.4
 */
public final class SurefireUtils {

  public static final String SUREFIRE_REPORTS_PATH_PROPERTY = "sonar.junit.reportsPath";
  private static final Logger LOG = LoggerFactory.getLogger(SurefireUtils.class);

  private SurefireUtils() {
		// to prevent instantiation
  }


    public static File getReportsDirectory(Settings settings, FileSystem fs, PathResolver pathResolver) {
        File dir = getReportsDirectoryFromProperty(settings, fs, pathResolver);
        if (dir == null) {
            dir = new File(fs.baseDir(), "target/surefire-reports");
        }
        return dir;
    }

    //@CheckForNull
    private static File getReportsDirectoryFromProperty(Settings settings, FileSystem fs, PathResolver pathResolver) {
        if(settings.hasKey(SUREFIRE_REPORTS_PATH_PROPERTY)) {
            String path = settings.getString(SUREFIRE_REPORTS_PATH_PROPERTY);
            if (path != null) {
                try {
                    return pathResolver.relativeFile(fs.baseDir(), path);
                } catch (Exception e) {
                    LOG.info("Surefire report path: "+fs.baseDir()+"/"+path +" not found.");
                }
            }
        }
        return null;
    }
  /*
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
  */

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