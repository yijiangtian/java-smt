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
package org.sosy_lab.java_smt.api;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * This class provides an interface to an incremental SMT solver with methods for pushing and
 * popping formulas as well as SAT checks. Furthermore, interpolants can be generated for an
 * unsatisfiable list of formulas.
 *
 * @see ProverEnvironment The non-interpolating ProverEnvironment for general notes that also apply
 *     to this interface.
 */
public interface InterpolatingProverEnvironment extends BasicProverEnvironment {

  /**
   * Add a formula to the environment stack, asserting it. The returned value can be used when
   * selecting the formulas for interpolant generation.
   */
  @Override
  @Nonnull
  @CanIgnoreReturnValue
  default InterpolationHandle push(BooleanFormula f) {
    // Java8 does not support overriding of default-interface-methods,
    // thus we forward to the super-interface here.
    return BasicProverEnvironment.super.push(f);
  }

  /**
   * Compute an inductive sequence of interpolants over a list of interpolation handlers, generated
   * with {@link #push(BooleanFormula)} and {@link #addConstraint(BooleanFormula)}. Each list
   * element may contain one or multiple interpolation handles: the semantics of each such
   * collection is a conjunction of all the contained formulas.
   *
   * <p>The stack must contain exactly the partitioned formulas, but any order is allowed. For an
   * input of {@code N} partitions we return {@code N-1} interpolants.
   *
   * @return an inductive sequence of interpolants, such that the implication {@code AND(I_i, P_i)
   *     => I_(i+1)} is satisfied for all {@code i}, where {@code P_i} is the conjunction of all
   *     formulas in partition i.
   * @throws SolverException if interpolant cannot be computed (e.g. if interpolation procedure is
   *     incomplete).
   */
  List<BooleanFormula> getSeqInterpolants(
      List<? extends Collection<InterpolationHandle>> partitionedFormulas)
      throws SolverException, InterruptedException;

  /**
   * Compute an inductive sequence of interpolants. Syntax sugar over the method {@link
   * #getSeqInterpolants} for the case where all elements of the list contain only one handle.
   */
  default List<BooleanFormula> getSeqInterpolants2(List<InterpolationHandle> partitionedFormulas)
      throws SolverException, InterruptedException {
    return getSeqInterpolants(Lists.transform(partitionedFormulas, ImmutableSet::of));
  }

  /**
   * Compute a sequence of interpolants. The nesting array describes the start of the subtree for
   * tree interpolants. For inductive sequences of interpolants use a nesting array completely
   * filled with 0.
   *
   * <p>Example:
   *
   * <pre>
   * A  D
   * |  |
   * B  E
   * | /
   * C
   * |
   * F  H
   * | /
   * G
   *
   * arrayIndex     = [0,1,2,3,4,5,6,7]  // only for demonstration, not needed
   * partition      = [A,B,D,E,C,F,H,G]  // post-order of tree
   * startOfSubTree = [0,0,2,2,0,0,6,0]  // index of left-most leaf of the current element
   * </pre>
   *
   * @param partitionedFormulas of formulas
   * @param startOfSubTree The start of the subtree containing the formula at this index as root.
   * @return Tree interpolants respecting the nesting relation.
   * @throws SolverException if interpolant cannot be computed, for example because interpolation
   *     procedure is incomplete
   */
  List<BooleanFormula> getTreeInterpolants(
      List<? extends Collection<InterpolationHandle>> partitionedFormulas, int[] startOfSubTree)
      throws SolverException, InterruptedException;

  /**
   * Check whether the conjunction of all formulas on the stack together with the list of
   * assumptions is satisfiable.
   *
   * @param assumptions A list of literals.
   */
  boolean isUnsatWithAssumptions(Collection<BooleanFormula> assumptions)
      throws SolverException, InterruptedException;
}
