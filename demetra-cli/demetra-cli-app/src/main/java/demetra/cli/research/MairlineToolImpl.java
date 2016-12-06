/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.cli.research;

import be.nbb.demetra.mairline.MixedAirlineMonitor;
import be.nbb.demetra.mairline.MixedAirlineProcessingFactory;
import be.nbb.demetra.mairline.MixedAirlineResults;
import be.nbb.demetra.mairline.MixedAirlineSpecification;
import demetra.cli.research.MairlineTool.MairlineResults;
import ec.demetra.ssf.dk.DkConcentratedLikelihood;
import ec.demetra.ssf.implementations.structural.BasicStructuralModel;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.implementations.structural.ComponentUse;
import ec.demetra.ssf.implementations.structural.ModelSpecification;
import ec.tss.TsInformation;
import ec.tstoolkit.algorithm.CompositeResults;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = MairlineTool.class)
public final class MairlineToolImpl implements MairlineTool {

    @Override
    public MairlineResults create(TsInformation info, Options options) {
        MairlineResults result = new MairlineResults();
        result.setName(info.name);
        try {
            MixedAirlineSpecification spec = new MixedAirlineSpecification();
            spec.getDecompositionSpec().method=options.getMethod();
            CompositeResults rslt = MixedAirlineProcessingFactory.process(info.data, spec);
            MixedAirlineResults decomp = rslt.get(MixedAirlineProcessingFactory.DECOMPOSITION, MixedAirlineResults.class);
            MixedAirlineMonitor.MixedEstimation ref = decomp.getAllModels().get(0);
            MixedAirlineMonitor.MixedEstimation best = decomp.getBestModel();
            int nparams = 4;
            result.setLl(best.ll.getLogLikelihood());
            result.setRefll(ref.ll.getLogLikelihood());
            result.setDaic(ref.ll.AIC(nparams-1)-best.ll.AIC(nparams));
            result.setAic(best.ll.AIC(nparams));
            result.setBic(best.ll.BIC(nparams));
            result.setTh(best.model.getTheta());
            result.setBth(best.model.getBTheta());
            result.setNseasvar(best.model.getNoisyPeriodsVariance());
            result.setNoisy(best.model.getNoisyPeriods());

        } catch (Exception err) {
            result.setInvalidDataCause(err.getMessage());
        }
        return result;
    }

    //</editor-fold>
}
