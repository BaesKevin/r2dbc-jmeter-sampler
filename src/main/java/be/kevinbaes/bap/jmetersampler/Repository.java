package be.kevinbaes.bap.jmetersampler;

import org.apache.jmeter.samplers.SampleResult;

import java.util.List;

/**
 * Contract for repositories that provide test functionality for either JdbcSampler or R2dbcSampler.
 * @param <T> The type used in select queries.
 */
public interface Repository<T> {

  /**
   * Insert a number of records in a sequential manner, meaning that the previous query must be completed
   * before the next start. Completed means that the previous query has performed the query and released its
   * connection.
   *
   * @param sampleResult set parameters like connect end or latency, do not call sampleEnd
   */
  void insertSequential(SampleResult sampleResult);

  /**
   * Insert a number of records in a concurrent manner, meaning that queries can overlap.
   *
   * @implNote it is up to the you how the concurrency happens, as long as all queries are complete when the method returns
   *
   * @param sampleResult set parameters like connect end or latency, do not call sampleEnd
   */
  void insertInterleaved(SampleResult sampleResult);

  /**
   * Perform any query that returns rows that can be mapped to domain objects.
   *
   * @param sampleResult set parameters like connect end or latency, do not call sampleEnd
   * @return the rows mapped to domain objects
   */
  List<? extends T> select(SampleResult sampleResult);
}
