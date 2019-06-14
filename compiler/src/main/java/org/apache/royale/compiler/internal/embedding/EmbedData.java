/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.compiler.internal.embedding;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition.FunctionClassification;
import org.apache.royale.compiler.definitions.IGetterDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.embedding.IEmbedData;
import org.apache.royale.compiler.embedding.transcoders.ITranscoder;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.embedding.transcoders.DataTranscoder;
import org.apache.royale.compiler.internal.embedding.transcoders.ImageTranscoder;
import org.apache.royale.compiler.internal.embedding.transcoders.JPEGTranscoder;
import org.apache.royale.compiler.internal.embedding.transcoders.MovieTranscoder;
import org.apache.royale.compiler.internal.embedding.transcoders.PBJTranscoder;
import org.apache.royale.compiler.internal.embedding.transcoders.SoundTranscoder;
import org.apache.royale.compiler.internal.embedding.transcoders.TranscoderBase;
import org.apache.royale.compiler.internal.embedding.transcoders.XMLTranscoder;
import org.apache.royale.compiler.internal.projects.ASProject;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.projects.SourcePathManager;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.EmbedInvalidAttributeValueProblem;
import org.apache.royale.compiler.problems.EmbedNoSourceAttributeProblem;
import org.apache.royale.compiler.problems.EmbedQualityValueProblem;
import org.apache.royale.compiler.problems.EmbedScalingGridValueProblem;
import org.apache.royale.compiler.problems.EmbedSourceAttributeDoesNotExistProblem;
import org.apache.royale.compiler.problems.EmbedUnknownAttributeProblem;
import org.apache.royale.compiler.problems.EmbedUnknownMimeTypeProblem;
import org.apache.royale.compiler.problems.EmbedUnrecogniedFileTypeProblem;
import org.apache.royale.compiler.problems.FontEmbeddingNotSupported;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.IASProject;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swf.ISWFConstants;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.StringEncoder;

/**
 * This is the main class which contains all information extracted from embed
 * meta data.
 */
public class EmbedData implements IEmbedData
{
    private static final String EMBED_SWC_SEP = "|";

    public static class SkinClassInfo
    {
        public boolean needsIBorder;
        public boolean needsIFlexDisplayObject;
        public boolean royaleMovieClipOrSprite;

        public boolean needsBorderMetrics;
        public boolean needsMeasuredHeight;
        public boolean needsMeasuredWidth;

        public boolean needsMove;
        public boolean needsSetActualSize;

        protected SkinClassInfo(ICompilerProject project, ClassDefinition definition)
        {
            needsIBorder = !definition.isInstanceOf(TranscoderBase.CORE_PACKAGE + ".IBorder", project);
            needsIFlexDisplayObject = !definition.isInstanceOf(TranscoderBase.CORE_PACKAGE + ".IFlexDisplayObject", project);
            royaleMovieClipOrSprite = definition.isInstanceOf(TranscoderBase.CORE_PACKAGE + ".RoyaleMovieClip", project) ||
                                    definition.isInstanceOf(TranscoderBase.CORE_PACKAGE + ".FlexSprite", project);

            final INamespaceDefinition qualifier = definition.getNamespaceReference().resolveNamespaceReference(project);
            needsBorderMetrics = needsGetter(project, qualifier, definition, "borderMetrics");
            needsMeasuredHeight = needsGetter(project, qualifier, definition, "measuredHeight");
            needsMeasuredWidth = needsGetter(project, qualifier, definition, "measuredWidth");
            needsMove = needsFunction(project, qualifier, definition, "move");
            needsSetActualSize = needsFunction(project, qualifier, definition, "setActualSize");
        }

        private static boolean needsGetter(ICompilerProject project, INamespaceDefinition qualifier, ClassDefinition classDefinition, String baseName)
        {
            final ASScope scope = classDefinition.getContainedScope();
            IDefinition def = scope.getQualifiedPropertyFromDef(project, classDefinition, baseName, qualifier, false);
            return (def instanceof IGetterDefinition) ? false : true;
        }

