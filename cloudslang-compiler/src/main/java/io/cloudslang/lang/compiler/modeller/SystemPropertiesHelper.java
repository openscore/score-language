/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.modeller.transformers.AbstractInOutForTransformer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

public class SystemPropertiesHelper extends AbstractInOutForTransformer {

    private static final String OBJECT = "object";
    private static final String OBJECTS = "objects";
    private static final String OBJECT_VALUE = "value";
    private static final String CHILD_OBJECTS = "child_objects";
    private static final String OBJECT_PROPERTY = "property";
    private static final String OBJECT_PROPERTIES = "properties";


    public Set<String> getObjectRepositorySystemProperties(Map<String, Object> objectMap) {
        @SuppressWarnings("unchecked")
        List<Object> objects = (List<Object>) objectMap.get(OBJECTS);
        Set<String> systemProps = new HashSet<>();
        Stack<List<Object>> stack = new Stack<>();
        getSystemPropertiesObjRepo(objects, stack);
        stack.parallelStream()
                .forEach(stackElement -> systemProps.addAll(getSystemPropertyForObject(stackElement)));
        stack.clear();
        return systemProps;
        //stack.removeAllElements();
    }

    private Set<String> getSystemPropertyForObject(Object object) {
        Set<String> stringSet = new HashSet<>();
        if (object instanceof ArrayList) {
            for (Object elementList : (ArrayList) object) {
                LinkedHashMap valueMap = (LinkedHashMap) ((LinkedHashMap) elementList).get(OBJECT_PROPERTY);
                Accumulator accumulator = extractFunctionData(
                        (Serializable) ((LinkedHashMap) valueMap.get(OBJECT_VALUE)).get(OBJECT_VALUE));
                stringSet.addAll(accumulator.getSystemPropertyDependencies());
            }
        }
        return stringSet;
    }

    private void getSystemPropertiesObjRepo(List<Object> objRepository, Stack<List<Object>> stack) {

        if (objRepository != null) {
            for (Object object : objRepository) {
                @SuppressWarnings("unchecked") Map<String, Object> mappedObject =
                        (Map<String, Object>) ((LinkedHashMap) object).get(OBJECT);
                @SuppressWarnings("unchecked") List<Object> childObjects =
                        (List<Object>) mappedObject.get(CHILD_OBJECTS);
                @SuppressWarnings("unchecked") List<Object> properties =
                        (List<Object>) mappedObject.get(OBJECT_PROPERTIES);
                if (!childObjects.isEmpty()) {
                    getSystemPropertiesObjRepo(childObjects, stack);
                }
                stack.push(properties);
            }
        }
    }

    public Set<String> getSystemPropertiesFromSettings(Map<String, Object> objectMap) {
        Set<String> systemProps = new HashSet<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> sapSettings = (Map<String, Object>) objectMap.get("sap");
        findSystemPropertiesSapSettings(sapSettings, systemProps);
        @SuppressWarnings("unchecked")
        Map<String, Object> windowsSettings = (Map<String, Object>) objectMap.get("windows");
        findSystemPropertiesWindowsSettings(windowsSettings, systemProps);
        @SuppressWarnings("unchecked")
        Map<String, Object> webSettings = (Map<String, Object>) objectMap.get("web");
        findSystemPropertiesWebSettings(webSettings, systemProps);
        return systemProps;
    }

    private void findSystemPropertiesSapSettings(Map<String, Object> sapSettings,
            Set<String> systemProps) {
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("user")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("client")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("language")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("password")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("server")));
    }

    private void findSystemPropertiesWindowsSettings(Map<String, Object> windowsSettings,
            Set<String> systemProps) {
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> appType = (LinkedHashMap) windowsSettings.get("apps");
        if (MapUtils.isNotEmpty(appType)) {
            for (Object value : appType.values()) {
                systemProps.addAll(getSystemPropertyValue((String) ((LinkedHashMap) value).get("args")));
                systemProps.addAll(getSystemPropertyValue((String) ((LinkedHashMap) value).get("path")));
                systemProps.addAll(getSystemPropertyValue((String) ((LinkedHashMap) value).get("directory")));
            }
        }
    }

    private void findSystemPropertiesWebSettings(Map<String, Object> webSettings,
            Set<String> systemProps) {
        systemProps.addAll(getSystemPropertyValue((String) webSettings.get("address")));
        systemProps.addAll(getSystemPropertyValue((String) webSettings.get("browser")));
    }

    private Set<String> getSystemPropertyValue(String property) {
        Set<String> systemProperties = new HashSet<>();
        if (StringUtils.isNotEmpty(property)) {
            systemProperties = extractFunctionData(property).getSystemPropertyDependencies();
        }
        return systemProperties;
    }
}
    
