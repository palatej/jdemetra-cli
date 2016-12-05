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

import be.nbb.demetra.sssts.SSSTSMonitor;
import be.nbb.demetra.sts.BsmSpecification;
import be.nbb.demetra.sssts.SSSTSProcessingFactory;
import be.nbb.demetra.sssts.SSSTSResults;
import be.nbb.demetra.sssts.SSSTSSpecification;
import be.nbb.demetra.sts.StsEstimation;
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
@ServiceProvider(service = SsstsTool.class)
public final class SsstsToolImpl implements SsstsTool {

    @Override
    public SsstsResults create(TsInformation info, Options options) {
        SsstsResults result = new SsstsResults();
        result.setName(info.name);
        try {
            SSSTSSpecification spec = new SSSTSSpecification();
            ModelSpecification mspec = new ModelSpecification();
            mspec.useLevel(ComponentUse.Free);
            mspec.useSlope(ComponentUse.Free);
            mspec.useNoise(ComponentUse.Free);
            mspec.useCycle(ComponentUse.Unused);
            spec.getDecompositionSpec().method=options.getMethod();
            spec.getDecompositionSpec().noisyComponent=options.getNoisy();

            CompositeResults rslt = SSSTSProcessingFactory.process(info.data, spec);
            SSSTSResults decomp= rslt.get(SSSTSProcessingFactory.DECOMPOSITION, SSSTSResults.class);
            SSSTSMonitor.MixedEstimation best = decomp.getBestModel();
            SSSTSMonitor.MixedEstimation ref = decomp.getAllModels().get(0);
            int nparams=4;
            result.setLl(best.ll.getLogLikelihood());
            result.setRefll(ref.ll.getLogLikelihood());
            result.setDaic(ref.ll.AIC(nparams-1)-best.ll.AIC(nparams));
            result.setAic(best.ll.AIC(nparams));
            result.setBic(best.ll.BIC(nparams));
            result.setNvar(best.model.getNvar());
            result.setSvar(best.model.getSvar());
            result.setLvar(best.model.getLvar());
            result.setSeasvar(best.model.getSeasvar());
                        result.setNseasvar(best.model.getNoisyPeriodsVariance());
                        result.setNoisy(best.model.getNoisyPeriods());

        } catch (Exception err) {
            result.setInvalidDataCause(err.getMessage());
        }
        return result;
    }

    //</editor-fold>
}
