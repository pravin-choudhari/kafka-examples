import java.util.Properties;
import model.Account;
import model.Company;
import model.Trade;
import model.TradeDetail;
import model.TradeSummary;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import serde.JsonPOJODeserializer;
import serde.JsonPOJOSerializer;

public class VanillaKafkaStream {

  public static final String TRADES_TOPIC = "trades";
  public static final String ACCOUNT_TOPIC = "account";
  public static final String COMPANY_TOPIC = "company";
  public static final String TRADE_SUMMARY_TOPIC = "trade_summary";

  public static void main(String[] args) {
    final Topology topology = buildTopology();
    final KafkaStreams kafkaStreams = new KafkaStreams(topology, getKafkaStreamsKafkaConfig());

    kafkaStreams.start();
    System.out.println("Started Kafka streams app");
  }

  private static Properties getKafkaStreamsKafkaConfig() {
    final Properties conf = new Properties();

    conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    conf.put(StreamsConfig.APPLICATION_ID_CONFIG, "Sample_App");
    conf.put(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, "20000");
    conf.put(StreamsConfig.RETRY_BACKOFF_MS_CONFIG, "500");
    return conf;
  }

  private static Topology buildTopology() {
    final StreamsBuilder builder = new StreamsBuilder();

    //Get stream of trades
    final KStream<String, Trade> tradeKStream = builder.stream(TRADES_TOPIC,
        Consumed.with(Serdes.String(),
            Serdes.serdeFrom(new JsonPOJOSerializer<Trade>(),
                new JsonPOJODeserializer<Trade>(Trade.class))));

    //Get stream of Account
    final GlobalKTable<String, Account> accounts = builder.globalTable(ACCOUNT_TOPIC,
        Materialized.<String, Account, KeyValueStore<Bytes, byte[]>>as("account-store")
            .withKeySerde(Serdes.String())
            .withValueSerde(Serdes.serdeFrom(new JsonPOJOSerializer<Account>(),
                new JsonPOJODeserializer<Account>(Account.class))));

    //Get stream of Company
    final GlobalKTable<String, Company> companyData = builder.globalTable(COMPANY_TOPIC,
        Materialized.<String, Company, KeyValueStore<Bytes, byte[]>>as("company-store")
            .withKeySerde(Serdes.String())
            .withValueSerde(Serdes.serdeFrom(new JsonPOJOSerializer<Company>(),
                new JsonPOJODeserializer<Company>(Company.class))));

    // join the incoming trade stream with account table
    final KStream<String, TradeDetail> accountEnrichedKStream = tradeKStream.leftJoin(accounts,
        (accountKey, trade) -> {
          return trade.getAccountKey();
        }, (trade, account) -> new TradeDetail(trade, account));

    //join this enriched data with the company data
    final KStream<String, TradeSummary> finalData =
        accountEnrichedKStream.join(companyData,
            (key, expect0) -> expect0.getCompanyId(),
            (tradeDetail, company) -> new TradeSummary(tradeDetail, company));

    //Aggregate the final enriched data
    //This stream would aggregate and publish data to output topic
    finalData.groupByKey(
        Grouped.with(Serdes.String(),
            Serdes.serdeFrom(new JsonPOJOSerializer<TradeSummary>(),
                new JsonPOJODeserializer<TradeSummary>(TradeSummary.class))))
        .aggregate(() -> {
          return new TradeSummary(null, null);
        }, (k, newSummary, aggregateSummary) -> {
          final Double netAmt = newSummary.getNetAmount() + aggregateSummary.getNetAmount();
          final Integer quantity =
              newSummary.getTradedQuantity() + aggregateSummary.getTradedQuantity();

          newSummary.setNetAmount(netAmt);
          newSummary.setTradedQuantity(quantity);
          return newSummary;
        }, Materialized.with(Serdes.String(),
            Serdes.serdeFrom(new JsonPOJOSerializer<TradeSummary>(),
                new JsonPOJODeserializer<TradeSummary>(TradeSummary.class))))
        .toStream()
        .to(TRADE_SUMMARY_TOPIC);

    return builder.build();
  }
}
