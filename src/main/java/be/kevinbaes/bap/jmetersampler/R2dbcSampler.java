package be.kevinbaes.bap.jmetersampler;

import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import be.kevinbaes.bap.jmetersampler.domain.DeviceEvent;
import be.kevinbaes.bap.jmetersampler.r2dbc.R2dbcTest;
import be.kevinbaes.bap.jmetersampler.r2dbc.R2dbcTestConfiguration;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.Interruptible;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import static be.kevinbaes.bap.jmetersampler.SamplerSetupUtil.*;
import static be.kevinbaes.bap.jmetersampler.r2dbc.R2dbcTestConfiguration.SELECT;

/**
 * Sampler which can perform a select or N insert queries against a postgres database.
 *
 * Use this sampler with care when using a sampler with more than one thread.
 * If you are overloading the server, you will get this error:
 * org.postgresql.util.PSQLException: FATAL: sorry, too many clients already
 *
 * The reason no to use threads is that the non-blocking IO makes it so a test
 * execution might interleave with another, meaning more active clients than
 * postgres allows. Comparing it with JDBC, the executions can't interleave because all calls block the current thread.
 *
 * This class was based on the SleepTest class from the jmeter source.
 */
public class R2dbcSampler extends AbstractJavaSamplerClient implements Serializable, Interruptible {

  private static final Logger LOG = LoggerFactory.getLogger(R2dbcSampler.class);

  private static int RETRY_COUNT_DEFAULT = 3;
  private static int RETRY_DELAY_DEFAULT = 100;
  private static int INSERT_COUNT_DEFAULT = 1;
  private static final String DRIVER_TYPE_DEFAULT = "pooled";

  private String samplerName;

  private R2dbcTest r2dbcTest;
  private final SamplerSetupUtil setupUtil;

  private transient volatile Thread myThread;

  /**
   * The Java Sampler uses the default constructor to instantiate an instance
   * of the client class.
   */
  public R2dbcSampler() {
    setupUtil = new SamplerSetupUtil();
    LOG.debug(whoAmI() + "\tConstruct");
  }

  /**
   * Get configuration parameters and initialize classes
   */
  @Override
  public void setupTest(JavaSamplerContext context) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(whoAmI() + "\tConnection factory created");
      listParameters(context);
    }

    String driverType = context.getParameter(DRIVER_TYPE_PARAM);
    String queryType = context.getParameter(QUERY_TYPE_PARAM, SELECT);
    int insertCount = context.getIntParameter(INSERT_COUNT_PARAM, INSERT_COUNT_DEFAULT);
    int retryCount = context.getIntParameter(RETRY_COUNT_PARAM, RETRY_COUNT_DEFAULT);
    int retryDelay = context.getIntParameter(RETRY_DELAY_PARAM, RETRY_DELAY_DEFAULT);

    try {
      ConnectionOptions options = setupUtil.connectionOptions(context);
      r2dbcTest = new R2dbcTest(new R2dbcTestConfiguration(driverType, queryType, insertCount, retryCount, retryDelay), options);
    } catch (Exception e) {
      LOG.error("something went wrong initializing r2dbctest", e);
    }
    samplerName = context.getParameter(TestElement.NAME);
  }

  /**
   *
   *
   * <code>SampleResult</code> has many fields which can be used. At a
   * minimum, the test should use <code>SampleResult.sampleStart</code> and
   * <code>SampleResult.sampleEnd</code>to set the time that the test
   * required to execute. It is also a good idea to set the sampleLabel and
   * the successful flag.
   *
   * @param javaSamplerContext the context to run with. This provides access to
   *                initialization parameters and JMeter variables.
   * @return a SampleResult giving the results of this sample.
   */
  public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
    SampleResult results = new SampleResult();
    results.setSampleLabel(samplerName);

    try {
      results.sampleStart();

      myThread = Thread.currentThread();
      if(r2dbcTest != null) {
        List<DeviceEvent> events = r2dbcTest.performDatabaseQueries(results);
        LOG.info("results contained [{}] events", events);
        results.setSuccessful(true);
      } else {
        LOG.error("R2dbc test is null");
        results.setSuccessful(false);
      }

      myThread = null;
    } catch (Exception e) {
      LOG.error("R2dbcTest: error during sample", e);
      results.setSuccessful(false);
      results.setResponseMessage(e.toString());
    } finally {
      LOG.info("r2dbc test ended");
      results.sampleEnd();
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(whoAmI() + "\trunTest()" + "\tTime:\t" + results.getTime());
      listParameters(javaSamplerContext);
    }

    return results;
  }

  @Override
  public void teardownTest(JavaSamplerContext context) {
    if(this.r2dbcTest != null) {
      this.r2dbcTest.testEnded();
    }
  }

  /**
   * Provide a list of parameters which this test supports. Any parameter
   * names and associated values returned by this method will appear in the
   * GUI by default so the user doesn't have to remember the exact names. The
   * user can add other parameters which are not listed here. If this method
   * returns null then no parameters will be listed. If the value for some
   * parameter is null then that parameter will be listed in the GUI with an
   * empty value.
   *
   * @return a specification of the parameters used by this test which should
   * be listed in the GUI, or null if no parameters should be listed.
   */
  @Override
  public Arguments getDefaultParameters() {
    Arguments params = setupUtil.defaultArguments();

    params.addArgument(DRIVER_TYPE_PARAM, DRIVER_TYPE_DEFAULT);
    params.addArgument(QUERY_TYPE_PARAM, SELECT);
    params.addArgument(INSERT_COUNT_PARAM, Integer.toString(INSERT_COUNT_DEFAULT));
    params.addArgument(RETRY_COUNT_PARAM, Integer.toString(RETRY_COUNT_DEFAULT));
    params.addArgument(RETRY_DELAY_PARAM, Integer.toString(RETRY_DELAY_DEFAULT));

    return params;
  }

  /**
   * Dump a list of the parameters in this context to the debug log.
   *
   * @param context the context which contains the initialization parameters.
   */
  private void listParameters(JavaSamplerContext context) {
    Iterator<String> argsIt = context.getParameterNamesIterator();
    while (argsIt.hasNext()) {
      String lName = argsIt.next();
      LOG.debug(lName + "=" + context.getParameter(lName));
    }
  }

  /**
   * Generate a String identifier of this test for debugging purposes.
   *
   * @return a String identifier for this test instance
   */
  private String whoAmI() {
    StringBuilder sb = new StringBuilder();
    sb.append(Thread.currentThread().toString());
    sb.append("@");
    sb.append(Integer.toHexString(hashCode()));
    return sb.toString();
  }

  public void interrupt(Thread thread) {
    if (thread != null) {
      thread.interrupt();
    }
  }

}
