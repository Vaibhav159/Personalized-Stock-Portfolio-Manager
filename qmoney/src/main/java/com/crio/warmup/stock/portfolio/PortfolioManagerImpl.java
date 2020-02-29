package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Now we want to convert our code into a module, so we will not call it from
  // main anymore.
  // Copy your code from Module#3
  // PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and make sure that it
  // follows the method signature.
  // Logic to read Json file and convert them into Objects will not be required
  // further as our
  // clients will take care of it, going forward.
  // Test your code using Junits provided.
  // Make sure that all of the tests inside PortfolioManagerTest using command
  // below -
  // ./gradlew test --tests PortfolioManagerTest
  // This will guard you against any regressions.
  // run ./gradlew build in order to test yout code, and make sure that
  // the tests and static code quality pass.

  // CHECKSTYLE:OFF
  // public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
  // PortfolioTrade trade, Double buyPrice,Double sellPrice)
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate) {
    double totalReturn;
    double annualized;
    double maxdays;
    double daysBetween;
    Double buyPrice;
    double sellPrice;
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < portfolioTrades.size(); i++) {
      list.add(portfolioTrades.get(i).getSymbol());
    }
    List<AnnualizedReturn> ar = new ArrayList<AnnualizedReturn>();
    try {
      for (int i = 0; i < list.size(); i++) {
        List<TiingoCandle> collection = getStockQuote(portfolioTrades.get(i).getSymbol(),
            portfolioTrades.get(i).getPurchaseDate(), endDate);

        daysBetween = ChronoUnit.DAYS.between(portfolioTrades.get(i).getPurchaseDate(), endDate);
        buyPrice = collection.get(0).getOpen();
        sellPrice = collection.get(collection.size() - 1).getClose();
        maxdays = 365 / daysBetween;
        totalReturn = (sellPrice - buyPrice) / buyPrice;
        annualized = Math.pow((1 + totalReturn), maxdays) - 1;
        ar.add(i, new AnnualizedReturn(portfolioTrades.get(i).getSymbol(), annualized, totalReturn));
      }
    } catch (JsonProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Collections.sort(ar, Collections.reverseOrder(Comparator.comparingDouble(AnnualizedReturn::getAnnualizedReturn)));
    return ar;

  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo thirdparty APIs to a separate function.
  // It should be split into fto parts.
  // Part#1 - Prepare the Url to call Tiingo based on a template constant,
  // by replacing the placeholders.
  // Constant should look like
  // https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=?&endDate=?&token=?
  // Where ? are replaced with something similar to <ticker> and then actual url
  // produced by
  // replacing the placeholders with actual parameters.

  public List<TiingoCandle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {

    String s = buildUri(symbol, from, to);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String result = restTemplate.getForObject(s, String.class);
    List<TiingoCandle> collection = mapper.readValue(result, new TypeReference<ArrayList<TiingoCandle>>() {
    });
    return collection;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate=" + startDate
        + "&endDate=" + endDate + "&token=765d771c192650d7d2e334ecbc2032149540ccf9";
    return uriTemplate;
  }
}
