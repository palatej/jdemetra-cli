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

import be.nbb.demetra.sssts.SSHSMonitor;
import be.nbb.demetra.sts.BsmSpecification;
import be.nbb.demetra.sssts.SSHSProcessingFactory;
import be.nbb.demetra.sssts.SSHSResults;
import be.nbb.demetra.sssts.SSHSSpecification;
import demetra.cli.research.SshsTool.SshsResults;
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
@ServiceProvider(service = SshsTool.class)
public final class SshsToolImpl implements SshsTool {

    @Override
    public SshsResults create(TsInformation info, Options options) {
        SshsResults result = new SshsResults();
        result.setName(info.name);
        try {
            SSHSSpecification spec = new SSHSSpecification();
            ModelSpecification mspec = new ModelSpecification();
            mspec.useLevel(ComponentUse.Free);
            mspec.useSlope(ComponentUse.Free);
            mspec.useNoise(ComponentUse.Free);
            mspec.useCycle(ComponentUse.Unused);
            spec.getDecompositionSpec().method=options.getMethod();
            spec.getDecompositionSpec().noisyComponent=options.getNoisy();
            CompositeResults rslt = SSHSProcessingFactory.process(info.data, spec);
            SSHSResults decomp = rslt.get(SSHSProcessingFactory.DECOMPOSITION, SSHSResults.class);
            SSHSMonitor.MixedEstimation ref = decomp.getAllModels().get(0);
            SSHSMonitor.MixedEstimation best = decomp.getBestModel();
            int nparams = 4;
            result.setLl(best.ll.getLogLikelihood());
            result.setRefll(ref.ll.getLogLikelihood());
            result.setDaic(ref.ll.AIC(nparams-1)-best.ll.AIC(nparams));
            result.setAic(best.ll.AIC(nparams));
            result.setBic(best.ll.BIC(nparams));
            result.setNvar(best.model.getBasicStructuralModel().getVariance(Component.Noise));
            result.setSvar(best.model.getBasicStructuralModel().getVariance(Component.Slope));
            result.setLvar(best.model.getBasicStructuralModel().getVariance(Component.Level));
            result.setLvar(best.model.getBasicStructuralModel().getVariance(Component.Seasonal));
            result.setNseasvar(best.model.getNoisyPeriodsVariance());
            result.setNoisy(best.model.getNoisyPeriods());

        } catch (Exception err) {
            result.setInvalidDataCause(err.getMessage());
        }
        return result;
    }

    //</editor-fold>
}
