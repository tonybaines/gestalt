package com.github.tonybaines.gestalt.transformers

import groovy.util.logging.Slf4j

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

@Slf4j
class PropertyTypeTransformer {
    Map<Class, Method> transformations

    static PropertyTypeTransformer from(Class transformerClass) {
        def tx = new PropertyTypeTransformer()
        tx.loadTransformationFunctions(transformerClass)
        tx
    }

    def loadTransformationFunctions(Class transformerClass) {
        Map<Class, List<Method>> transformationCandidates =
                transformerClass
                        .declaredMethods
                        .grep { Method m -> Modifier.isStatic(m.modifiers)}
                        .grep { Method m ->
                    m.parameters.length == 1 &&
                            m.parameters[0].type == String
                }
                .groupBy { it.returnType }

        def singleMethodDefined = { it.value.size() == 1 }

        // Ignoring any type where more than one transformation function is defined
        transformationCandidates
                .findAll { !singleMethodDefined(it) }
                .forEach { type, methods ->
            log.warn("Ignoring duplicate transformation functions to ${type}: ${methods.collect { it.toString() }}")
        }

        this.transformations = transformationCandidates
                .findAll { singleMethodDefined(it) }
                .collectEntries { [it.key, it.value.first()] }
    }

    private PropertyTypeTransformer() {
    }

    def transform(String s, Class destClass) {
        def transform = transformations[destClass]
        try {
            transform?.invoke(null, s)
        } catch (InvocationTargetException e) {
            // Unwrap to rethrow the actual exception
            throw e.cause
        }
    }
}
