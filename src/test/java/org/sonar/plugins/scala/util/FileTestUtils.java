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
package org.sonar.plugins.scala.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;

public final class FileTestUtils {

  public static final InputFile SCALA_SOURCE_FILE = new DummyScalaFile(false);
  public static final InputFile SCALA_TEST_FILE = new DummyScalaFile(true);

  private static final Logger LOGGER = LoggerFactory.getLogger(FileTestUtils.class);

  private FileTestUtils() {
    // to prevent instantiation
  }

  public static String getRelativePath(String path) {
    return FileTestUtils.class.getResource(path).getFile();
  }
  
  public static List<InputFile> getInputFiles(String path, String fileNameBase, int numberOfFiles) {
    return getInputFiles(path, fileNameBase, "scala", numberOfFiles);
  }

  public static List<InputFile> getInputFiles(String path, String fileNameBase, String fileSuffix, int numberOfFiles) {
    List<InputFile> mainFiles = new ArrayList<InputFile>();

    URL resourceURL = FileTestUtils.class.getResource(path + fileNameBase + "1." + fileSuffix);
    for (int i = 1; resourceURL != null && i <= numberOfFiles; ) {
      String relativePath = path + fileNameBase + i + "." + fileSuffix;
  
      if (relativePath.charAt(0) == '/') {
            relativePath = relativePath.substring(1);
      }
     
      DefaultInputFile inputFile = new DefaultInputFile(relativePath);
      inputFile.setLanguage(fileSuffix);
      inputFile.setAbsolutePath(resourceURL.getPath());
      
      mainFiles.add(inputFile);
      resourceURL = FileTestUtils.class.getResource(path + fileNameBase + (++i) + "." + fileSuffix);
    }

    return mainFiles;
  }
  
  public static void addInputFiles(FileSystem fileSystem, Iterable<InputFile> inputFiles, boolean testFile) {
    for (InputFile inputFile : inputFiles) {
    	((DefaultInputFile) inputFile).setType(testFile ? InputFile.Type.TEST : InputFile.Type.MAIN);
    	((DefaultFileSystem) fileSystem).add(inputFile);
    }
  }
  
  public static List<String> getContentOfFiles(String path, String fileNameBase, int numberOfFiles) throws IOException {
    List<String> contentOfFiles = new ArrayList<String>();

    URL resourceURL = FileTestUtils.class.getResource(path + fileNameBase + "1.scala");
    for (int i = 1; resourceURL != null && i <= numberOfFiles; ) {
      try {
        contentOfFiles.add(FileUtils.readFileToString(new File(resourceURL.getFile()), Charset.defaultCharset().toString()));
      } catch (IOException ioe) {
        LOGGER.error("Unexpected I/O exception occurred", ioe);
        throw ioe;
      }
      resourceURL = FileTestUtils.class.getResource(path + fileNameBase + (++i) + ".scala");
    }

    return contentOfFiles;
  }
}
