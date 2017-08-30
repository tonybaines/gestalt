package com.github.tonybaines.gestalt.transformers

import groovy.util.logging.Slf4j

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

@Slf4j
class PropertyTypeTransformer {
    Map<Class, Method> fromStringTransformations = [:]
    Map<Class, Method> toStringTransformations = [:]

    static PropertyTypeTransformer NULL = new PropertyTypeTransformer()

    static PropertyTypeTransformer from(Class transformerClass) {
        def tx = new PropertyTypeTransformer()
        tx.loadTransformationFunctions(transformerClass)
        tx.loadSerialisationFunctions(transformerClass)
        tx
    }

    private def singleMethodDefined = { it.value.size() == 1 }

    private def loadTransformationFunctions(Class transformerClass) {
        Map<Class, List<Method>> transformationCandidates =
                transformerClass
                        .declaredMethods
                        .grep { Method m -> Modifier.isStatic(m.modifiers)}
                        .grep { Method m ->
                    m.parameters.length == 1 &&
                            m.parameters[0].type == String
                }
                .groupBy { it.returnType }

        // Ignoring any type where more than one transformation function is defined
        transformationCandidates
                .findAll { !singleMethodDefined(it) }
                .forEach { type, methods ->
            log.warn("Ignoring duplicate transformation functions to ${type}: ${methods.collect { it.toString() }}")
        }

        this.fromStringTransformations = transformationCandidates
                .findAll { singleMethodDefined(it) }
                .collectEntries { [it.key, it.value.first()] }
    }

    /* Functions for turning an instance of a type into a String */
    private def loadSerialisationFunctions(Class transformerClass) {
        Map<Class, List<Method>> serialisationCandidates =
                transformerClass
                        .declaredMethods
                        .grep { Method m -> Modifier.isStatic(m.modifiers)}
                        .grep { Method m ->
                    m.parameters.length == 1 &&
                            m.returnType == String
                }
                .groupBy { it.parameters[0].type }

        // Ignoring any type where more than one persistence function is defined
        serialisationCandidates
                .findAll { !singleMethodDefined(it) }
                .forEach { type, methods ->
            log.warn("Ignoring duplicate custom serialisation functions from ${type}: ${methods.collect { it.toString() }}")
        }

        this.toStringTransformations = serialisationCandidates
                .findAll { singleMethodDefined(it) }
                .collectEntries { [it.key, it.value.first()] }
    }

    private PropertyTypeTransformer() {
    }

    def fromString(String s, Class destClass) {
        def transform = fromStringTransformations[destClass]
        try {
            transform?.invoke(null, s)
        } catch (InvocationTargetException e) {
            // Unwrap to rethrow the actual exception
            throw e.cause
        }
    }

    def toString(Object x) {
        def transform = toStringTransformations[x.class]
        try {
            transform?.invoke(null, x)
        } catch (InvocationTargetException e) {
            // Unwrap to rethrow the actual exception
            throw e.cause
        }
    }

    boolean hasTransformationTo(Class destinationType) {
        fromStringTransformations.containsKey(destinationType)
    }

    boolean hasTransformationFrom(Class sourceType) {
        toStringTransformations.containsKey(sourceType)
    }
}
