package com.jetbrains.internship2024.annotations;

public class TypeUse {
    public <@TypeUseAnnotation U> U castMethod(Object object) {
        return (U) object;
    }
}
