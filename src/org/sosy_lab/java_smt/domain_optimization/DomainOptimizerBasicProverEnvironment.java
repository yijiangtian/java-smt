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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.sosy_lab.java_smt.api.BasicProverEnvironment;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Model;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverException;

class DomainOptimizerBasicProverEnvironment<T> implements BasicProverEnvironment<T> {

    final SolverContext delegate;
    private final ProverEnvironment wrapped;

  DomainOptimizerBasicProverEnvironment(
      SolverContext delegate, ProverEnvironment pProverEnvironment) {
        this.delegate = delegate;
    this.wrapped = pProverEnvironment;
    }

    @Override
    public void pop() {
        this.wrapped.pop();
    }

    @Override
    public T addConstraint(BooleanFormula constraint) throws InterruptedException {
    // TODO add constraint into Optimizer
    this.wrapped.addConstraint(constraint);
        return null;
    }

    @Override
    public void push() {
    // TODO push Optimizer
    this.wrapped.push();
    }

    @Override
    public boolean isUnsat() throws SolverException, InterruptedException {
    // TODO check status of Optimizer
    return this.wrapped.isUnsat();
    }

    @Override
    public boolean isUnsatWithAssumptions(Collection<BooleanFormula> assumptions) throws SolverException, InterruptedException {
    throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Model getModel() throws SolverException {
    // TODO check model of Optimizer
    return this.wrapped.getModel();
    }

    @Override
    public List<BooleanFormula> getUnsatCore() {
    throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Optional<List<BooleanFormula>> unsatCoreOverAssumptions(Collection<BooleanFormula> assumptions) throws SolverException, InterruptedException {
    throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void close() {
    wrapped.close();
    }

    @Override
    public <R> R allSat(AllSatCallback<R> callback, List<BooleanFormula> important) throws InterruptedException, SolverException {
    // TODO we could implement this via extension of AbstractProverWithAllSat
    throw new UnsupportedOperationException("not yet implemented");
    }
}
