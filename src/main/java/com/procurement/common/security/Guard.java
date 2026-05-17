package com.procurement.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Guard {
  @AliasFor("permissions")
  String[] value() default {};

  @AliasFor("value")
  String[] permissions() default {};

  String[] roles() default {};
}
