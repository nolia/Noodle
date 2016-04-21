package com.noodle;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * @author Nikolay Soroka - Stanfy (http://stanfy.com)
 */
@Ignore
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public abstract class BaseTests {

}
