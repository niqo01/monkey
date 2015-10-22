package com.monkeysarmy.fit;

import com.monkeysarmy.fit.MonkeyApp;
import com.monkeysarmy.fit.U2020Module;

final class Modules {
  static Object[] list(MonkeyApp app) {
    return new Object[] {
        new U2020Module(app)
    };
  }

  private Modules() {
    // No instances.
  }
}
