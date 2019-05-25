package be.kevinbaes.bap.jmetersampler;

import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import be.kevinbaes.bap.jmetersampler.jdbc.JdbcTest;
import be.kevinbaes.bap.jmetersampler.jdbc.JdbcTestConfiguration;
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
import static be.kevinbaes.bap.jmetersampler.jdbc.JdbcTestConfiguration.UNPOOLED;

/**
 * Sampler that can insert and select using JDBC driver.
 * The reason to use this over the standard jdbc sampler in jmeter is that this
 * implementation comes as close as possible to the R2DBC implementation, meaning
 * more reliable result tests.
 */
public class JdbcSampler  extends AbstractJavaSamplerClient implements Serializable, Interruptible {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcSampler.class);

  private static final String INSERT = "insert";
  private static final String SELECT = "select";


  private static int INSERT_COUNT_DEFAULT = 1;
  private static final String DRIVER_TYPE_DEFAULT = UNPOOLED;



  // The name of the sampler
  private String name;

  private transient volatile Thread myThread;

  private JdbcTest jdbcTest;
  private final SamplerSetupUtil setupUtil;

  /**
   * The Java Sampler uses the default constructor to instantiate an instance
   * of the client class.
   */
  public JdbcSampler() {
    setupUtil = new SamplerSetupUtil();
    LOG.debug(whoAmI() + "\tConstruct");
  }

  /**
   * Get driver configuration parameters and initialze JdbcRepository
   */
  @Override
  public void setupTest(JavaSamplerContext context) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(whoAmI() + "\tConnection factory created");
      listParameters(context);
    }

    String driverType = context.getParameter(DRIVER_TYPE_PARAM, DRIVER_TYPE_DEFAULT);
    String queryType = context.getParameter(QUERY_TYPE_PARAM, SELECT);
    int insertCount = context.getIntParameter(INSERT_COUNT_PARAM, INSERT_COUNT_DEFAULT);

    ConnectionOptions options = setupUtil.connectionOptions(context);

    LOG.info("insert count: " + insertCount);

    try{
      this.jdbcTest = new JdbcTest(new JdbcTestConfiguration(driverType, queryType, insertCount), options);
    } catch (Exception e) {
      LOG.error("something went wrong initializing JDBC test", e);
    }
    name = context.getParameter(TestElement.NAME);
  }

  /**
   * Perform a single sample. In this case, this method will simply sleep for
   * some amount of time. Perform a single sample for each iteration. This
   * method returns a <code>SampleResult</code> object.
   * <code>SampleResult</code> has many fields which can be used. At a
   * minimum, the test should use <code>SampleResult.sampleStart</code> and
   * <code>SampleResult.sampleEnd</code>to set the time that the test
   * required to execute. It is also a good idea to set the sampleLabel and
   * the successful flag.
   *
   * @param javaSamplerContext the context to run with. This provides access to
   *                initialization parameters.
   * @return a SampleResult giving the results of this sample.
   * @see org.apache.jmeter.samplers.SampleResult#sampleStart()
   * @see org.apache.jmeter.samplers.SampleResult#sampleEnd()
   * @see org.apache.jmeter.samplers.SampleResult#setSuccessful(boolean)
   * @see org.apache.jmeter.samplers.SampleResult#setSampleLabel(String)
   */
  public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
    SampleResult results = new SampleResult();
    results.setSampleLabel(name);
    // Only do the calculation if it is needed
    results.setSamplerData("Sleep Test: time");

    try {
      // Record sample start time.
      results.sampleStart();
      myThread = Thread.currentThread();
      // Execute the sample. In this case sleep for the
      // specified time.

      List<?> response = jdbcTest.performDatabaseQueries(results);
      if(response != null) {
        LOG.info("response contained [{}] events", response.size());
      }

      myThread = null;

      results.setSuccessful(true);
    } catch (Exception e) {
      LOG.error("JdbcTest: error during sample", e);
      results.setSuccessful(false);
      results.setResponseMessage(e.toString());
    } finally {
      results.sampleEnd();
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(whoAmI() + "\trunTest()" + "\tTime:\t" + results.getTime());
      listParameters(javaSamplerContext);
    }

    return results;
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
    Arguments params = new SamplerSetupUtil().defaultArguments();

    params.addArgument(DRIVER_TYPE_PARAM, UNPOOLED);
    params.addArgument(QUERY_TYPE_PARAM, SELECT);
    params.addArgument(INSERT_COUNT_PARAM, Integer.toString(INSERT_COUNT_DEFAULT));

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
