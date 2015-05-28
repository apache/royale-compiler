package flex2.tools;

import org.apache.flex.compiler.clients.COMPJSC;

/**
 * @author: Frederic Thomas
 * Date: 25/05/2015
 * Time: 14:05
 */
public class CompJSC extends MxmlJSC {

    static {
        COMPILER = COMPJSC.class;
    }
}
