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
package org.sosy_lab.java_smt.solvers.smtinterpol;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import de.uni_freiburg.informatik.ultimate.logic.Annotation;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.InterpolatingProverEnvironment;
import org.sosy_lab.java_smt.api.InterpolationHandle;
import org.sosy_lab.java_smt.api.SolverException;
import org.sosy_lab.java_smt.basicimpl.InterpolationHandlerImpl;

class SmtInterpolInterpolatingProver extends SmtInterpolBasicProver
    implements InterpolatingProverEnvironment {

  private final SmtInterpolFormulaManager mgr;
  private final SmtInterpolEnvironment env;

  SmtInterpolInterpolatingProver(SmtInterpolFormulaManager pMgr) {
    super(pMgr);
    mgr = pMgr;
    env = mgr.createEnvironment();
  }

  @Override
  public void pop() {
    super.pop();
  }

  @Override
  public InterpolationHandle addConstraint(BooleanFormula f) {
    Preconditions.checkState(!isClosed());
    String termName = generateTermName();
    Term t = mgr.extractInfo(f);
    Term annotatedTerm = env.annotate(t, new Annotation(":named", termName));
    env.assertTerm(annotatedTerm);
    assertedFormulas.peek().add(t);
    return new InterpolationHandlerImpl<>(termName);
  }

  @Override
  public List<BooleanFormula> getSeqInterpolants(
      List<? extends Iterable<InterpolationHandle>> partitionedTermNames)
      throws SolverException, InterruptedException {
    Preconditions.checkState(!isClosed());

    final Term[] formulas = new Term[partitionedTermNames.size()];
    for (int i = 0; i < formulas.length; i++) {
      formulas[i] = buildConjunctionOfNamedTerms(partitionedTermNames.get(i));
    }

    // get interpolants of groups
    final Term[] itps = env.getInterpolants(formulas);

    final List<BooleanFormula> result = new ArrayList<>();
    for (Term itp : itps) {
      result.add(mgr.encapsulateBooleanFormula(itp));
    }
    return result;
  }

  @Override
  public List<BooleanFormula> getTreeInterpolants(
      List<? extends Iterable<InterpolationHandle>> partitionedTermNames, int[] startOfSubTree)
      throws SolverException, InterruptedException {
    Preconditions.checkState(!isClosed());

    final Term[] formulas = new Term[partitionedTermNames.size()];
    for (int i = 0; i < formulas.length; i++) {
      formulas[i] = buildConjunctionOfNamedTerms(partitionedTermNames.get(i));
    }

    assert checkSubTrees(partitionedTermNames, startOfSubTree);

    // get interpolants of groups
    final Term[] itps = env.getTreeInterpolants(formulas, startOfSubTree);

    final List<BooleanFormula> result = new ArrayList<>();
    for (Term itp : itps) {
      result.add(mgr.encapsulateBooleanFormula(itp));
    }
    return result;
  }

  /**
   * Checks for a valid subtree-structure. This code is taken from SMTinterpol itself, where it is
   * disabled.
   */
  private static boolean checkSubTrees(
      List<? extends Iterable<InterpolationHandle>> partitionedTermNames, int[] startOfSubTree) {
    for (int i = 0; i < partitionedTermNames.size(); i++) {
      if (startOfSubTree[i] < 0) {
        throw new AssertionError("subtree array must not contain negative element");
      }
      int j = i;
      while (startOfSubTree[i] < j) {
        j = startOfSubTree[j - 1];
      }
      if (startOfSubTree[i] != j) {
        throw new AssertionError("malformed subtree array.");
      }
    }
    if (startOfSubTree[partitionedTermNames.size() - 1] != 0) {
      throw new AssertionError("malformed subtree array.");
    }

    return true;
  }

  protected BooleanFormula getInterpolant(Term termA, Term termB)
      throws SolverException, InterruptedException {
    Preconditions.checkState(!isClosed());
    // get interpolant of groups
    Term[] itp = env.getInterpolants(new Term[] {termA, termB});
    assert itp.length == 1; // 2 groups -> 1 interpolant

    return mgr.encapsulateBooleanFormula(itp[0]);
  }

  private Term buildConjunctionOfNamedTerms(Iterable<InterpolationHandle> termNames) {
    Preconditions.checkState(!isClosed());

    Term[] terms = StreamSupport.stream(termNames.spliterator(), false)
        .map(t -> env.term((String) t.getValue()))
        .toArray(Term[]::new);

    if (terms.length > 1) {
      return env.term("and", terms);
    } else {
      return Iterators.getOnlyElement(Iterators.forArray(terms));
    }
  }
}
