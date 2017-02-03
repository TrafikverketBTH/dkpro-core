package org.dkpro.core.stagger;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ StockholmAbbreviationsSegmenterTest.class, StockholmPosTaggerTest.class, StockholmSegmenterTest.class })
public class AllTests {

}
