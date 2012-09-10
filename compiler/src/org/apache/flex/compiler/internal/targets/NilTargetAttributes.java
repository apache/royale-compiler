package org.apache.flex.compiler.internal.targets;

import java.util.Collections;
import java.util.Map;

/**
 * Nil implementation of {@link ITargetAttributes}.
 */
public class NilTargetAttributes implements ITargetAttributes
{
    private NilTargetAttributes() {}
    public static NilTargetAttributes INSTANCE = new NilTargetAttributes();
    
    @Override
    public Float getWidth()
    {
        return null;
    }

    @Override
    public Float getHeight()
    {
        return null;
    }

    @Override
    public Double getWidthPercentage()
    {
        return null;
    }

    @Override
    public Double getHeightPercentage()
    {
        return null;
    }

    @Override
    public Float getFrameRate()
    {
        return null;
    }

    @Override
    public String getPreloaderClassName()
    {
        return null;
    }

    @Override
    public String getRuntimeDPIProviderClassName()
    {
        return null;
    }

    @Override
    public String getSplashScreenImage()
    {
        return null;
    }

    @Override
    public String getPageTitle()
    {
        return null;
    }

    @Override
    public Integer getScriptRecursionLimit()
    {
        return null;
    }

    @Override
    public Integer getScriptTimeLimit()
    {
        return null;
    }

    @Override
    public Boolean getUseGPU()
    {
        return null;
    }

    @Override
    public Boolean getUseDirectBlit()
    {
        return null;
    }

    @Override
    public Boolean getUsePreloader()
    {
        return null;
    }

    @Override
    public String getBackgroundColor()
    {
        return null;
    }

    @Override
    public Map<String, String> getRootInfoAttributes()
    {
        return Collections.emptyMap();
    }
}