package com.ecommerce.test;

/**
 * Master test suite runner executing all design pattern and frontend verification test cases.
 * 
 * @author baktiyarovamereilim-lang
 */
public class TestSuiteRunner {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("E-COMMERCE SHOPPING CART - AUTOMATED TEST SUITE RUNNER");
        System.out.println("================================================================================\n");

        int passed = 0;
        int failed = 0;

        Runnable[] testSuites = {
                FactoryMethodPatternTest::runTests,
                BuilderPatternTest::runTests,
                ObserverPatternTest::runTests,
                StrategyPatternTest::runTests,
                DecoratorPatternTest::runTests,
                AdapterPatternTest::runTests,
                FacadePatternTest::runTests,
                FrontendLogicTest::runTests
        };

        for (Runnable test : testSuites) {
            try {
                test.run();
                passed++;
            } catch (Throwable t) {
                System.err.println("  FAILED: " + t.getMessage());
                failed++;
            }
        }

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.printf("Test Execution Results: %d Passed, %d Failed (Total: %d suites)%n",
                passed, failed, passed + failed);
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
