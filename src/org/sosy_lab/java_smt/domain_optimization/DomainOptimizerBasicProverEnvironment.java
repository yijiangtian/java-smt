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

import org.sosy_lab.java_smt.api.BasicProverEnvironment;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Model;
import org.sosy_lab.java_smt.api.SolverException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

class DomainOptimizerBasicProverEnvironment<T> implements BasicProverEnvironment<T> {

    private final BasicProverEnvironment<T> wrapped;
    final DomainOptimizer optimizer;

    DomainOptimizerBasicProverEnvironment(BasicProverEnvironment<T> wrapped, DomainOptimizer optimizer) {
        this.wrapped = wrapped;
        this.optimizer = optimizer;
    }

    @Override
    public void pop() {

    }

    @Override
    public T addConstraint(BooleanFormula constraint) throws InterruptedException {
        return null;
    }

    @Override
    public void push() {

    }

    @Override
    public boolean isUnsat() throws SolverException, InterruptedException {
        return false;
    }

    @Override
    public boolean isUnsatWithAssumptions(Collection<BooleanFormula> assumptions) throws SolverException, InterruptedException {
        return false;
    }

    @Override
    public Model getModel() throws SolverException {
        return null;
    }

    @Override
    public List<BooleanFormula> getUnsatCore() {
        return null;
    }

    @Override
    public Optional<List<BooleanFormula>> unsatCoreOverAssumptions(Collection<BooleanFormula> assumptions) throws SolverException, InterruptedException {
        return Optional.empty();
    }

    @Override
    public void close() {

    }

    @Override
    public <R> R allSat(AllSatCallback<R> callback, List<BooleanFormula> important) throws InterruptedException, SolverException {
        return null;
    }
}