        private static boolean needsFunction(ICompilerProject project, INamespaceDefinition qualifier, ClassDefinition classDefinition, String baseName)
        {
            final ASScope scope = classDefinition.getContainedScope();
            IDefinition def = scope.getQualifiedPropertyFromDef(project, classDefinition, baseName, qualifier, false);
            if (def instanceof IFunctionDefinition)
            {
                FunctionClassification classification = ((IFunctionDefinition)def).getFunctionClassification();
                return (classification == FunctionClassification.CLASS_MEMBER) ? false : true;
            }

            return false;
        }
    }

    public EmbedData(String containingSourceFilename, String specifiedQName)
    {
        this.containingSourceFilename = containingSourceFilename;
        this.attributes = new HashMap<EmbedAttribute, Object>();
        this.specifiedName = specifiedQName;
        this.swcSource = null;
        this.skinClassInfo = null;
    }

    private final String containingSourceFilename;
    private final HashMap<EmbedAttribute, Object> attributes;
    private final String specifiedName;
    private TranscoderBase transcoder;
    private ISWCFileEntry swcSource;
    @SuppressWarnings("unused")
    private SkinClassInfo skinClassInfo;
    private ICompilerProject project;

    /**
     * Add an attribute
     * 
     * @param project containing project
     * @param location source location of the attribute
     * @param key attribute key
     * @param value attribute value
     * @param problems any problems with the key or value
     * @return true if there was an error
     */
    public boolean addAttribute(ICompilerProject project, ISourceLocation location, String key, String value, Collection<ICompilerProblem> problems)
    {
    	this.project = project;
        boolean hadError = false;
        try
        {
            // a null key means default to source, ie [Embed="image.png"]
            if (EmbedAttribute.SOURCE.equals(key) || key == null)
            {
                // put source resolution problems into a separate collection first
                // so that if we fail because there's an octothorpe, and resolve successfully
                // later on, we haven't created incorrect problems.
                List<ICompilerProblem> resolveProblems = new LinkedList<ICompilerProblem>();
                String source = getResolvedSourcePath(project, location, value, resolveProblems);

                // could not resolve the source, so check for an octothorpe
                // which indicates a file within a SWC
                if (source == null)
                {
                    int octothorpe = value.indexOf("#");
                    if (octothorpe != -1)
                    {
                        source = getResolvedSourcePath(project, location, value.substring(0, octothorpe), problems);

                        String symbol = value.substring(octothorpe + 1);
                        attributes.put(EmbedAttribute.SYMBOL, symbol);
                    }
                    else
                    {
                        problems.addAll(resolveProblems);
                    }
                }

                if (source != null)
                {
                    attributes.put(EmbedAttribute.SOURCE, source);

                    // if we have a filename, but the mimeType hasn't been set yet,
                    // set if from the filename, but override it later on if there
                    // is an explicit mimeType, as that takes priority
                    if (!attributes.containsKey(EmbedAttribute.MIME_TYPE))
                    {
                        attributes.put(EmbedAttribute.MIME_TYPE, EmbedMIMEType.getMimeTypeFromFilename(source));
                    }
                }
                else
                {
                    Collection<ICompilationUnit> referencingCUs = project.getCompilationUnits(containingSourceFilename);
                    for (ICompilationUnit cu : referencingCUs)
                    {
                        ((RoyaleProject)project).addUnfoundReferencedSourceFileDependency(value, cu);
                    }
                    hadError = true;
                }
            }
            else if (EmbedAttribute.MIME_TYPE.equals(key))
            {
                attributes.put(EmbedAttribute.MIME_TYPE, EmbedMIMEType.getMimeTypeFromMimeString(value));
            }
            else if (EmbedAttribute.COMPRESSION.equals(key))
            {
                attributes.put(EmbedAttribute.COMPRESSION, Boolean.parseBoolean(value));
            }
            else if (EmbedAttribute.ENCODING.equals(key))
            {
                attributes.put(EmbedAttribute.ENCODING, value);
            }
            else if (EmbedAttribute.EXPORT_SYMBOL.equals(key))
            {
                attributes.put(EmbedAttribute.EXPORT_SYMBOL, value);
            }
            else if (EmbedAttribute.FLASH_TYPE.equals(key))
            {
                attributes.put(EmbedAttribute.FLASH_TYPE, Boolean.parseBoolean(value));
            }
            else if (EmbedAttribute.ORIGINAL.equals(key))
            {
                attributes.put(EmbedAttribute.ORIGINAL, value);
            }
            else if (EmbedAttribute.QUALITY.equals(key))
            {
                double doubleValue = Double.parseDouble(value);
                if (doubleValue < 0 || doubleValue > 100)
                {
                    problems.add(new EmbedQualityValueProblem(location, doubleValue));
                    hadError = true;
                }
                else
                {
                    Float floatValue = (float)(doubleValue / 100.0);
                    attributes.put(EmbedAttribute.QUALITY, floatValue);
                }
            }
            else if (EmbedAttribute.SCALE_GRID_BOTTOM.equals(key))
            {
                Integer intValue = Integer.parseInt(value);
                if (intValue.intValue() < 0)
                {
                    problems.add(new EmbedScalingGridValueProblem(location, EmbedAttribute.SCALE_GRID_BOTTOM, intValue.intValue()));
                    hadError = true;
                }
                else
                {
                    intValue *= ISWFConstants.TWIPS_PER_PIXEL;
                    attributes.put(EmbedAttribute.SCALE_GRID_BOTTOM, intValue);
                }
            }
            else if (EmbedAttribute.SCALE_GRID_LEFT.equals(key))
            {
                Integer intValue = Integer.parseInt(value);
                if (intValue.intValue() < 0)
                {
                    problems.add(new EmbedScalingGridValueProblem(location, EmbedAttribute.SCALE_GRID_LEFT, intValue.intValue()));
                    hadError = true;
                }
                else
                {
                    intValue *= ISWFConstants.TWIPS_PER_PIXEL;
                    attributes.put(EmbedAttribute.SCALE_GRID_LEFT, intValue);
                }
            }
            else if (EmbedAttribute.SCALE_GRID_RIGHT.equals(key))
            {
                Integer intValue = Integer.parseInt(value);
                if (intValue.intValue() < 0)
                {
                    problems.add(new EmbedScalingGridValueProblem(location, EmbedAttribute.SCALE_GRID_RIGHT, intValue.intValue()));
                    hadError = true;
                }
                else
                {
                    intValue *= ISWFConstants.TWIPS_PER_PIXEL;
                    attributes.put(EmbedAttribute.SCALE_GRID_RIGHT, intValue);
                }
            }
            else if (EmbedAttribute.SCALE_GRID_TOP.equals(key))
            {
                Integer intValue = Integer.parseInt(value);
                if (intValue.intValue() < 0)
                {
                    problems.add(new EmbedScalingGridValueProblem(location, EmbedAttribute.SCALE_GRID_TOP, intValue.intValue()));
                    hadError = true;
                }
                else
                {
                    intValue *= ISWFConstants.TWIPS_PER_PIXEL;
                    attributes.put(EmbedAttribute.SCALE_GRID_TOP, intValue);
                }
            }
            else if (EmbedAttribute.SKIN_CLASS.equals(key))
            {
                attributes.put(EmbedAttribute.SKIN_CLASS, value);
                if (value == null || value.length() == 0)
                {
                    //problems.add(new EmbedNoSkinClassProblem(location));
                    hadError = true;
                }

                if (!attributes.containsKey(EmbedAttribute.MIME_TYPE))
                {
                    attributes.put(EmbedAttribute.MIME_TYPE, EmbedMIMEType.SKIN);
                }

                // resolve the skin class here, as need to resolve against
                // a specific project
                IDefinition skinSymbol = project.resolveQNameToDefinition(value);
                if (skinSymbol == null)
                {
                    //problems.add(new EmbedNoSkinClassProblem(location));
                    hadError = true;                    
                }
                else
                {
                    // set the file from which the symbol came from to the source
                    // so we can still detect whether EmbedDatas are equal if
                    // a symbol resolves to a different class depending on the project
                    String source = skinSymbol.getContainingFilePath();
                    attributes.put(EmbedAttribute.SOURCE, source);

                    assert (skinSymbol instanceof ClassDefinition);
                    skinClassInfo = new SkinClassInfo(project, (ClassDefinition)skinSymbol);
                }
            }
            else if (EmbedAttribute.SMOOTHING.equals(key))
            {
                attributes.put(EmbedAttribute.SMOOTHING, Boolean.parseBoolean(value));
            }
            else if (EmbedAttribute.SYMBOL.equals(key))
            {
                attributes.put(EmbedAttribute.SYMBOL, value);
            }
            else if (EmbedAttribute.ADV_ANTI_ALIASING.equals(key) ||
                    EmbedAttribute.EMBED_AS_CFF.equals(key) ||
                    EmbedAttribute.UNICODE_RANGE.equals(key) ||
                    EmbedAttribute.FONT_FAMILY.equals(key) ||
                    EmbedAttribute.FONT_NAME.equals(key) ||
                    EmbedAttribute.FONT_STYLE.equals(key) ||
                    EmbedAttribute.FONT_WEIGHT.equals(key) ||
                    EmbedAttribute.SYSTEM_FONT.equals(key) ||
                    EmbedAttribute.SOURCE_LIST.equals(key))
            {
                // silently ignore these, as proper problem will be reported elsewhere
            }
            else
            {
                problems.add(new EmbedUnknownAttributeProblem(location, key));
                hadError = true;
            }
        }
        catch (NumberFormatException e)
        {
            problems.add(new EmbedInvalidAttributeValueProblem(location, key, value));
            hadError = true;
        }

        return hadError;
    }

