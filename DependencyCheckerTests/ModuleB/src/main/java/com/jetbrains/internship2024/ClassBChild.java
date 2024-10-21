package com.jetbrains.internship2024;

public class ClassBChild extends ClassA{
    public void something() {
        ClassA obj = new ClassBChild();
        if(obj instanceof ClassBChild) {
            ClassBChild obj2 = (ClassBChild)obj;
        }
    }
}