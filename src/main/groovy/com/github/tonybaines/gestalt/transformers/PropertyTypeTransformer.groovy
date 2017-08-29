package com.github.tonybaines.gestalt.transformers

import java.lang.reflect.Method

class PropertyTypeTransformer {

    Map<Class, Method> transformations

    static PropertyTypeTransformer from(Class transformerClass) {
        def tx = new PropertyTypeTransformer()
        tx.loadTransformationFunctions(transformerClass)
        tx
    }

    def loadTransformationFunctions(Class transformerClass) {
        def stringAcceptingMethods = transformerClass.declaredMethods.grep{ Method m ->
            m.parameters.length == 1 &&
                    m.parameters[0].type == String
        }

        this.transformations = stringAcceptingMethods.collectEntries { m ->
            [m.returnType, m]
        }
    }

    private PropertyTypeTransformer() {
    }

    def transform(String s, Class destClass) {
        transformations[destClass].invoke(null, s)
    }
}
