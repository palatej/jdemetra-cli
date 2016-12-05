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

import ec.tss.TsInformation;
import ec.tstoolkit.algorithm.implementation.TramoProcessingFactory;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = AirlineTool.class)
public final class AirlineToolImpl implements AirlineTool {

    @Override
    public AirlineResults create(TsInformation info, Options options) {
        AirlineResults result = new AirlineResults();
        result.setName(info.name);
        try {
            TramoSpecification spec=TramoSpecification.TR2.clone();
            spec.getArima().setMean(false);
            spec.getRegression().getCalendar().getTradingDays().setTradingDaysType(TradingDaysType.TradingDays);
            PreprocessingModel rslt = TramoProcessingFactory.instance.generateProcessing(spec).process(info.data);
            
            ConcentratedLikelihood likelihood = rslt.estimation.getLikelihood();
            SarimaModel arima = rslt.estimation.getArima();
            int nparams=2;
            result.setLl(likelihood.getLogLikelihood());
            result.setAic(likelihood.AIC(nparams));
            result.setBic(likelihood.BIC(nparams));
            result.setTh(arima.theta(1));
            result.setBth(arima.btheta(1));
        } catch (Exception err) {
            result.setInvalidDataCause(err.getMessage());
        }
        return result;
    }

    //</editor-fold>
}
