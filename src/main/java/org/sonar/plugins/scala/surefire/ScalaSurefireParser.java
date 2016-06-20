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
import java.io.FilenameFilter;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.StaxParser;

public class ScalaSurefireParser {

	private static final Logger LOG = LoggerFactory.getLogger(ScalaSurefireParser.class);
	private final FileSystem fileSystem;
	
	public ScalaSurefireParser(FileSystem fileSystem){
		this.fileSystem = fileSystem;
	}
	
  public void collect(Project project, SensorContext context, File reportsDir) {
    File[] xmlFiles = getReports(reportsDir);

    if (xmlFiles.length == 0) {
      // See http://jira.codehaus.org/browse/SONAR-2371
      if (project.getModules().isEmpty()) {
        context.saveMeasure(CoreMetrics.TESTS, 0.0);
      }
    } else {
      parseFiles(context, xmlFiles);
    }
  }

  private File[] getReports(File dir) {
	    if (dir == null) {
	      return new File[0];
	    } else if (!dir.isDirectory()) {
	      LOG.warn("Reports path not found: " + dir.getAbsolutePath());
	      return new File[0];
	    }
	    File[] unitTestResultFiles = findXMLFilesStartingWith(dir, "TEST-");
	    if (unitTestResultFiles.length == 0) {
	      // maybe there's only a test suite result file
	      unitTestResultFiles = findXMLFilesStartingWith(dir, "TESTS-");
	    }
	    if (unitTestResultFiles.length == 0) {
		  // maybe there's only a test suite result file
		  unitTestResultFiles = findXMLFilesStartingWith(dir, "");
		}
	    return unitTestResultFiles;
	  }

  private File[] findXMLFilesStartingWith(File dir, final String fileNameStart) {
    return dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(fileNameStart) && name.endsWith(".xml");
      }
    });
  }
  
  private void parseFiles(SensorContext context, File[] reports) {
    UnitTestIndex index = new UnitTestIndex();
    parseFiles(reports, index);
    save(index, context);
  }

  private void parseFiles(File[] reports, UnitTestIndex index) {
    SurefireStaxHandler staxParser = new SurefireStaxHandler(index);
    StaxParser parser = new StaxParser(staxParser, false);
    for (File report : reports) {
      try {
        parser.parse(report);
      } catch (XMLStreamException e) {
        throw new SonarException("Fail to parse the Surefire report: " + report, e);
      }
    }
  }

  private void save(UnitTestIndex index, SensorContext context) {
	    long negativeTimeTestNumber = 0;
	    for (Map.Entry<String, UnitTestClassReport> entry : index.getIndexByClassname().entrySet()) {
	      UnitTestClassReport report = entry.getValue();
	      if (report.getTests() > 0) {
	        negativeTimeTestNumber += report.getNegativeTimeTestNumber();
	        InputFile resource = getUnitTestResource(entry.getKey());
	        if (resource != null) {
	          save(report, resource, context);
	        } else {
	          LOG.warn("Resource not found: {}", entry.getKey());
	        }
	      }
	    }
	    if (negativeTimeTestNumber > 0) {
	      LOG.warn("There is {} test(s) reported with negative time by surefire, total duration may not be accurate.", negativeTimeTestNumber);
	    }
	  }
  
  private void save(UnitTestClassReport report, InputFile resource, SensorContext context) {
        double testsCount = report.getTests() - report.getSkipped();
        saveMeasure(context, resource, CoreMetrics.SKIPPED_TESTS, report.getSkipped());
        saveMeasure(context, resource, CoreMetrics.TESTS, testsCount);
        saveMeasure(context, resource, CoreMetrics.TEST_ERRORS, report.getErrors());
        saveMeasure(context, resource, CoreMetrics.TEST_FAILURES, report.getFailures());
        saveMeasure(context, resource, CoreMetrics.TEST_EXECUTION_TIME, report.getDurationMilliseconds());
        double passedTests = testsCount - report.getErrors() - report.getFailures();
        if (testsCount > 0) {
          double percentage = passedTests * 100d / testsCount;
          saveMeasure(context, resource, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage));
        }
        saveResults(context, resource, report);
      }

  private void saveMeasure(SensorContext context, InputFile resource, Metric metric, double value) {
    if (!Double.isNaN(value)) {
      context.saveMeasure(resource, metric, value);
    }
  }

  private void saveResults(SensorContext context, InputFile resource, UnitTestClassReport report) {
    context.saveMeasure(resource, new Measure(CoreMetrics.TEST_DATA, report.toXml()));
  }

  private InputFile getUnitTestResource(String classKey){
	  String filename = classKey.replace('.', '/') + ".scala";	  
	  FilePredicates filePredicates = fileSystem.predicates();
	
	  return fileSystem.inputFile(filePredicates. matchesPathPattern("**/*" + filename));
  }

}
