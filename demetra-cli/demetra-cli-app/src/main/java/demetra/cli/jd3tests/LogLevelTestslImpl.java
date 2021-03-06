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
package demetra.cli.jd3tests;

import demetra.cli.tests.*;
import ec.satoolkit.diagnostics.FTest;
import ec.satoolkit.diagnostics.FriedmanTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.tss.TsInformation;
import ec.tstoolkit.information.StatisticalTest;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = LogLevelTestsTool.class)
public final class LogLevelTestslImpl implements LogLevelTestsTool {

    @Override
    public LogLevelTestsResults create(TsInformation info, Options options) {
        LogLevelTestsResults result = new LogLevelTestsResults();
        result.setName(info.name);
        try{
        }
        catch (Exception err){}
        return result;
    }

    //</editor-fold>
}
