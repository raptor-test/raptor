//package com.raptor;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.jupiter.api.*;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.io.IOException;
//
//@SpringBootTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//public class ApplicationTest extends RaptorBase{
//    @BeforeAll
//    public void beforeClass()  {
//        initiateReporting("Test-Scans");
//    }
//
//    @AfterAll
//    public void afterClass()  {
//        closeReporting();
//    }
//
//    @Test
//    @DisplayName("testCTScan")
//    public void testCTScan(TestInfo testInfo) throws InterruptedException, IOException {
//        String testFileName = "scans/test-ct-scan.json";
//        runRaptorScript(testFileName,null);
//    }
//
//    @Test
//    @DisplayName("testMriScan")
//    public void testMriScan(TestInfo testInfo) throws InterruptedException, IOException {
//        String testFileName = "scans/test-mri-scan.json";
//        runRaptorScript(testFileName,null);
//    }
//}
