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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import javax.annotation.Nullable;

/**
 * Super interface for {@link ProverEnvironment}, {@link InterpolatingProverEnvironment}
 * and {@link OptimizationProverEnvironment}.
 */
public interface BasicProverEnvironment extends AutoCloseable {

  /**
   * Push a backtracking point and add a formula to the environment stack, asserting it. The return
   * value may be used to identify this formula for interpolation when
   * {@link InterpolatingProverEnvironment} is used, and is {@code null} otherwise.
   */
  @Nullable
  @CanIgnoreReturnValue
  default InterpolationHandle push(BooleanFormula f) {
    push();
    return addConstraint(f);
  }

  /** Remove one formula from the environment stack. */
  void pop();

  /** Add constraint to the context. The return value may be used to identify this formula for
   * interpolation in {@link InterpolatingProverEnvironment}, and is {@code null} otherwise.
   */
  @Nullable
  @CanIgnoreReturnValue
  InterpolationHandle addConstraint(BooleanFormula constraint);

  /** Create a backtracking point. */
  void push();

  /** @return whether the conjunction of all formulas on the stack is unsatisfiable. */
  boolean isUnsat() throws SolverException, InterruptedException;

  /**
   * Get a satisfying assignment to the set of constraints.
   * May be partial, may include assignments to temporary symbols created by a solver.
   *
   * <p>This should be called only immediately after an {@link #isUnsat()}
   * call that returned {@code false}.
   */
  Model getModel() throws SolverException;

  /**
   * Get a list of satisfying assignments from the generated model.
   * Equivalent to serializing a model obtained with {@link #getModel()},
   * yet removes the need for resource management.
   *
   * <p>Depending on the solver, using this method might be more efficient than iterating
   * multiple times over the model obtained by {@link #getModel()}.
   */
  ImmutableList<Model.ValueAssignment> getModelAssignments() throws SolverException;

  /**
   * Close the prover environment. The environment should not be used after closing.
   */
  @Override
  void close();
}
