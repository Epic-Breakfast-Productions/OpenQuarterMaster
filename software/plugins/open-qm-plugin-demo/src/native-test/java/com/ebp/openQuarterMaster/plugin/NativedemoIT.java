package com.ebp.openQuarterMaster.plugin;

import com.ebp.openQuarterMaster.plugin.demo.quarkus.DemoTest;
import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativedemoIT extends DemoTest {

    // Execute the same tests but in native mode.
}