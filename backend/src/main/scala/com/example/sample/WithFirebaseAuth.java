package com.example.sample;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Implementation restriction: subclassing Classfile does not
 * make your annotation visible at runtime.  If that is what
 * you want, you must write the annotation class in Java.
 * final class WithFirebaseAuth extends scala.annotation.ClassfileAnnotation
 *
 * なのでjavaファイル
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface WithFirebaseAuth {
}
