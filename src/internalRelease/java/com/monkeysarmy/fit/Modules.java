package com.monkeysarmy.fit;

final class Modules {
  static Object[] list(MonkeyApp app) {
    return new Object[] {
        new MonkeyModule(app),
        new InternalReleaseMonkeyModule()
    };
  }

  private Modules() {
    // No instances.
  }
}
