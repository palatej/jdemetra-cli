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

import be.nbb.demetra.sssts.SeasonalSpecification;
import be.nbb.demetra.toolset.Record;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.implementations.structural.SeasonalModel;
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
public interface SshsTool {

    @Value
    public static class Options {

        SeasonalSpecification.EstimationMethod method;
        Component noisy;
    }

    @Data
    public static class SshsResults implements Record {

        private String name;
        private double refll, ll, daic, aic, bic, lvar, svar, seasvar, nseasvar, nvar;
        private int[] noisy;
        private String invalidDataCause;

        @Override
        public InformationSet generate() {
            InformationSet info = new InformationSet();
            info.set("series", name);
            info.set("refll", refll);
            info.set("ll", ll);
            info.set("daic", daic);
            info.set("aic", aic);
            info.set("bic", bic);
            info.set("lvar", lvar);
            info.set("svar", svar);
            info.set("seasvar", seasvar);
            info.set("nvar", nvar);
            info.set("nseasvar", nseasvar);
            info.set("nnoisy", noisy.length);
            for (int i=0; i<Math.min(noisy.length,8); ++i){
                info.set("noisy-"+(i), noisy[i]);
            }
            if (invalidDataCause != null) {
                info.set("error", invalidDataCause);
            }
            return info;
        }
    }

    @Nonnull
    SshsResults create(@Nonnull TsInformation info, @Nonnull Options options);

    @Nonnull
    default List<InformationSet> create(TsCollectionInformation info, Options options) {
        return info.items.parallelStream().map(o -> create(o, options).generate()).collect(Collectors.toList());
    }

    public static SshsTool getDefault() {
        return Lookup.getDefault().lookup(SshsTool.class);
    }
}
