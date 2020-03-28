
package com.crio.warmup.stock.quotes;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {

  private RestTemplate restTemplate;

  public AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
        throws JsonProcessingException {
    String url = buildUri(symbol);
    ObjectMapper mapper = getObjectMapper();
    String result = restTemplate.getForObject(url,String.class); 
    AlphavantageDailyResponse response = mapper.readValue(result, AlphavantageDailyResponse.class);
    List<AlphavantageCandle> stockQuCandles = new ArrayList<AlphavantageCandle>();
    Map<LocalDate, AlphavantageCandle> ans = response.getCandles();
    for (Map.Entry<LocalDate, AlphavantageCandle> item : ans.entrySet()) {
      if ((item.getKey().isEqual(from) || item.getKey().isAfter(from)) 
          && (item.getKey().isBefore(to) || item.getKey().isEqual(to))) {
        AlphavantageCandle input = item.getValue();
        input.setDate(item.getKey());
        stockQuCandles.add(input);
      }
    }
    Collections.sort(stockQuCandles,Comparator.comparing(AlphavantageCandle::getDate));
    List<Candle> alphaCandle = new ArrayList<Candle>();
    alphaCandle.addAll(stockQuCandles);
    return alphaCandle;
  }

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Inplement the StockQuoteService interface as per the contracts.
  // The implementation of this functions will be doing following tasks
  // 1. Build the appropriate url to communicate with thirdparty.
  // The url should consider startDate and endDate if it is supported by the
  // provider.
  // 2. Perform thirdparty communication with the Url prepared in step#1
  // 3. Map the response and convert the same to List<Candle>
  // 4. If the provider does not support startDate and endDate, then the
  // implementation
  // should also filter the dates based on startDate and endDate.
  // Make sure that result contains the records for for startDate and endDate
  // after filtering.
  // 5. return a sorted List<Candle> sorted ascending based on Candle#getDate
  // Call alphavantage service to fetch daily adjusted data for last 20 years.
  // Refer to
  // documentation here - https://www.alphavantage.co/documentation/
  // Make sure you use {RestTemplate#getForObject(URI, String)} else the test will
  // fail.
  // Run the tests using command below and make sure it passes
  // ./gradlew test --tests AlphavantageServiceTest
  // CHECKSTYLE:OFF
  // CHECKSTYLE:ON
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Write a method to create appropriate url to call alphavantage service. Method
  // should
  // be using configurations provided in the {@link @application.properties}.
  // Use thie method in #getStockQuote.

  protected String buildUri(String symbol) {
    String uriTemplate = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + symbol
        + "&outputsize=full&apikey=Z1VRBERM9RP3Z0QM";
    return uriTemplate;
  }

}
