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

package org.apache.royale.compiler.tree;

/**
 * Constants used to identify AST nodes to the BURM.
 */
public enum ASTNodeID
{
    UnknownID(null),

    AccessorID(null),
    AnonymousFunctionID(null),
    ArgumentID(null),
    ArgumentRestID("..."),
    ArrayIndexExpressionID(null),
    ArrayLiteralID(null),
    AssignmentExpressionID("="),
    BaseDefinitionID(null),
    BaseLiteralContainerID(null),
    BaseProjectRootID(null),
    BaseStatementExpressionID(null),
    BaseStatementID(null),
    BaseTypedDefinitionID(null),
    BaseVariableID("var"),
    BasicMetaID("metadata"),
    BindableVariableID("var"),
    BlockID("{"),
    BreakID("break"),
    CatchID("catch"),
    ClassID("class"),
    ClassReferenceID("class"),
    ComponentBlockID("{"),
    ConditionalID("?"),
    ConfigBlockID("CONFIG"),
    ContainerID(null),
    ContinueID("continue"),
    DefaultID("default"),
    DefaultXMLStatementID(null),
    DoWhileLoopID("do"),
    DynamicID("dynamic"),
    E4XFilterID(".("),
    ElseID("else"),
    EmbedID("embed"),
    EventTagID("event"),
    ExpressionID(null),
    ExternalProjectRootID(null),
    EventTriggerTagID(null),
    FXGFileID(null),
    FileID(null),
    FinalID("final"),
    FinallyID("finally"),
    FoldedExpressionID(null),
    ForLoopID("for"),
    ForEachLoopID("for"),
    FullNameID(null),
    FunctionCallID(null),
    FunctionID("function"),
    FunctionObjectID(null),
    GetterID("get"),
    GotoID("goto"),
    IdentifierID(null),
    IfStatementID("if"),
    ImportID("import"),
    IncludeContainerID("include"),
    InspectableTagID(null),
    InstructionListID(null),
    InterfaceID("interface"),
    KeywordDefaultXMLNamespaceID("default xml namespace"),
    KeywordConstID("const"),
    KeywordExtendsID("extends"),
    KeywordFunctionID("function"),
    KeywordImplementsID("implements"),
    KeywordGetID("get"),
    KeywordNewID("new"),
    KeywordSetID("set"),
    KeywordVarID("var"),
    KeywordClassID("class"),
    KeywordInterfaceID("interface"),
    LabledStatementID(null),
    LeafID(null),
    LiteralID(null),
    LiteralArrayID(null),
    LiteralBooleanID(null),
    LiteralIntegerID(null),
    LiteralIntegerZeroID(null),
    LiteralDoubleID(null),
    LiteralNullID("null"),
    LiteralNumberID(null),
    LiteralObjectID(null),
    LiteralRegexID(null),
    LiteralStringID(null),
    LiteralVoidID("void"),
    LiteralUintID(null),
    LiteralXMLID(null),
    LoopID(null),
    MemberAccessExpressionID(null),
    MemberedID(null),
    MetaTagID(null),
    MetaTagsID(null),
    ModifierID(null),
    ModifiersSetID(null),
    NativeID("native"),
    NamespaceAccessExpressionID(null),
    NamespaceIdentifierID(null),
    NamespaceID(null),
    NilID(null),
    NodeBaseID(null),
    NonResolvingIdentifierID(null),
    ObjectLiteralExpressionID(null),
    ObjectLiteralValuePairID(null),
    OperatorExpressionID(null),
    Op_EqualID("=="),
    Op_LessThanID("<"),
    Op_MultiplyID("*"),
    Op_ModuloID("%"),
    Op_DivideID("/"),
    Op_AddID("+"),
    Op_SubtractID("-"),
    Op_PreDecrID("--"),
    Op_PreIncrID("++"),
    Op_PostIncrID("++"),
    Op_PostDecrID("--"),
    Op_ColonID(":"),
    Op_ConditionalID("?"),
    Op_CommaID(","),
    Op_LeftShiftID("<<"),
    Op_LeftShiftAssignID("<<="),
    Op_RightShiftID(">>"),
    Op_RightShiftAssignID(">>="),
    Op_UnsignedRightShift(">>>"),
    Op_UnsignedRightShiftAssignID(">>>="),
    Op_DeleteID("delete"),
    Op_DescendantsID(".."),
    Op_TypeOfID("typeof"),
    Op_MultiplyAssignID("*="),
    Op_MemberAccessID(null),
    Op_AsID("as"),
    Op_IsID("is"),
    Op_InID("in"),
    Op_InstanceOfID("instanceof"),
    Op_AssignId("="),
    Op_NamespaceAccessID(null),
    Op_DivideAssignID("/="),
    Op_ModuloAssignID("%="),
    Op_BitwiseAndID("&"),
    Op_BitwiseAndAssignID("&="),
    Op_BitwiseXorID("^"),
    Op_BitwiseXorAssignID("^="),
    Op_BitwiseOrID("|"),
    Op_BitwiseOrAssignID("|="),
    Op_AddAssignID("+="),
    Op_SubtractAssignID("-="),
    Op_LogicalNotID("!"),
    Op_BitwiseNotID("~"),
    Op_GreaterThanID(">"),
    Op_GreaterThanEqualsID(">="),
    Op_LessThanEqualsID("<="),
    Op_StrictEqualID("==="),
    Op_NotEqualID("!="),
    Op_StrictNotEqualID("!=="),
    Op_LogicalAndID("&&"),
    Op_LogicalAndAssignID("&&="),
    Op_LogicalOrID("||"),
    Op_LogicalOrAssignID("||="),
    Op_AtID("at"),
    Op_FilterID(".("),
    Op_VoidID("void"),
    OverrideID("override"),
    PackageID("package"),
    PropertiesFileID(null),
    PropertiesEntryID(null),
    ProjectRootID(null),
    ProjectUnitID(null),
    LRUCacheID(null),
    QualifiedNameExpressionID(null),
    QualifiedNamespaceExpressionID(null),
    ReturnStatementID("return"),
    ResourceBundleTagID(null),
    SWCFileID(null),
    SWCNamespaceIdentifierID(null),
    SetterID("set"),
    SharedMetaTagsID(null),
    StaticID("static"),
    StopID("stop"),
    SuperID("super"),
    SwitchID("switch"),
    TerminalID(null),
    TernaryExpressionID("?"),
    ThisID("this"),
    ThrowsStatementID("throw"),
    TransparentContainerID(null),
    TreeID(null),
    TryID("try"),
    TypeID(null),
    TypedExpressionID(null),
    UseID("use"),
    VariableExpressionID(null),
    VariableID("var"),
    VectorInformationID(null),
    FunctionInfoID(null),
    ArgumentInfoID(null),
    VectorLiteralID(null),
    VectorID(null),
    WhileLoopID("while"),
    WithID("with"),
    XMLContentID(null),
    XMLListContentID(null),
    RuntimeNameExpressionID(null),
    VoidID("void"),

