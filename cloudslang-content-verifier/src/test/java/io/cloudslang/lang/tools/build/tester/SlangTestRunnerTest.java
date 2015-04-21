package io.cloudslang.lang.tools.build.tester;

import com.google.common.collect.ImmutableMap;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by stoneo on 4/15/2015.
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangTestRunnerTest.Config.class)
public class SlangTestRunnerTest {


    @Autowired
    private SlangTestRunner slangTestRunner;

    @Autowired
    private TestCasesYamlParser parser;

    @Autowired
    private Slang slang;

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Before
    public void resetMocks() {
        Mockito.reset(parser);
        Mockito.reset(slang);
    }

    @Test
    public void createTestCaseWithNullTestPath() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangTestRunner.createTestCases(null);
    }

    @Test
    public void createTestCaseWithEmptyTestPath(){
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangTestRunner.createTestCases("");
    }

    @Test
    public void createTestCaseWithInvalidTestPath() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("directory");
        slangTestRunner.createTestCases("aaa");
    }

    @Test
    public void createTestCaseWithPathWithNoTests() throws Exception {
        URI resource = getClass().getResource("/dependencies").toURI();
        Map<String, SlangTestCase> testCases = slangTestRunner.createTestCases(resource.getPath());
        Assert.assertEquals("No test cases were supposed to be created", 0, testCases.size());
    }

    @Test
    public void createTestCaseWithPathWithValidTests() throws Exception {
        URI resource = getClass().getResource("/test/valid").toURI();
        Map<String, SlangTestCase> testCases = new HashMap<>();
        testCases.put("Test1", new SlangTestCase("Test1", "path", "desc", null, null, null, null, null, null));
        when(parser.parseTestCases(Mockito.any(SlangSource.class))).thenReturn(testCases);
        Map<String, SlangTestCase> foundTestCases = slangTestRunner.createTestCases(resource.getPath());
        Assert.assertEquals("1 test case was supposed to be created", 1, foundTestCases.size());
    }

    @Test
    public void runTestCasesFromEmptyMap(){
        Map<SlangTestCase, String> failedTests = slangTestRunner.runAllTests("path", new HashMap<String, SlangTestCase>(), new HashMap<String, CompilationArtifact>(), null);
        Assert.assertEquals("No test cases should fail", 0, failedTests.size());
    }

    @Test
    public void runTestCasesFromNullMap(){
        Map<SlangTestCase, String> failedTests = slangTestRunner.runAllTests("path", null, new HashMap<String, CompilationArtifact>(), null);
        Assert.assertEquals("No test cases should fail", 0, failedTests.size());
    }

    @Test
    public void runNullTestCase(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        testCases.put("test1", null);
        Map<SlangTestCase, String> failedTests = slangTestRunner.runAllTests("path", testCases, new HashMap<String, CompilationArtifact>(), null);
        Assert.assertEquals("1 test case should fail", 1, failedTests.size());
        String errorMessage = failedTests.values().iterator().next();
        Assert.assertTrue(errorMessage.contains("Test case"));
        Assert.assertTrue(errorMessage.contains("null"));
    }

    @Test
    public void runTestCaseWithNoTestFlowPathProperty(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", null, null, null, null, null, null, null, null);
        testCases.put("test1", testCase);
        Map<SlangTestCase, String> failedTests = slangTestRunner.runAllTests("path", testCases, new HashMap<String, CompilationArtifact>(), null);
        Assert.assertEquals("1 test case should fail", 1, failedTests.size());
        String errorMessage = failedTests.values().iterator().next();
        Assert.assertTrue(errorMessage.contains("testFlowPath"));
        Assert.assertTrue(errorMessage.contains("mandatory"));
    }

    @Test
    public void runTestCaseWithNoCompiledFlow(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", null, null, null, null, null, null, null);
        testCases.put("test1", testCase);
        Map<SlangTestCase, String> failedTests = slangTestRunner.runAllTests("path", testCases, new HashMap<String, CompilationArtifact>(), null);
        Assert.assertEquals("1 test case should fail", 1, failedTests.size());
        String errorMessage = failedTests.values().iterator().next();
        Assert.assertTrue(errorMessage.contains("testFlowPath"));
        Assert.assertTrue(errorMessage.contains("missing"));
    }

    @Test
    public void runTestCaseWithCompiledFlow(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", null, null, null, null, null, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ScoreEventListener listener = (ScoreEventListener) invocationOnMock.getArguments()[0];
                listener.onEvent(new ScoreEvent(ScoreLangConstants.EVENT_EXECUTION_FINISHED, ImmutableMap.of(LanguageEventData.RESULT, "SUCCESS")));
                return listener;
            }
        }).when(slang).subscribeOnEvents(any(ScoreEventListener.class), anySetOf(String.class));
        Map<SlangTestCase, String> failedTests = slangTestRunner.runAllTests("path", testCases, compiledFlows, null);
        Assert.assertEquals("No test cases should fail", 0, failedTests.size());
    }


    @Configuration
    static class Config {

        @Bean
        public SlangTestRunner slangTestRunner() {
            return new SlangTestRunner();
        }

        @Bean
        public TestCasesYamlParser parser(){
            return mock(TestCasesYamlParser.class);
        }

        @Bean
        public Yaml yaml(){
            return mock(Yaml.class);
        }

        @Bean
        public Slang slang(){
            return mock(Slang.class);
        }
    }}
