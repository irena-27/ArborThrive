package com.blackrock.challenge.service.spec;

public class AndSpec<T> implements Spec<T> {
  private final Spec<T> a;
  private final Spec<T> b;

  public AndSpec(Spec<T> a, Spec<T> b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public boolean ok(T t) {
    return a.ok(t) && b.ok(t);
  }

  @Override
  public String reason() {
    // reason is typically used when a check fails; call the failing spec's reason in validator
    return "AND";
  }
}
