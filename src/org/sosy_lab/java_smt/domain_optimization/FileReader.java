/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2016  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sosy_lab.java_smt.domain_optimization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverException;

final class FileReader {


  private FileReader() {
    throw new AssertionError("Instantiating utility class.");
  }

  public static String parseHeader(String path) throws FileNotFoundException {
    String header = "";
    try (Scanner scanner = new Scanner(new File(path), Charset.defaultCharset().name())) {
      while (scanner.hasNextLine()) {
        String s = scanner.nextLine();
        if (s.contains("(declare-fun")) {
          header = header.concat(s);
        }
      }
    }
    return header;
  }

  public static List<String> parseAsserts(String path) throws FileNotFoundException {
    List<String> asserts = new ArrayList<>();
    String toAppend = "( assert";
    try (Scanner scanner = new Scanner(new File(path), Charset.defaultCharset().name())) {
      scanner.useDelimiter("assert");
      while (scanner.hasNext()) {
        String toAssert = scanner.next();
        asserts.add(toAssert);
      }
    }
    asserts.remove(0);
    List<String> processedAsserts = new ArrayList<>();
    for (String s : asserts) {
      String newString = toAppend.concat(s);
      String finishedString = newString.substring(0, newString.length() - 1);
      processedAsserts.add(finishedString);
    }
    return processedAsserts;
  }

  public static void main(String[] args)
      throws InvalidConfigurationException, InterruptedException, IOException,
             SolverException {
    String filePath = System.getProperty("user.dir") + File.separator + "benchmark_1.smt2";
    String header = parseHeader(filePath);
    List<String> asserts = parseAsserts(filePath);

    Configuration config = Configuration.builder().setOption("useDomainOptimizer", "true").build();
    LogManager logger = BasicLogManager.create(config);
    ShutdownManager shutdown = ShutdownManager.create();
    try (BufferedWriter writer =
             Files.newBufferedWriter(Paths.get("output.txt"), Charset.defaultCharset())) {
      try (DomainOptimizerSolverContext delegate =
               (DomainOptimizerSolverContext) SolverContextFactory.createSolverContext(
                   config, logger, shutdown.getNotifier(), Solvers.SMTINTERPOL)) {
        FormulaManager fmgr = delegate.getFormulaManager();
        try (DomainOptimizerProverEnvironment env = new DomainOptimizerProverEnvironment(
            delegate)) {
          long startTime = System.nanoTime();
          for (String toAssert : asserts) {
            BooleanFormula constraint = fmgr.parse(header + toAssert);
            env.addConstraint(constraint);
          }
          long endTime = System.nanoTime();
          writer.write("isUnsat with DomainOptimizer: " + env.isUnsat());
          writer.write("Execution-time: " + (endTime - startTime));
        }
       try (ProverEnvironment basicEnv = delegate.newProverEnvironment()) {
          long startTime = System.nanoTime();
          for (String toAssert : asserts) {
            BooleanFormula constraint = fmgr.parse(header + toAssert);
            basicEnv.addConstraint(constraint);
          }
          long endTime = System.nanoTime();
          writer.write("is Unsat without DomainOptimizer: " + basicEnv.isUnsat());
          writer.write("Execution-time: " + (endTime - startTime));
        }
      }
    }
  }
}
