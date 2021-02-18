/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.PyObjectValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.services.ScriptsService;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.python.core.Py;
import org.python.core.PyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author stoneo
 * @version $Id$
 * @since 06/11/2014
 */
@Component
public class ScriptEvaluator extends ScriptProcessor {
    private static String LINE_SEPARATOR = "\n";
    private static final String SYSTEM_PROPERTIES_MAP = "sys_prop";
    private static final String ACCESSED_RESOURCES_SET = "accessed_resources_set";
    private static final String BACKWARD_COMPATIBLE_ACCESS_METHOD = "def accessed(key):" +
            LINE_SEPARATOR + "  pass";
    private static final boolean EXTERNAL_PYTHON = !Boolean.valueOf(
            System.getProperty("use.jython.expressions", "true"));


    public static final int MAX_LENGTH = Integer.getInteger("input.error.max.length", 1000);

    @Resource(name = "externalPythonRuntimeService")
    private PythonRuntimeService pythonRuntimeService;

    @Resource(name = "jythonRuntimeService")
    private PythonRuntimeService legacyJythonRuntimeService;

    @Autowired
    private ScriptsService scriptsService;

    public Value evalExpr(String expr, Map<String, Value> context, Set<SystemProperty> systemProperties,
                          Set<ScriptFunction> functionDependencies) {
        try {
            if (EXTERNAL_PYTHON) {
                return doEvaluateExpressionExternalPython(expr, context, systemProperties, functionDependencies);
            } else {
                return doEvaluateExpressionJython(expr, context, systemProperties, functionDependencies);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error in evaluating expression: '" +
                    getTruncatedExpression(expr) + "',\n\t" +
                    handleExceptionSpecialCases(exception.getMessage()), exception);
        }
    }

    private Value doEvaluateExpressionJython(String expr,
                                             Map<String, Value> context,
                                             Set<SystemProperty> systemProperties,
                                             Set<ScriptFunction> functionDependencies) {
        Map<String, Serializable> jythonContext = createJythonContext(context);
        boolean systemPropertiesDefined = functionDependencies.contains(ScriptFunction.GET_SYSTEM_PROPERTY);
        if (systemPropertiesDefined) {
            jythonContext.put(SYSTEM_PROPERTIES_MAP,
                    (Serializable) prepareSystemPropertiesForJython(systemProperties));
        }
        return processJythonEvaluation(expr, jythonContext, systemPropertiesDefined, functionDependencies);
    }

    private Value doEvaluateExpressionExternalPython(String expr,
                                                     Map<String, Value> context,
                                                     Set<SystemProperty> systemProperties,
                                                     Set<ScriptFunction> functionDependencies) {
        Map<String, Serializable> pythonContext = createExternalPythonContext(context);
        boolean systemPropertiesDefined = functionDependencies.contains(ScriptFunction.GET_SYSTEM_PROPERTY);
        if (systemPropertiesDefined) {
            pythonContext.put(SYSTEM_PROPERTIES_MAP,
                    (Serializable) prepareSystemPropertiesForExternalPython(systemProperties));
        }

        PythonEvaluationResult result = pythonRuntimeService.eval(
                buildAddFunctionsScriptForExternalPython(functionDependencies), expr, pythonContext);

        //noinspection unchecked
        Set<String> accessedResources = (Set<String>) result.getResultContext().get(ACCESSED_RESOURCES_SET);
        return ValueFactory.create(result.getEvalResult(), getSensitive(pythonContext, accessedResources));
    }

    public Value testExpr(String expr, Map<String, Value> context, Set<SystemProperty> systemProperties,
                          Set<ScriptFunction> functionDependencies, long timeoutPeriod) {
        try {
            if (EXTERNAL_PYTHON) {
                return doTestExternalPython(expr, context, systemProperties, functionDependencies, timeoutPeriod);

            } else {
                return doTestJython(expr, context, systemProperties, functionDependencies, timeoutPeriod);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error in evaluating expression: '" +
                    getTruncatedExpression(expr) + "',\n\t" +
                    handleExceptionSpecialCases(exception.getMessage()), exception);
        }
    }

    private Value doTestJython(String expr,
                               Map<String, Value> context,
                               Set<SystemProperty> systemProperties,
                               Set<ScriptFunction> functionDependencies,
                               long timeoutPeriod) {
        Map<String, Serializable> pythonContext = createJythonContext(context);
        boolean systemPropertiesDefined = functionDependencies.contains(ScriptFunction.GET_SYSTEM_PROPERTY);
        if (systemPropertiesDefined) {
            pythonContext.put(SYSTEM_PROPERTIES_MAP,
                    (Serializable) prepareSystemPropertiesForJython(systemProperties));
        }
        return processJythonExpressionTesting(expr, pythonContext, systemPropertiesDefined,
                functionDependencies, timeoutPeriod);
    }

    private Value doTestExternalPython(String expr,
                                       Map<String, Value> context,
                                       Set<SystemProperty> systemProperties,
                                       Set<ScriptFunction> functionDependencies,
                                       long timeoutPeriod) {
        Map<String, Serializable> pythonContext = createExternalPythonContext(context);
        boolean systemPropertiesDefined = functionDependencies.contains(ScriptFunction.GET_SYSTEM_PROPERTY);
        if (systemPropertiesDefined) {
            pythonContext.put(SYSTEM_PROPERTIES_MAP,
                    (Serializable) prepareSystemPropertiesForExternalPython(systemProperties));
        }
        PythonEvaluationResult result = pythonRuntimeService.test(
                buildAddFunctionsScriptForExternalPython(functionDependencies), expr, pythonContext,
                timeoutPeriod);

        //noinspection unchecked
        Set<String> accessedResources = (Set<String>) result.getResultContext().get(ACCESSED_RESOURCES_SET);
        return ValueFactory.create(result.getEvalResult(),
                getSensitive(pythonContext, accessedResources));
    }

