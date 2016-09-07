package org.apache.flex.maven.flexjs.extension;

import org.apache.maven.repository.internal.LanguageSupport;
import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaScopeDeriver;
import org.eclipse.aether.util.graph.transformer.JavaScopeSelector;

import javax.inject.Named;

/**
 * Created by christoferdutz on 18.07.16.
 */
@Named
@Component( role = LanguageSupport.class, hint = "flexjs" )
public class FlexJsLanguageSupport implements LanguageSupport {

    private static final FlexJsScopeSelector SCOPE_SELECTOR = new FlexJsScopeSelector();
    private static final FlexJsScopeDeriver SCOPE_DERIVER = new FlexJsScopeDeriver();

    public FlexJsLanguageSupport() {
        System.out.println("FlexJS Support");
    }

    @Override
    public String getLanguageName() {
        return "flexjs";
    }

    @Override
    public ConflictResolver.ScopeSelector getScopeSelector() {
        return SCOPE_SELECTOR;
    }

    @Override
    public ConflictResolver.ScopeDeriver getScopeDeriver() {
        return SCOPE_DERIVER;
    }

}