    /// MXML-specific nodes
    MXMLApplicationID(null, true),
    MXMLArrayID(null, true),
    MXMLBindingID(null, true),
    MXMLBindingAttributeID(null, true),
    MXMLBooleanID(null, true),
    MXMLClassID(null, true),
    MXMLClassDefinitionID(null, true),
    MXMLClearID(null, true),
    MXMLComponentID(null, true),
    MXMLConcatenatedDataBindingID(null, true),
    MXMLDataBindingID(null, true),
    MXMLDateID(null, true),
    MXMLDeclarationsID(null, true),
    MXMLDeferredInstanceID(null, true),
    MXMLDefinitionID(null, true),
    MXMLDesignLayerID(null, true),
    MXMLDocumentID(null, true),
    MXMLEmbedID(null, true),
    MXMLEffectSpecifierID(null, true),
    MXMLEventSpecifierID(null, true),
    MXMLFactoryID(null, true),
    MXMLFileID(null, true),
    MXMLFunctionID(null, true),
    MXMLHTTPServiceID(null, true),
    MXMLHTTPServiceRequestID(null, true),
    MXMLImplementsID(null, true),
    MXMLInstanceID(null, true),
    MXMLIntID(null, true),
    MXMLLibraryID(null, true),
    MXMLLiteralID(null, true),
    MXMLMetadataID(null, true),
    MXMLModelID(null, true),
    MXMLModelPropertyID(null, true),
    MXMLModelRootID(null, true),
    MXMLNumberID(null, true),
    MXMLObjectID(null, true),
    MXMLPrivateID(null, true),
    MXMLPropertySpecifierID(null, true),
    MXMLRegExpID(null, true),
    MXMLRemoteObjectID(null, true),
    MXMLRemoteObjectMethodID(null, true),
    MXMLReparentID(null, true),
    MXMLRepeaterID(null, true),
    MXMLResourceID(null, true),
    MXMLScriptID(null, true),
    MXMLStateID(null, true),
    MXMLStringID(null, true),
    MXMLStyleID(null, true),
    MXMLStyleSpecifierID(null, true),
    MXMLUintID(null, true),
    MXMLVectorID(null, true),
    MXMLWebServiceID(null, true),
    MXMLWebServiceOperationID(null, true),
    MXMLXMLID(null, true),
    MXMLXMLListID(null, true),

    InvalidNodeID(null);

    private boolean isMXMLNode = false;

    /**
     * Construct an ASTNodeID with a paraphrase.
     * 
     * @param paraphrase - the paraphrase expression for this ID.
     */
    ASTNodeID(String paraphrase)
    {
        this.paraphrase = paraphrase;
    }

    ASTNodeID(String paraphrase, boolean isMXMLNode)
    {
        this.paraphrase = paraphrase;
        this.isMXMLNode = isMXMLNode;
    }

    /**
     * The explicit paraphrase, if one was provided.
     */
    private String paraphrase;

    /**
     * Get a paraphrase expression for this node ID. If the enum constructor
     * supplied an explicit paraphrase, use that; otherwise return a cleaned up
     * version of the enum itself.
     */
    public String getParaphrase()
    {
        String result;

        if (this.paraphrase != null)
        {
            result = this.paraphrase;
        }
        else
        {
            result = this.toString();

            //  Make the text a little more readable
            //  by trimming jargon suffixes and prefixes.
            if (result.endsWith("ID"))
                result = result.substring(0, result.lastIndexOf("ID"));
            if (result.endsWith("Id"))
                result = result.substring(0, result.lastIndexOf("Id"));

            if (result.startsWith("Keyword"))
                result = result.substring(7);
            else if (result.startsWith("Op_"))
                result = result.substring(3);
        }

        return result;
    }
    
    public boolean isMXMLNode()
    {
        return isMXMLNode;
    }
   
}
