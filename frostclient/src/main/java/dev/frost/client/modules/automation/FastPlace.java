// FastPlace | Automation | Fabric 1.21.1
// removes the 4-tick block place delay via mixin flag
package dev.frost.client.modules.automation;

import dev.frost.client.modules.Module;

public class FastPlace extends Module {

    // read by MixinClientPlayerInteractionManager
    public static boolean noPlaceDelay = false;

    public FastPlace() { super("FastPlace", Category.AUTOMATION); }

    @Override protected void onEnable()  { noPlaceDelay = true; }
    @Override protected void onDisable() { noPlaceDelay = false; }
}
