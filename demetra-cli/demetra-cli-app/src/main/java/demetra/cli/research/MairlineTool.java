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

import be.nbb.demetra.mairline.MaSpecification;
import be.nbb.demetra.mairline.MixedAirlineSpecification;
import be.nbb.demetra.sssts.SeasonalSpecification;
import be.nbb.demetra.toolset.Record;
import ec.demetra.ssf.implementations.structural.Component;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.information.InformationSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Data;
import lombok.Value;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(isSingleton = true)
public interface MairlineTool {

    @Value
    public static class Options {

        MaSpecification.EstimationMethod method;
    }

    @Data
    public static class MairlineResults implements Record {

        private String name;
        private double refll, ll, daic, aic, bic, th, bth, nseasvar;
        private int[] noisy;
        private String invalidDataCause;

        @Override
        public InformationSet generate() {
            InformationSet info = new InformationSet();
            info.set("series", name);
            if (invalidDataCause != null) {
                info.set("error", invalidDataCause);
            } else {
                info.set("refll", refll);
                info.set("ll", ll);
                info.set("daic", daic);
                info.set("aic", aic);
                info.set("bic", bic);
                info.set("th", th);
                info.set("bth", bth);
                info.set("nseasvar", nseasvar);
                info.set("nnoisy", noisy == null ? 0 : noisy.length);
                if (noisy != null) {
                    for (int i = 0; i < Math.min(noisy.length, 8); ++i) {
                        info.set("noisy-" + (i), noisy[i]);
                    }
                }
            }
            return info;
        }
    }

    @Nonnull
    MairlineResults create(@Nonnull TsInformation info, @Nonnull Options options);

    @Nonnull
    default List<InformationSet> create(TsCollectionInformation info, Options options) {
        return info.items.parallelStream().map(o -> create(o, options).generate()).collect(Collectors.toList());
    }

    public static MairlineTool getDefault() {
        return Lookup.getDefault().lookup(MairlineTool.class);
    }
}
