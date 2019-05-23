package be.kevinbaes.bap;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.ConnectionFactory;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import sun.nio.ch.Interruptible;

import java.io.Serializable;
import java.util.Iterator;

/**
 * This class was based on the SleepTest class from the jmeter source.
 */
public class R2dbcSampler extends AbstractJavaSamplerClient implements Serializable, Interruptible {

  private static final Logger LOG = LoggerFactory.getLogger(R2dbcSampler.class);

  private static final String QUERY_TYPE_PARAM = "Query type";
  private static final String INSERT_COUNT_PARAM = "Insert count";

  private static final String INSERT = "insert";
  private static final String SELECT = "select";

  private static int INSERT_COUNT_DEFAULT = 1;

  private String queryType;

  // The name of the sampler
  private String name;
  private int insertCount;

  private transient volatile Thread myThread;

  private ConnectionFactory connectionFactory;
  private R2dbcGoalRepository goalRepository;

  /**
   * The Java Sampler uses the default constructor to instantiate an instance
   * of the client class.
   */
  public R2dbcSampler() {
    LOG.debug(whoAmI() + "\tConstruct");
  }

  /**
   * Get driver configuration parameters and initialze R2dbcGoalRepository
   */
  @Override
  public void setupTest(JavaSamplerContext context) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(whoAmI() + "\tConnection factory created");
      listParameters(context);
    }

    queryType = context.getParameter(QUERY_TYPE_PARAM, SELECT);
    insertCount = context.getIntParameter(INSERT_COUNT_PARAM, INSERT_COUNT_DEFAULT);

    this.connectionFactory = new R2dbcConfig().postgresConnectionFactory();
    this.goalRepository = new R2dbcGoalRepository(connectionFactory);

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

      if(queryType.equals(INSERT)) {
        LOG.info("inserting [{}] times", insertCount);

        Mono<Void> inserts = Mono.empty();

        for (int i = 0; i < insertCount; i++) {
          inserts = inserts.then(goalRepository.insert(i));
        }

        inserts.block();
      } else {
        goalRepository
            .select()
            .block();
      }

      myThread = null;

      results.setSuccessful(true);
    } catch (Exception e) {
      LOG.error("R2dbcTest: error during sample", e);
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
    Arguments params = new Arguments();

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
