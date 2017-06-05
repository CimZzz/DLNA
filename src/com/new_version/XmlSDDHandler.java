package com.new_version;

import com.new_version.tools.XmlAnalyzeStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class XmlSDDHandler extends DefaultHandler {
    private final Service service;
    private final XmlAnalyzeStream.AnalyzerChain analyzerChain;

    private Action action;
    private boolean isInArgument;
    private HashMap<String,Argument> argumentPool;
    private HashMap<String,StateVariable> stateVariablePool;

    private String argumentName;
    private String argumentRelatedVar;

    private String varSendEvents;
    private String varName;
    private String varDataType;
    private String varDefaultValue;
    private List<String> varAllowList;
    private HashMap<String,String> varAllowRange;

    XmlSDDHandler(Service service) {
        this.service = service;
        this.service.actionMap = new HashMap<>();
        this.service.stateVariableMap = new HashMap<>();
        argumentPool = new HashMap<>();
        stateVariablePool = new HashMap<>();

        XmlAnalyzeStream sepcVersion;
        XmlAnalyzeStream actionList;
        XmlAnalyzeStream actionStream;
        XmlAnalyzeStream argumentList;
        XmlAnalyzeStream argumentStream;
        XmlAnalyzeStream serviceStateTable;
        XmlAnalyzeStream stateVariableStream;
        XmlAnalyzeStream allowValueList;
        XmlAnalyzeStream allowValueRange;
        XmlAnalyzeStream root = new XmlAnalyzeStream("scpd",false)
                .addChildElement(sepcVersion = new XmlAnalyzeStream("specVersion",false))
                .addChildElement(actionList = new XmlAnalyzeStream("actionList",false))
                .addChildElement(serviceStateTable = new XmlAnalyzeStream("serviceStateTable",false));

        sepcVersion
                .addChildElement(new XmlAnalyzeStream("major",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        service.specMajorVersion = Integer.parseInt(value);
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("minor",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        service.specMajorVersion = Integer.parseInt(value);
                    }
                }));

        actionList
                .addChildElement(actionStream = new XmlAnalyzeStream("action",true,new XmlAnalyzeStream.OnElementCallback(){
                    @Override
                    public void onElementStart(Attributes attributes) {
                        action = new Action();
                    }

                    @Override
                    public void onElementEnd() {
                        service.actionMap.put(action.name,action);
                    }
                }));

        actionStream
                .addChildElement(new XmlAnalyzeStream("name",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        action.name = value;
                    }
                }))
                .addChildElement(argumentList = new XmlAnalyzeStream("argumentList",true));

        argumentList
                .addChildElement(argumentStream = new XmlAnalyzeStream("argument",true,new XmlAnalyzeStream.OnElementCallback(){
                    @Override
                    public void onElementEnd() {
                        Argument argument = argumentPool.get(argumentName);
                        if(argument == null) {
                            argument = new Argument();
                            argument.name = argumentName;
                            argument.variable = stateVariablePool.get(argumentRelatedVar);
                            if(argument.variable == null)
                                stateVariablePool.put(argumentRelatedVar,argument.variable = new StateVariable());
                        }
                        if(isInArgument)
                            action.inArgumentList.add(argument);
                        else action.outArgumentList.add(argument);
                    }
                }));

        argumentStream
                .addChildElement(new XmlAnalyzeStream("name",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        argumentName = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("direction",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        isInArgument = value.equals("in");
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("relatedStateVariable",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        argumentRelatedVar = value;
                    }
                }));

        serviceStateTable
                .addChildElement(stateVariableStream = new XmlAnalyzeStream("stateVariable",true,new XmlAnalyzeStream.OnElementCallback(){
                    @Override
                    public void onElementStart(Attributes attributes) {
                        varSendEvents = attributes.getValue("sendEvents");
                    }

                    @Override
                    public void onElementEnd() {
                        StateVariable variable = stateVariablePool.get(varName);
                        if(variable == null)
                            variable = new StateVariable();

                        variable.name = varName;
                        variable.dataType = varDataType;
                        variable.sendEvents = varSendEvents;
                        variable.defaultValue = varDefaultValue;
                        variable.allowedValueList = varAllowList;
                        variable.allowedValueRange = varAllowRange;
                        varAllowList = null;
                        varAllowRange = null;
                        service.stateVariableMap.put(varName,variable);
                    }
                }));

        stateVariableStream
                .addChildElement(new XmlAnalyzeStream("name",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        varName = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("dataType",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        varDataType = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("defaultValue",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        varDefaultValue = value;
                    }
                }))
                .addChildElement(allowValueList = new XmlAnalyzeStream("allowedValueList",true,new XmlAnalyzeStream.OnElementCallback(){
                    @Override
                    public void onElementStart(Attributes attributes) {
                        varAllowList = new LinkedList<>();
                    }
                }))
                .addChildElement(allowValueRange = new XmlAnalyzeStream("allowedValueRange",true,new XmlAnalyzeStream.OnElementCallback(){
                    @Override
                    public void onElementStart(Attributes attributes) {
                        varAllowRange = new HashMap<>();
                    }
                }));


        allowValueList
                .addChildElement(new XmlAnalyzeStream("allowedValue",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        varAllowList.add(value);
                    }
                }));

        allowValueRange
                .addChildElement(new XmlAnalyzeStream.DefaultElement(true,new XmlAnalyzeStream.OnElementCallback(true) {
                    @Override
                    public void onElementValue(String value) {
                        varAllowRange.put(getElementName(),value);
                    }
                }));

        analyzerChain = new XmlAnalyzeStream.AnalyzerChain(root);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        analyzerChain.startElement(qName,attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        analyzerChain.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        analyzerChain.endElement(qName);
    }
}
