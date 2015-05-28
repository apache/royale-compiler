package flex2.tools;

import org.apache.flex.compiler.clients.problems.ProblemQueryProvider;
import org.apache.flex.compiler.clients.JSCompilerEntryPoint;
import org.apache.flex.compiler.clients.MXMLJSC;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.as.ASBackend;
import org.apache.flex.compiler.internal.driver.js.amd.AMDBackend;
import org.apache.flex.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.flex.compiler.internal.driver.mxml.flexjs.MXMLFlexJSBackend;
import org.apache.flex.compiler.internal.driver.mxml.vf2js.MXMLVF2JSBackend;
import org.apache.flex.compiler.problems.ICompilerProblem;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Frederic Thomas
 * Date: 25/05/2015
 * Time: 14:05
 */
public class MxmlJSC implements ProblemQueryProvider {

    protected static Class<? extends MXMLJSC> COMPILER = MXMLJSC.class;

    protected JSCompilerEntryPoint compiler;

    protected JSCompilerEntryPoint getCompilerInstance(IBackend backend) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (compiler == null) {
            compiler = COMPILER.getDeclaredConstructor(IBackend.class).newInstance(backend);
        }
        return compiler;
    }

    public int execute(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IBackend backend = new ASBackend();
        String jsOutputTypeString = "";
        for (String s : args) {
            String[] kvp = s.split("=");

            if (s.contains("-js-output-type")) {
                jsOutputTypeString = kvp[1];
            }
        }

        if (jsOutputTypeString.equals("")) {
            jsOutputTypeString = MXMLJSC.JSOutputType.FLEXJS.getText();
        }

        MXMLJSC.JSOutputType jsOutputType = MXMLJSC.JSOutputType.fromString(jsOutputTypeString);
        switch (jsOutputType) {
            case AMD:
                backend = new AMDBackend();
                break;
            case FLEXJS:
                backend = new MXMLFlexJSBackend();
                break;
            case GOOG:
                backend = new GoogBackend();
                break;
            case VF2JS:
                backend = new MXMLVF2JSBackend();
                break;
        }

        final Set<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        return getCompilerInstance(backend).mainNoExit(args, problems, false);
    }

    @Override
    public ProblemQuery getProblemQuery() {
        return ((ProblemQueryProvider) compiler).getProblemQuery();
    }
}
