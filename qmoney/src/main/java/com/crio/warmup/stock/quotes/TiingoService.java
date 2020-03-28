
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {


  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Now we will be separating communication with Tiingo from PortfolioManager.
  //  Generate the functions as per the declarations in the interface and then
  //  Move the code from PortfolioManagerImpl#getSTockQuotes inside newly created method.
  //  Run the tests using command below -
  //  ./gradlew test --tests TiingoServiceTest and make sure it passes.
  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) 
      throws JsonProcessingException {
    String url = buildUri(symbol, from, to);
    String result = restTemplate.getForObject(url, String.class);
    ObjectMapper mapper = getObjectMapper();
    List<TiingoCandle> coll = mapper.readValue(
            result, new TypeReference<ArrayList<TiingoCandle>>() {
            });
    Collections.sort(coll,Comparator.comparing(TiingoCandle::getDate));
    List<Candle> stockQuList = new ArrayList<Candle>();
    stockQuList.addAll(coll);
    return stockQuList;
  }



  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Write a method to create appropriate url to call tiingo service.
  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate=" + startDate
        + "&endDate=" + endDate + "&token=765d771c192650d7d2e334ecbc2032149540ccf9";
    return uriTemplate;
  }
}