    /**
     * Returns the value of an attribute.
     * 
     * @param attribute An embed attribute.
     * @return value of an attribute.  null if attribute does not exist
     */
    public Object getAttribute(EmbedAttribute attribute)
    {
        return attributes.get(attribute);
    }

    /**
     * @return All attributes
     */
    public EmbedAttribute[] getAttributes()
    {
        return attributes.keySet().toArray(new EmbedAttribute[attributes.size()]);
    }

    /**
     * @param project The compiler project.
     * @param location The source location.
     * @param problems The colleciton of compiler projects to which this method should add problems.
     * @return true if the transcoder was successfully constructed
     */
    public boolean createTranscoder(ICompilerProject project, ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        // there should always be a source, with the exception of skin embedding, so don't
        // create a transcoder in this error state
        String source = (String)getAttribute(EmbedAttribute.SOURCE);
        if (source == null && getAttribute(EmbedAttribute.SKIN_CLASS) == null)
        {
            problems.add(new EmbedNoSourceAttributeProblem(location));
            return false;
        }
        String uniqueName = source;
        List<File> sourcePaths = ((IASProject)project).getSourcePath();
        for (File sourcePath : sourcePaths)
        {
        	String sourcePathString = sourcePath.getAbsolutePath();
        	if (source.startsWith(sourcePathString))
        	{
        		uniqueName = source.substring(sourcePathString.length());
        		uniqueName = uniqueName.replace("\\", "/");
        		break;
        	}
        }

        // also check that we have a mimetype set, as don't know what transcoder
        // to create without it!
        EmbedMIMEType mimeType = (EmbedMIMEType)getAttribute(EmbedAttribute.MIME_TYPE);
        if (mimeType == null)
        {
            problems.add(new EmbedUnrecogniedFileTypeProblem(location, source));
            return false;
        }

        Workspace workspace = (Workspace)project.getWorkspace();
        switch (mimeType)
        {
            case JPEG:
            case JPG:
            case PNG:
            case GIF:
            {
                Boolean compression = (Boolean)getAttribute(EmbedAttribute.COMPRESSION);
                Float quality = (Float)getAttribute(EmbedAttribute.QUALITY);
                if ((compression != null && compression == true) || quality != null)
                {
                    transcoder = new JPEGTranscoder(this, workspace);
                }
                else
                {
                    transcoder = new ImageTranscoder(this, workspace);
                }
                break;
            }
            case MP3:
            {
                transcoder = new SoundTranscoder(this, workspace);
                break;
            }
            case FLASH:
            {
                transcoder = new MovieTranscoder(this, workspace);
                break;
            }
            case PBJ:
            {
                transcoder = new PBJTranscoder(this, workspace);
                break;
            }
            case OCT_STRM:
            {
                transcoder = new DataTranscoder(this, workspace);
                break;
            }
            case XML:
            {
                transcoder = new XMLTranscoder(this, workspace);
                break;
            }
            case SKIN:
            {
                //transcoder = new SkinTranscoder(this, workspace, skinClassInfo);
                break;
            }
            case TTF:
            case TTC:
            case OTF:
            case FONT:
            case DFONT:
            {
                problems.add(new FontEmbeddingNotSupported(location));
                transcoder = null;
                break;
            }
            case TEXT:
            case PROPERTIES:
            	break;  // don't need transcoder for text
            default:
            {
                problems.add(new EmbedUnknownMimeTypeProblem(location, mimeType));
                transcoder = null;
            }
        }

        if (transcoder == null)
            return false;

        transcoder.hashCodeSourceName = uniqueName;
        
        // there were problems with the transcoder because of attribute settings
        // so don't return it, and let the user deal with the errors
        if (!transcoder.analyze(location, problems))
        {
            transcoder = null;
            return false;
        }

        return true;
    }

