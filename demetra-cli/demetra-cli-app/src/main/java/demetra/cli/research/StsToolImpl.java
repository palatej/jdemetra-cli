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

import be.nbb.demetra.sts.BsmSpecification;
import be.nbb.demetra.sts.StsEstimation;
import be.nbb.demetra.sts.StsProcessingFactory;
import be.nbb.demetra.sts.StsSpecification;
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
@ServiceProvider(service = StsTool.class)
public final class StsToolImpl implements StsTool {

    @Override
    public StsResults create(TsInformation info, Options options) {
        StsResults result = new StsResults();
        result.setName(info.name);
        try {
            StsSpecification spec = new StsSpecification();
            BsmSpecification dspec = spec.getDecompositionSpec();
            ModelSpecification mspec = new ModelSpecification();
            mspec.useLevel(ComponentUse.Free);
            mspec.useSlope(ComponentUse.Free);
            mspec.useNoise(ComponentUse.Free);
            mspec.useCycle(ComponentUse.Unused);
            mspec.setSeasonalModel(options.getModel());
            dspec.setModelSpecification(mspec);

            CompositeResults rslt = StsProcessingFactory.process(info.data, spec);
            StsEstimation estimation = rslt.get(StsProcessingFactory.ESTIMATION, StsEstimation.class);
            BasicStructuralModel model = estimation.getModel();
            DkConcentratedLikelihood likelihood = estimation.getLikelihood();
            int nparams=3;
            result.setLl(likelihood.getLogLikelihood());
            result.setAic(likelihood.AIC(nparams));
            result.setBic(likelihood.BIC(nparams));
            result.setNvar(model.getVariance(Component.Noise));
            result.setCvar(model.getVariance(Component.Cycle));
            result.setSvar(model.getVariance(Component.Slope));
            result.setLvar(model.getVariance(Component.Level));
            result.setSeasvar(model.getVariance(Component.Seasonal));
        } catch (Exception err) {
            result.setInvalidDataCause(err.getMessage());
        }
        return result;
    }

    //</editor-fold>
}
