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
package demetra.cli.odbc;

import be.nbb.demetra.toolset.ProviderTool;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import be.nbb.cli.util.joptsimple.JOptSimpleArgsParser;
import be.nbb.cli.util.BasicCliLauncher;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.odbc.OdbcBean;
import ec.tss.tsproviders.odbc.OdbcProvider;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.xml.XmlTsCollection;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.proc.CommandRegistration;
import org.openide.util.NbBundle;
import be.nbb.cli.util.joptsimple.ComposedOptionSpec;
import demetra.cli.helpers.XmlUtil;
import demetra.cli.tsproviders.TsDataBuild;
import demetra.cli.tsproviders.TsProviderOptionSpecs;

/**
 * Retrieves time series from an ODBC DSN.
 *
 * @author Philippe Charles
 */
public final class Odbc2Ts implements BasicCommand<Odbc2Ts.Parameters> {

    @CommandRegistration
    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Odbc2Ts::new, o -> o.so);
    }

    public static final class Parameters {

        StandardOptions so;
        public OdbcBean input;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        OdbcProvider provider = new OdbcProvider();
        TsCollectionInformation result = ProviderTool.getDefault().getTsCollection(provider, params.input, TsInformationType.All);
        XmlUtil.writeValue(params.output, XmlTsCollection.class, result);
        provider.dispose();
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<OdbcBean> input = new OdbcOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            Parameters result = new Parameters();
            result.input = input.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }

    @NbBundle.Messages({
        "odbc2ts.dbName=Data source name",
        "odbc2ts.tableName=Table name",
        "odbc2ts.dimColumns=Comma-separated list of dimension columns",
        "odbc2ts.periodColumn=Period column",
        "odbc2ts.valueColumn=Value column",
        "odbc2ts.versionColumn=Version column",
        "odbc2ts.frequency=Frequency of observations",
        "odbc2ts.aggregationType=Aggregation method to use when the frequency is defined"
    })
    private static final class OdbcOptionsSpec implements ComposedOptionSpec<OdbcBean> {

        // source
        private final OptionSpec<String> dbName;
        private final OptionSpec<String> tableName;
        private final OptionSpec<String> dimColumns;
        private final OptionSpec<String> periodColumn;
        private final OptionSpec<String> valueColumn;
        private final OptionSpec<String> versionColumn;
        // options
        private final ComposedOptionSpec<DataFormat> dataFormat;
        private final ComposedOptionSpec<TsDataBuild> tsDataBuild;

        public OdbcOptionsSpec(OptionParser p) {
            this.dbName = p
                    .accepts("dsn", Bundle.odbc2ts_dbName())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("dsn")
                    .defaultsTo("");
            this.tableName = p
                    .accepts("table", Bundle.odbc2ts_tableName())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("table")
                    .defaultsTo("");
            this.dimColumns = p
                    .accepts("dims", Bundle.odbc2ts_dimColumns())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("dimColumns")
                    .withValuesSeparatedBy(',')
                    .defaultsTo("");
            this.periodColumn = p
                    .accepts("period", Bundle.odbc2ts_periodColumn())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("periodColumn")
                    .defaultsTo("");
            this.valueColumn = p
                    .accepts("value", Bundle.odbc2ts_valueColumn())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("valueColumn")
                    .defaultsTo("");
            this.versionColumn = p
                    .accepts("rev", Bundle.odbc2ts_versionColumn())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("versionColumn")
                    .defaultsTo("");
            this.dataFormat = TsProviderOptionSpecs.newDataFormatSpec(p);
            this.tsDataBuild = TsProviderOptionSpecs.newTsDataBuildSpec(p);
        }

        @Override
        public OdbcBean value(OptionSet o) {
            OdbcBean result = new OdbcBean();
            result.setDbName(dbName.value(o));
            result.setTableName(tableName.value(o));
            result.setDimColumns(Joiner.on(',').join(dimColumns.values(o)));
            result.setPeriodColumn(periodColumn.value(o));
            result.setValueColumn(valueColumn.value(o));
            result.setVersionColumn(versionColumn.value(o));
            result.setDataFormat(dataFormat.value(o));
            TsDataBuild tmp = tsDataBuild.value(o);
            result.setFrequency(tmp.getFrequency());
            result.setAggregationType(tmp.getAggregationType());
            return result;
        }
    }
}
