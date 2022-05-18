package it.gov.pagopa.rtd.ms.rtdmsfileregister;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RtdMsFileRegisterApplication {

  private static final Logger log = LoggerFactory.getLogger(RtdMsFileRegisterApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(RtdMsFileRegisterApplication.class, args);
  }

  @Autowired
  RtdFileRepository repository;

  @Bean
  public CommandLineRunner demo(RtdFileRepository repository) {
    return (args) -> {

      RtdFile tmp1 = new RtdFile();
      tmp1.setStatus(0);
      tmp1.setName("test");
      RtdFile tmp2 = new RtdFile();
      tmp2.setStatus(1);
      tmp2.setName("prova");
      RtdFile tmp3 = new RtdFile();
      tmp3.setStatus(1);
      tmp3.setName("file1");
      RtdFile tmp4 = new RtdFile();
      tmp4.setStatus(5);
      tmp4.setName("primo");
      RtdFile tmp5 = new RtdFile();
      tmp5.setStatus(5);
      tmp5.setName("secondo");

      repository.save(tmp1);
      repository.save(tmp2);
      repository.save(tmp3);
      repository.save(tmp4);
      repository.save(tmp5);

      // fetch all customers
      log.info("Files found with findAll():");
      log.info("-------------------------------");
      for (RtdFile customer : repository.findAll()) {
        log.info(customer.toString());
      }
      log.info("");

      // fetch an individual customer by ID
      RtdFile customer = repository.findByName("test");
      log.info("File found with findByName(\"test\"):");
      log.info("--------------------------------");
      log.info(customer.toString());
      log.info("");

      // fetch customers by last name
      log.info("Customer found with findByStatus(1):");
      log.info("--------------------------------------------");
      repository.findByStatus(1).forEach(bauer -> {
        log.info(bauer.toString());
      });
      // for (Customer bauer : repository.findByLastName("Bauer")) {
      //  log.info(bauer.toString());
      // }
      log.info("");
    };
  }

}
