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

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.plugins.scala.compiler.Lexer;
import org.sonar.plugins.scala.language.Comment;
import org.sonar.plugins.scala.language.Scala;
import org.sonar.plugins.scala.metrics.CommentsAnalyzer;
import org.sonar.plugins.scala.metrics.ComplexityCalculator;
import org.sonar.plugins.scala.metrics.FunctionCounter;
import org.sonar.plugins.scala.metrics.LinesAnalyzer;
import org.sonar.plugins.scala.metrics.PublicApiCounter;
import org.sonar.plugins.scala.metrics.StatementCounter;
import org.sonar.plugins.scala.metrics.TypeCounter;
import org.sonar.plugins.scala.util.MetricDistribution;
import org.sonar.plugins.scala.util.StringUtils;

/**
 * This is the main sensor of the Scala plugin. It gathers all results
 * of the computation of base metrics for all Scala resources.
 *
 * @author Felix Müller
 * @since 0.1
 */
public class BaseMetricsSensor extends AbstractScalaSensor {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseMetricsSensor.class);

  public BaseMetricsSensor(Scala scala, FileSystem fileSystem) {
	  super(scala, fileSystem);
  }

  public void analyse(Project project, SensorContext sensorContext) {
	final String charset = fileSystem.encoding().toString();   

    MetricDistribution complexityOfFunctions = null;    
    FilePredicates filePredicates = fileSystem.predicates();

    for (InputFile inputFile : fileSystem.inputFiles(filePredicates.and(filePredicates.hasLanguage(Scala.KEY), filePredicates.hasType(InputFile.Type.MAIN)))) {
     
      sensorContext.saveMeasure(inputFile, CoreMetrics.FILES, 1.0);

      try {
        final String source = FileUtils.readFileToString(inputFile.file(), charset);
        final List<String> lines = StringUtils.convertStringToListOfLines(source);
        final List<Comment> comments = new Lexer().getComments(source);

        final CommentsAnalyzer commentsAnalyzer = new CommentsAnalyzer(comments);
        final LinesAnalyzer linesAnalyzer = new LinesAnalyzer(lines, commentsAnalyzer);

        addLineMetrics(sensorContext, inputFile, linesAnalyzer);
        addCommentMetrics(sensorContext, inputFile, commentsAnalyzer);
        addCodeMetrics(sensorContext, inputFile, source);
        addPublicApiMetrics(sensorContext, inputFile, source);

        complexityOfFunctions = sumUpMetricDistributions(complexityOfFunctions,
            ComplexityCalculator.measureComplexityOfFunctions(source));

      } catch (IOException ioe) {
        LOGGER.error("Could not read the file: " + inputFile.absolutePath(), ioe);
      }
    }

    if (complexityOfFunctions != null)
      sensorContext.saveMeasure(complexityOfFunctions.getMeasure());

  }

  private void addLineMetrics(SensorContext sensorContext, InputFile scalaFile, LinesAnalyzer linesAnalyzer) {
    sensorContext.saveMeasure(scalaFile, CoreMetrics.LINES, (double) linesAnalyzer.countLines());
    sensorContext.saveMeasure(scalaFile, CoreMetrics.NCLOC, (double) linesAnalyzer.countLinesOfCode());
  }

  private void addCommentMetrics(SensorContext sensorContext, InputFile scalaFile,
      CommentsAnalyzer commentsAnalyzer) {
    sensorContext.saveMeasure(scalaFile, CoreMetrics.COMMENT_LINES,
        (double) commentsAnalyzer.countCommentLines());
  }

  private void addCodeMetrics(SensorContext sensorContext, InputFile scalaFile, String source) {
    sensorContext.saveMeasure(scalaFile, CoreMetrics.CLASSES,
        (double) TypeCounter.countTypes(source));
    sensorContext.saveMeasure(scalaFile, CoreMetrics.STATEMENTS,
        (double) StatementCounter.countStatements(source));
    sensorContext.saveMeasure(scalaFile, CoreMetrics.FUNCTIONS,
        (double) FunctionCounter.countFunctions(source));
    sensorContext.saveMeasure(scalaFile, CoreMetrics.COMPLEXITY,
        (double) ComplexityCalculator.measureComplexity(source));
  }

  private void addPublicApiMetrics(SensorContext sensorContext, InputFile scalaFile, String source) {
    sensorContext.saveMeasure(scalaFile, CoreMetrics.PUBLIC_API,
        (double) PublicApiCounter.countPublicApi(source));
    sensorContext.saveMeasure(scalaFile, CoreMetrics.PUBLIC_UNDOCUMENTED_API,
        (double) PublicApiCounter.countUndocumentedPublicApi(source));
  }

  private MetricDistribution sumUpMetricDistributions(MetricDistribution oldDistribution,
      MetricDistribution newDistribution) {
    if (oldDistribution == null) {
      return newDistribution;
    }

    oldDistribution.add(newDistribution);
    return oldDistribution;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}