    /**
     * Returns the qname of the class generated from the EmbedData.  The name
     * is guaranteed to be unique and not conflict with user space names, unless
     * a user defined class has been decorated with the embed metadata, in which
     * case, the users class name will be returned.
     * @return qname
     */
    public String getQName()
    {
        if (specifiedName != null)
            return specifiedName;

        String source = (String)getAttribute(EmbedAttribute.SOURCE);
        if (swcSource != null)
        {
            source = EMBED_SWC_SEP.concat(source);
            source = swcSource.getContainingSWCPath().concat(source);
        }

        String uniqueName = source;
        List<File> sourcePaths = ((IASProject)project).getSourcePath();
        for (File sourcePath : sourcePaths)
        {
        	String sourcePathString = sourcePath.getAbsolutePath();
        	if (source.startsWith(sourcePathString))
        	{
        		uniqueName = source.substring(sourcePathString.length());
        		uniqueName = uniqueName.replace("\\", "/");
        		break;
        	}
        }
        String filename = FilenameUtils.getName(source);
        filename = filename.replace(".", "_");
        String qname = filename + "$" + StringEncoder.stringToMD5String(uniqueName);
        transcoder.hashCodeSourceName = uniqueName;
//        System.out.println("Embed UniqueName: " + uniqueName);
//        System.out.println("Embed QName: " + qname);
        
        // add the transcoder hashCode to the end of the QName to ensure
        // two embed data's with the same source, but different attributes
        // don't clash
        qname += transcoder.hashCode();

        return qname;
    }

