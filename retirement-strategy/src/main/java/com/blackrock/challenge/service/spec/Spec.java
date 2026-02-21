package com.blackrock.challenge.service.spec;

public interface Spec<T> {
  boolean ok(T t);
  String reason();

  default Spec<T> and(Spec<T> other) {
    return new AndSpec<>(this, other);
  }
}