    private String getTruncatedExpression(String expr) {
        return expr.length() > MAX_LENGTH ? expr.substring(0, MAX_LENGTH) + "..." : expr;
    }

    private String buildAddFunctionsScriptForExternalPython(Set<ScriptFunction> functionDependencies) {
        String functions = "";
        for (ScriptFunction function : functionDependencies) {
            functions += scriptsService.getScript(function);
            functions = appendDelimiterBetweenFunctions(functions);
        }
        return functions;
    }

    private String buildAddFunctionsScriptForJython(Set<ScriptFunction> functionDependencies) {
        String functions = "";
        for (ScriptFunction function : functionDependencies) {
            functions += scriptsService.getScript(function);
            functions = appendDelimiterBetweenFunctions(functions);
        }
        functions += BACKWARD_COMPATIBLE_ACCESS_METHOD;
        functions = appendDelimiterBetweenFunctions(functions);
        return functions;
    }

    private String appendDelimiterBetweenFunctions(String text) {
        return text + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    private Map<String, Value> prepareSystemPropertiesForExternalPython(Set<SystemProperty> properties) {
        Map<String, Value> processedSystemProperties = new HashMap<>();
        for (SystemProperty property : properties) {
            processedSystemProperties.put(property.getFullyQualifiedName(),
                    ValueFactory.createPyObjectValueForExternalPython(property.getValue()));
        }
        return processedSystemProperties;
    }

    private Map<String, Value> prepareSystemPropertiesForJython(Set<SystemProperty> properties) {
        Map<String, Value> processedSystemProperties = new HashMap<>();
        for (SystemProperty property : properties) {
            processedSystemProperties.put(property.getFullyQualifiedName(),
                    ValueFactory.createPyObjectValueForJython(property.getValue()));
        }
        return processedSystemProperties;
    }

    private String handleExceptionSpecialCases(String message) {
        String processedMessage = message;
        if (StringUtils.isNotEmpty(message) && message.contains("get_sp") && message.contains("not defined")) {
            processedMessage = message + ". Make sure to use correct syntax for the function:" +
                    " get_sp('fully.qualified.name', optional_default_value).";
        }
        return processedMessage;
    }

    private boolean getSensitive(Map<String, Serializable> fullContext, Set<String> accessedVariables) {
        if (CollectionUtils.isEmpty(accessedVariables)) {
            return false;
        }
        Collection<Serializable> accessedValues = fullContext.entrySet().stream()
                .flatMap(entry -> {
                    if (entry.getValue() instanceof Map) {
                        //noinspection unchecked
                        Map<String, Serializable> nestedContext = (Map<String, Serializable>) entry.getValue();
                        return nestedContext.entrySet().stream();
                    } else {
                        return Stream.of(entry);
                    }
                })
                .filter(entry -> accessedVariables.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        return checkSensitivity(accessedValues);
    }

    @Deprecated
    private boolean getSensitive(Map<String, Serializable> executionResultContext, boolean systemPropertiesInContext) {
        if (systemPropertiesInContext) {
            Map<String, Serializable> context = new HashMap<>(executionResultContext);
            PyObject rawSystemProperties = (PyObject) context.remove(SYSTEM_PROPERTIES_MAP);
            @SuppressWarnings("unchecked")
            Map<String, Value> systemProperties = Py.tojava(rawSystemProperties, Map.class);
            @SuppressWarnings("unchecked")
            Collection<Serializable> systemPropertyValues = (Collection) systemProperties.values();
            return checkSensitivity(systemPropertyValues) || checkSensitivity(context.values());
        } else {
            return (checkSensitivity(executionResultContext.values()));
        }
    }

    private boolean checkSensitivity(Collection<Serializable> values) {
        for (Serializable value : values) {
            if (value instanceof PyObjectValue) {
                PyObjectValue pyObjectValue = (PyObjectValue) value;
                if (pyObjectValue.isSensitive() && pyObjectValue.isAccessed()) {
                    return true;
                }
            }
        }
        return false;
    }

    //region Legacy Content
    private Value processJythonEvaluation(String expr, Map<String, Serializable> jythonContext,
                                          boolean systemPropertiesDefined,
                                          Set<ScriptFunction> functionDependencies) {
        PythonEvaluationResult result = legacyJythonRuntimeService.eval(
                buildAddFunctionsScriptForJython(functionDependencies), expr, jythonContext);
        if (systemPropertiesDefined) {
            jythonContext.remove(SYSTEM_PROPERTIES_MAP);
        }
        return ValueFactory.create(result.getEvalResult(), getSensitive(result.getResultContext(),
                systemPropertiesDefined));
    }

    private Value processJythonExpressionTesting(String expr, Map<String, Serializable> jythonContext,
                                                 boolean systemPropertiesDefined,
                                                 Set<ScriptFunction> functionDependencies, long timeoutPeriod) {
        PythonEvaluationResult result = legacyJythonRuntimeService.test(
                buildAddFunctionsScriptForJython(functionDependencies), expr, jythonContext,
                timeoutPeriod);
        if (systemPropertiesDefined) {
            jythonContext.remove(SYSTEM_PROPERTIES_MAP);
        }
        return ValueFactory.create(result.getEvalResult(), getSensitive(result.getResultContext(),
                systemPropertiesDefined));
    }
    //endregion
}