    /**
     * Check if the generated class extends another
     * 
     * @return true if another class is extended
     */
    public boolean generatedClassExtendsAnother()
    {
        String baseClassQname = transcoder.getBaseClassQName();
        if (baseClassQname.isEmpty())
            return false;

        return true;
    }

    /**
     * Get the transcoder used by this embed.  This can be null if there was
     * a problem with the Embed directive
     * 
     * @return transcoder
     */
    public final ITranscoder getTranscoder()
    {
        return transcoder;
    }

    /**
     * 
     * @return ISWCFileEntry entry to source asset contained within swc.  null if not contained within SWC
     */
    public final ISWCFileEntry getSWCSource()
    {
        return swcSource;
    }

    @Override
    public boolean equals(Object o)
    {
        assert (transcoder != null) : "equals called on EmbedData with null transcoder";

        if (!(o instanceof EmbedData))
            return false;

        // EmbedData's are considered equal if their transcoders are equal
        return transcoder.equals(((EmbedData)o).getTranscoder());
    }

    @Override
    public int hashCode()
    {
        assert (transcoder != null) : "hashCode called on EmbedData with null transcoder";
        return transcoder.hashCode();
    }

    private String getResolvedSourcePath(ICompilerProject project, ISourceLocation location, String sourceValue, Collection<ICompilerProblem> problems)
    {
        if (sourceValue == null || sourceValue.isEmpty())
        {
            problems.add(new EmbedNoSourceAttributeProblem(location));
            return null;
        }

        Map<String,String> searchedLocations = new LinkedHashMap<String,String>();
        String containingSourcePath = new File(containingSourceFilename).getParent();
        String sourceFile = getResolvedSourcePath(project, containingSourcePath,
                sourceValue, searchedLocations);
        if (sourceFile == null)
        {
            problems.add(new EmbedSourceAttributeDoesNotExistProblem(location, 
                    sourceValue, searchedLocations));
        }

        return sourceFile;
    }

