package com.monkeysarmy.fit;

final class Modules {
  static Object[] list(MonkeyApp app) {
    return new Object[] {
        new MonkeyModule(app),
        new DebugMonkeyModule()
    };
  }

  private Modules() {
    // No instances.
  }
}
