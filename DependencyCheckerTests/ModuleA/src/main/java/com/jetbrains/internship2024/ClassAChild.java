package com.jetbrains.internship2024;

public class ClassAChild extends ClassA{
    public void something() {
        ClassA obj = new ClassAChild();
        if(obj instanceof ClassAChild) {
            ClassAChild obj2 = (ClassAChild)obj;
        }
    }
}