    /**
     * Resolve the location to the requested embed asset filename based on the rules.
     * 1) Absolute filename
     * 2) relative to the containing source file
     * 3) the source path (if flash project)
     * 4) the library path (if flash project)
     * @param containingSourcePath
     * @param filename
     * @param searchedLocations A map of the locations searched for filename. The
     * key is the filename and the value is the id of the message format used to 
     * format the filename.
     * @return The absolute path to the requested filename, or null if not found.
     */
    private String getResolvedSourcePath(ICompilerProject project, 
            String containingSourcePath, String filename,
            Map<String,String> searchedLocations)
    {
        // first check if absolute path
        String sourceFile = null;
        if (new File(filename).isAbsolute())
        {
            searchedLocations.put(FilenameNormalization.normalize(filename), "QuotedPath");
            sourceFile = SourcePathManager.getSourceFileInPath(null, filename);
            if (sourceFile != null)
                return sourceFile;
        }

        // not an absolute path, so try relative to the containing source
        if (containingSourcePath != null)
        {
            File file = new File(containingSourcePath, filename);
            searchedLocations.put(FilenameNormalization.normalize(file).getAbsolutePath(),
                    "QuotedPath");
            sourceFile = SourcePathManager.getSourceFileInPath(new File(containingSourcePath), filename);
        }

        if (sourceFile != null)
            return sourceFile;

        if (project instanceof ASProject)
        {
            sourceFile = getResolvedSourcePath((ASProject)project, filename,
                    searchedLocations);
        }
        if (project instanceof RoyaleProject)
        {
            RoyaleProject royaleProject = (RoyaleProject) project;
            String packagePath = null;
            if((containingSourcePath != null) && !royaleProject.getSourcePath().isEmpty()) {
                for (File sourcePath : royaleProject.getSourcePath()) {
                	if (containingSourcePath.equals(sourcePath.getAbsolutePath()))
                	{
                		packagePath = "";
                		break;
                	}
                    if (containingSourcePath.startsWith(sourcePath.getAbsolutePath())) {
                        packagePath = containingSourcePath.substring(sourcePath.getAbsolutePath().length() + 1);
                        break;
                    }
                }
            }
            sourceFile = getResolvedSourcePath((RoyaleProject)project, filename, packagePath,
                    searchedLocations);
        }

        return sourceFile;
    }

    private String getResolvedSourcePath(ASProject project, String filename,
            Map<String,String> searchedLocations)
    {
        // Only files that start with a leading "/" are resolved using the 
        // source path.
        String sourceFile = null;
        boolean isAbsolute = filename.startsWith("/");  
        if (isAbsolute)
        {
            searchedLocations.put(filename.substring(1), "EmbedOnSourcePath");
            sourceFile = project.getSourceFileFromSourcePath(filename.substring(1));
        }
        
        if (sourceFile != null)
            return sourceFile;

        // Not in the source path, so finally look for the file within the libraries.
        // Absolute files are not looked up using the library path.
        if (!isAbsolute)
        {
            searchedLocations.put(filename, "EmbedOnLibraryPath");
            swcSource = project.getSourceFileFromLibraryPath(filename);
            if (swcSource != null)
            {
                sourceFile = swcSource.getPath();
            }            
        }

        return sourceFile;
    }

    private String getResolvedSourcePath(RoyaleProject project, String filename, String packagePath,
                                         Map<String,String> searchedLocations)
    {
        // Only files that start with a leading "/" are resolved using the
        // source path.
        String sourceFile = null;
        boolean isAbsolute = filename.startsWith("/");
        if (isAbsolute)
        {
            searchedLocations.put(filename.substring(1), "EmbedOnSourcePath");
            sourceFile = project.getSourceFileFromSourcePath(filename.substring(1));
        }

        if (sourceFile != null)
            return sourceFile;

        // Not in the source path, so finally look for the file within the libraries.
        // Absolute files are not looked up using the library path.
        if (!isAbsolute)
        {
            for(File sourceDirectory : project.getSourcePath()) {
                File potentialFile = sourceDirectory;
                if(packagePath != null) {
                    potentialFile = new File(potentialFile, packagePath);
                }
                searchedLocations.put(filename, potentialFile.getAbsolutePath());
                potentialFile = new File(potentialFile, filename);
                if(potentialFile.exists()) {
                    return potentialFile.getAbsolutePath();
                }
            }
            searchedLocations.put(filename, "EmbedOnLibraryPath");
            swcSource = project.getSourceFileFromLibraryPath(filename);
            if (swcSource != null)
            {
                sourceFile = swcSource.getPath();
            }
        }

        return sourceFile;
    }
}
